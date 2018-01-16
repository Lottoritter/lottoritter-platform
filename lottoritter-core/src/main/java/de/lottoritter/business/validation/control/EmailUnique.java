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

import de.lottoritter.business.player.entity.Player;
import de.lottoritter.platform.cdi.CDIBeanService;
import org.mongodb.morphia.Datastore;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * The annotated element for the eMail must be unique for the whole platform.
 *
 * @author Ulrich Cech
 */
@Documented
@Constraint(validatedBy = {EmailUnique.Validator.class})
@Target(value = {TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface EmailUnique {

    String DB_PROPERTY_EMAIL = "email";

    String message() default "{email_not_unique}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    /**
     * Check that the eMail is unique for the whole platform.
     */
    class Validator implements ConstraintValidator<EmailUnique, Player> {

        private Datastore datastore;

        @Override
        public void initialize(EmailUnique constraintAnnotation) {
            datastore = CDIBeanService.getInstance().getCDIBean(Datastore.class);
        }

        @Override
        public boolean isValid(Player player, ConstraintValidatorContext constraintValidatorContext) {
            boolean valid;
            if (player.isNew()) {
                valid = (datastore.find(Player.class).filter(DB_PROPERTY_EMAIL, player.getEmail()).get() == null);
            } else {
                valid =  datastore.createQuery(Player.class).filter(DB_PROPERTY_EMAIL, player.getEmail()).filter("_id !=", player.getId()).get() == null;
            }
            if (!valid) {
                constraintValidatorContext.disableDefaultConstraintViolation();
                constraintValidatorContext
                        .buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(DB_PROPERTY_EMAIL).addConstraintViolation();
                return false;
            }
            return true;
        }

    }
}
