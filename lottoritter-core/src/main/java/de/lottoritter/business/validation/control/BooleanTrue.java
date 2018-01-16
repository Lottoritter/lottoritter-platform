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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * The annotated element must be a boolean and must be true.
 *
 * @author Ulrich Cech
 */
@Constraint(validatedBy = {BooleanTrue.Validator.class})
@Target(value = {FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface BooleanTrue {
    String message() default "{key}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    class Validator implements ConstraintValidator<BooleanTrue, Boolean> {

        public void initialize(BooleanTrue parameters) {
        }

        @Override
        public boolean isValid(Boolean value, ConstraintValidatorContext constraintValidatorContext) {
            return (value != null) && (value == Boolean.TRUE);
        }

    }
}
