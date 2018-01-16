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
import de.lottoritter.business.payment.entity.PaymentState;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author Ulrich Cech
 */
@Stateless
public class PaymentStatisticsController {

    @Inject
    Datastore datastore;


    public PaymentStatisticsController() {
    }


    public Integer[] getSumOfPaymentsForMonthOfCurrentYear(ZonedDateTime from, ZonedDateTime to) {
        final Query<Payment> query = getPaymentsForMonthOfCurrentYear(from, to);
        Integer[] monthsSum = new Integer[] {0,0,0,0,0,0,0,0,0,0,0,0};
        final List<Payment> payments = query.asList();
        for (Payment payment : payments) {
            monthsSum[payment.getPayedAt().getMonthValue() - 1] += payment.getAmountInCent();
        }
        return monthsSum;
    }

    public Integer[] getSumOfTicketsForMonthOfCurrentYear(ZonedDateTime from, ZonedDateTime to) {
        final Query<Payment> query = getPaymentsForMonthOfCurrentYear(from, to);
        Integer[] monthsSum = new Integer[] {0,0,0,0,0,0,0,0,0,0,0,0};
        final List<Payment> payments = query.asList();
        for (Payment payment : payments) {
            if (payment.getTicketList() != null) {
                monthsSum[payment.getPayedAt().getMonthValue() - 1] += payment.getTicketList().size();
            }
        }
        return monthsSum;
    }

    private Query<Payment> getPaymentsForMonthOfCurrentYear(ZonedDateTime from, ZonedDateTime to) {
        final Query<Payment> query = datastore.createQuery(Payment.class);
        query.and(
                query.criteria("state").equal(PaymentState.SUCCESS),
                query.criteria("payedAt").greaterThanOrEq(from),
                query.criteria("payedAt").lessThanOrEq(to)
        );
        return query;
    }


}
