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
package de.lottoritter.business.payment.entity;

import de.lottoritter.business.validation.control.ValidationController;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Ulrich Cech
 */
public class PaymentLimitsTest {

    @Test
    public void testDayLimit() {
        PaymentLimits cut = new PaymentLimits();
        cut.setDayLimitInEuro(2000);
        try {
            ValidationController.get(PaymentLimits.class).processBeanValidationForProperty(cut, "dayLimitInEuro");
            fail("Invalid daylimit not recognized");
        } catch (ConstraintViolationException ex) {
            List<ConstraintViolation> errors = new ArrayList<>(ex.getConstraintViolations());
            errors.sort(Comparator.comparing(ConstraintViolation::getMessageTemplate));
            assertThat(errors.get(0).getMessageTemplate(), is("{payment_day_limit_max}"));
        }
        cut.setDayLimitInEuro(999);
        try {
            ValidationController.get(PaymentLimits.class).processBeanValidationForProperty(cut, "dayLimitInEuro");
        } catch (ConstraintViolationException ex) {
            fail("Correct daylimit not recognized");
        }
    }



}