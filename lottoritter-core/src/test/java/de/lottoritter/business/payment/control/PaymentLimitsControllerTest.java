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
package de.lottoritter.business.payment.control;

import de.lottoritter.business.payment.entity.Payment;
import de.lottoritter.business.payment.entity.PaymentLimits;
import de.lottoritter.business.payment.entity.PaymentState;
import de.lottoritter.business.payment.entity.PspCode;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.security.control.EncryptionService;
import de.lottoritter.business.temporal.control.DateTimeService;
import de.lottoritter.platform.persistence.FongoDbPersistenceTest;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Iterator;

import static org.junit.Assert.assertThat;

/**
 * @author Ulrich Cech
 */
public class PaymentLimitsControllerTest extends FongoDbPersistenceTest {

    @Test
    public void testCheckMonthLimit() throws NoSuchFieldException, IllegalAccessException {
        PaymentLimitsController cut = new PaymentLimitsController();
        cut.dateTimeService = new DateTimeService();
        cut.datastore = getDatastore();
        final EncryptionService encryptionService = getEncryptedFieldConverter().getEncryptionService();
        final Field encryptionKeyField = encryptionService.getClass().getDeclaredField("encryptionKey");
        encryptionKeyField.setAccessible(true);
        encryptionKeyField.set(encryptionService, createEncryptionKey("01234567890123456789012345678912"));
        TestPlayer player = new TestPlayer();
        player.setEmail("test@example.com");
        final PaymentLimits paymentLimits = new PaymentLimits();
        paymentLimits.setWeekLimitInEuro(250);
        paymentLimits.setDayLimitInEuro(50);
        player.setPaymentLimits(paymentLimits);
        getDatastore().save(player);

        assertThat(cut.isDayLimitReached(player, 2000), CoreMatchers.is(false));
        assertThat(cut.isWeekLimitReached(player, 2000), CoreMatchers.is(false));
        assertThat(cut.isMonthLimitReached(player, 2000), CoreMatchers.is(false));

        Payment payment = new Payment(player, PspCode.NO_OP);
        payment.setState(PaymentState.SUCCESS);
        payment.setPayedAt(cut.dateTimeService.getDateTimeNowEurope());
        payment.setAmountInCent(100000);
        getDatastore().save(payment);

        assertThat(cut.isDayLimitReached(player, 2000), CoreMatchers.is(true));
        assertThat(cut.isWeekLimitReached(player, 2000), CoreMatchers.is(true));
        assertThat(cut.isMonthLimitReached(player, 2000), CoreMatchers.is(true));
    }


    static class TestPlayer extends Player {
        @Override
        public void validate() {
        }
    }

    private Instance<String> createEncryptionKey(String val) {
        return new Instance<String>() {
            @Override
            public Iterator<String> iterator() {
                return null;
            }

            @Override
            public Instance<String> select(Annotation... qualifiers) {
                return null;
            }

            @Override
            public boolean isUnsatisfied() {
                return false;
            }

            @Override
            public boolean isAmbiguous() {
                return false;
            }

            @Override
            public void destroy(String instance) {

            }

            @Override
            public <U extends String> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
                return null;
            }

            @Override
            public <U extends String> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
                return null;
            }

            @Override
            public String get() {
                return val;
            }
        };
    }
}