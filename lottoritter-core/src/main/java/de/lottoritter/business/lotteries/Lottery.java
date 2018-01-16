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
package de.lottoritter.business.lotteries;

import de.lottoritter.business.temporal.control.DateTimeService;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author Ulrich Cech
 */
public abstract class Lottery {

    @Inject
    protected Datastore datastore;

    @Inject
    DateTimeService dateTimeService;

    public abstract ZonedDateTime getNextClosingDate();

    public abstract ZonedDateTime getNextDrawing(ZonedDateTime current, DrawingType drawingType);

    public abstract List<String> getPossibleAdditionalLotteries();

    public abstract String getIdentifier();

    public abstract Ticket createNewEmbeddedTicket(MainTicket parentTicket, String newAdditionalLotteries);

    public abstract WinCalculator getWinCalculator();


    public void setTicketStateAfterSubscriptionCancelled(MainTicket mainTicket) {
        ZonedDateTime lastDrawing = calculateEndingDateForCancelledPermaTicket(mainTicket);
        mainTicket.setPermaTicket(false);
        mainTicket.setEndingDate(lastDrawing);
        datastore.update(
                datastore.createQuery(MainTicket.class).field("_id").equal(mainTicket.getId()),
                datastore.createUpdateOperations(MainTicket.class)
                        .set("endingDate", lastDrawing).set("permaTicket", false)
        );
    }

    /**
     * Calculates the ending date of the ticket after a subscription is cancelled.<br/>
     * From startingDate, calculate the endingDate for the period and check, if the lastDrawingDate is before
     * the currentDate. If yes, go one drawing further (which is the startingDate of the next period) and again,
     * calculate the endingDate of this period... and so on.
     * Example: <br/>
     * Ticket-startingDate=07.06.2017 (2 week billing-period) with WESA as drawing-type<br/>
     * currentDate for cancelling subscription: 26.06.2017<br/>
     * So the period is added until 05.07.2017 (that is in the future (21.06.2017 was the billing date for the next
     * 2 weeks)), then we go one week and one daqy back (27.05.2017) and because of the WESA, we have to call to times the
     * "nextDrawing"-method and end up on 05.07.2017 18:00 Paris/Europe.<br/>
     */
    ZonedDateTime calculateEndingDateForCancelledPermaTicket(MainTicket mainTicket) {
        final DrawingType drawingType = DrawingType.fromType(mainTicket.getDrawingType());
        ZonedDateTime lastDrawingDateOfPayedPeriod = mainTicket.getStartingDate();
        final ZonedDateTime currentUTC = dateTimeService.getDateTimeNowUTC();

        int loopCounter = 0;
        do {
            if (loopCounter > 0) {
                lastDrawingDateOfPayedPeriod = getNextDrawing(lastDrawingDateOfPayedPeriod, drawingType);
            }
            ++loopCounter;
            lastDrawingDateOfPayedPeriod = lastDrawingDateOfPayedPeriod.plusWeeks(mainTicket.getDurationOrBillingPeriod());
            lastDrawingDateOfPayedPeriod = lastDrawingDateOfPayedPeriod.minusWeeks(1).minusDays(1);
            for (int i = 0; i < getMaxNumberOfDrawingsPerWeek(drawingType); i++) {
                lastDrawingDateOfPayedPeriod = getNextDrawing(lastDrawingDateOfPayedPeriod, drawingType);
            }
        } while (lastDrawingDateOfPayedPeriod.isBefore(currentUTC));
        return lastDrawingDateOfPayedPeriod;
    }

    protected int getMaxNumberOfDrawingsPerWeek(DrawingType drawingType) {
        return 1;
    }

    public ZonedDateTime getDateTimeNow() {
        return getDateTimeService().getDateTimeNowEurope();
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public abstract Jackpot getCurrentJackpot();

}
