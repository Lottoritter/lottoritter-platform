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
package de.lottoritter.business.player.entity;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.lang.annotation.ElementType.FIELD;

/**
 * @author Ulrich Cech
 */
@Constraint(validatedBy = {ValidDateFormat.Validator.class})
@Target(value = { FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ValidDateFormat {

    String message() default "{key}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String format() default "dd.MM.yyyy";


    class Validator implements ConstraintValidator<ValidDateFormat, String> {
        private String format;

        @Override
        public void initialize(ValidDateFormat constraintAnnotation) {
            format = constraintAnnotation.format();
        }

        @Override
        public boolean isValid(String dateString, ConstraintValidatorContext constraintValidatorContext) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.GERMANY);
            dateFormat.setLenient(false);
            try {
                dateFormat.parse(dateString);
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
    }
}
