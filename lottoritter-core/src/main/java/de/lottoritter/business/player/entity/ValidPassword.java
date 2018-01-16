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

import static java.lang.annotation.ElementType.TYPE;

/**
 * @author Ulrich Cech
 */
@Constraint(validatedBy = {ValidPassword.Validator.class})
@Target(value = {TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "{key}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    class Validator implements ConstraintValidator<ValidPassword, Player> {

        @Override
        public void initialize(ValidPassword constraintAnnotation) {
        }

        @Override
        public boolean isValid(Player entity, ConstraintValidatorContext constraintValidatorContext) {
            if (entity.getPasswordAgain() != null) {
                if (entity.getPasswordAgain().equals(entity.getPassword())) {
                    return true;
                } else {
                    constraintValidatorContext.disableDefaultConstraintViolation();
                    constraintValidatorContext
                            .buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                            .addPropertyNode("passwordAgain").addConstraintViolation();
                    return false;
                }
            }
            return false;
        }

    }
}
