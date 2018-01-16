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

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.annotation.ElementType.FIELD;

/**
 * The annotated element for username must be unique for the whole platform.
 *
 * @author Ulrich Cech
 */
@Documented
@Constraint(validatedBy = {LocaleCodeValid.Validator.class})
@Target(value = {FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LocaleCodeValid {
    String message() default "{localecode_not_valid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    /**
     * Check that the username if unique for the whole platform.
     */
    class Validator implements ConstraintValidator<LocaleCodeValid, String> {

        @Override
        public void initialize(LocaleCodeValid constraintAnnotation) {
        }

        /**
         * Checks if the locale string is valid (eg. de-DE).
         *
         * @param value The string to validate.
         * @param constraintValidatorContext context in which the constraint is evaluated.
         *
         */
        @Override
        public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
            if (Objects.isNull(value)) {
                return false;
            }
            Pattern pattern = Pattern.compile("(^[a-z]{2}\\-[A-Z]{2}$)|([a-z]{2})");
            Matcher matcher = pattern.matcher(value);
            return matcher.matches();
        }

    }
}
