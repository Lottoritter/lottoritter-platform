/*
 * Copyright 2017 Ulrich Cech & Christopher Schmidt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.lottoritter.business.validation.control;

import org.apache.commons.lang3.StringUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * The annotated element must not be null or empty, it must be filled with at least <code>min</code> legal character,
 * where <code>min</code> cannot be smaller than 1.
 *
 * Supported types are:
 * <ul>
 * <li><code>String</code> (string length is evaluated)</li>
 *
 * @author Ulrich Cech
 */
@Constraint(validatedBy = {NotEmpty.Validator.class})
@Target(value = {FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface NotEmpty {
    String message() default "{key}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return size the element must be higher or equal to
     */
    int min() default 1;


    class Validator implements ConstraintValidator<NotEmpty, String> {
        private int min;

        public void initialize(NotEmpty parameters) {
            min = parameters.min();
            validateParameters();
        }

        /**
         * Checks the allowed length of the specified string.
         *
         * @param value The string to validate.
         * @param constraintValidatorContext context in which the constraint is evaluated.
         *
         * @return Returns <code>false</code> if the string is <code>null</code> or blank-empty
         *  (<code>""</code> or <code>" "</code>),
         *  or the length of <code>value</code> is smaller than the given <code>min</code> parameter.
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
            if (StringUtils.isBlank(value)) {
                return false;
            }
            int length = value.length();
            return (length >= min);
        }

        private void validateParameters() {
            if ( min < 1 ) {
                throw new IllegalArgumentException( "The min parameter cannot be smaller than 1." );
            }
        }

    }
}
