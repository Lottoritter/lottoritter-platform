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

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles Bean Validation for entities.
 *
 * @author Ulrich Cech
 */
public class ValidationController {

    private static final Map<Class<?>, ValidationController> cache = new ConcurrentHashMap<>();


    private ValidationController() {
    }


    public static <T> ValidationController get(final Class<T> clazz) {
        ValidationController vc = cache.get(clazz);
        if (vc == null) {
            vc = new ValidationController();
            cache.put(clazz, vc);
        }
        return vc;
    }

    public <T> void processBeanValidation(final T entity) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> errors = validator.validate(entity);
        if (!errors.isEmpty()) {
            throw new ConstraintViolationException(errors);
        }
    }

    public <T> void processBeanValidationForProperty(final T entity, String propertyName) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> errors = validator.validateProperty(entity, propertyName);
        if (!errors.isEmpty()) {
            throw new ConstraintViolationException(errors);
        }
    }

    public <T> void processBeanValidationForGroup(final T entity, Class<?>... groupClasses) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> errors = validator.validate(entity, groupClasses);
        if (!errors.isEmpty()) {
            throw new ConstraintViolationException(errors);
        }
    }
}
