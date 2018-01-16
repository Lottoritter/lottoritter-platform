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
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * The annotated element size must be between the specified boundaries (included) or the element is null or empty.
 *
 * Supported types are:
 * <ul>
 * <li><code>String</code> (string length is evaluated)</li>
 *
 * @author Ulrich Cech
 */
@Documented
@Constraint(validatedBy = {EmptyOrSize.Validator.class})
@Target(value = {METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface EmptyOrSize {
    String message() default "{key}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * @return size the element must be higher or equal to
     */
    int min() default 0;

    /**
     * @return size the element must be lower or equal to
     */
    int max() default Integer.MAX_VALUE;



    /**
     * Check that a string's length is empty (null or "") or between min and max.
     */
    class Validator implements ConstraintValidator<EmptyOrSize, String> {
        private int min;
        private int max;

        @Override
        public void initialize(EmptyOrSize parameters) {
            min = parameters.min();
            max = parameters.max();
            validateParameters();
        }

        /**
         * Checks the allowed length of the specified string.
         *
         * @param value The string to validate.
         * @param constraintValidatorContext context in which the constraint is evaluated.
         *
         * @return Returns <code>true</code> if the string is <code>null</code> or empty (<code>""</code>,
         *         or the length of <code>value</code> between the specified
         *         <code>min</code> and <code>max</code> values (inclusive), <code>false</code> otherwise.
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
            if (StringUtils.isBlank(value)) {
                return true;
            }
            int length = value.length();
            return ((length >= min) && (length <= max));
        }

        private void validateParameters() {
            if ( min < 0 ) {
                throw new IllegalArgumentException( "The min parameter cannot be negative." );
            }
            if ( max < 0 ) {
                throw new IllegalArgumentException( "The max parameter cannot be negative." );
            }
            if ( max < min ) {
                throw new IllegalArgumentException( "The length cannot be negative." );
            }
        }

    }
}
