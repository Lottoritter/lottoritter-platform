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
package de.lottoritter.business.lotteries.gluecksspirale;

import de.lottoritter.business.lotteries.DrawingType;
import de.lottoritter.business.lotteries.Jackpot;
import de.lottoritter.business.lotteries.Lottery;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.WinCalculator;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author Ulrich Cech
 */
@Singleton
@Named
public class GluecksspiraleLottery extends Lottery implements Serializable {

    private static final long serialVersionUID = 595716933865065733L;

    public static final String IDENTIFIER = "gluecksspirale";

    public static final int HIGHEST_SELECTABLE_NUMBER = 9;
    public static final int MAX_FIELDS_PER_TICKET = 1;
    public static final int SELECTABLE_NUMBERS = 7;

    @EJB
    GluecksspiraleWinCalculator gluecksspiraleWinCalculator;


    public GluecksspiraleLottery() {
    }


    @Override
    public List<String> getPossibleAdditionalLotteries() {
        return Collections.emptyList();
    }

    @Override
    public Jackpot getCurrentJackpot() {
        return null; // TODO: German6aus49Jackpot.getCurrent(datastore);
    }

    @Override
    public ZonedDateTime getNextClosingDate() {
        ZonedDateTime nextDrawing = getNextDrawing(null, null);
        nextDrawing = nextDrawing.withHour(18).withMinute(45);
        return nextDrawing;
    }

    @Override
    public ZonedDateTime getNextDrawing(final ZonedDateTime startDate, final DrawingType drawingType) {
        ZonedDateTime baseTimstamp = startDate;
        if (baseTimstamp == null) {
            baseTimstamp = getDateTimeNow();
        }
        LocalTime currentTime = baseTimstamp.toLocalTime();
        ZonedDateTime drawingDate;
        final int weekDay = baseTimstamp.getDayOfWeek().getValue();
        // current time is before drawing on friday => drawing for friday
        if ((weekDay < 6) || (weekDay == 6 && currentTime.isBefore(LocalTime.of(18,45)))) {
            drawingDate = baseTimstamp.plusDays(6 - weekDay).withHour(19).withMinute(0).withSecond(0).withNano(0);
            // current time is after drawing on friday => drawing for next friday
        } else {
            drawingDate = baseTimstamp.plusDays(6 + (7 - weekDay)).withHour(19).withMinute(0).withSecond(0).withNano(0);
        }
        return drawingDate;
    }


    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Ticket createNewEmbeddedTicket(MainTicket parentTicket, String newAdditionalLotteries) {
        throw new UnsupportedOperationException("Gluecksspirale cannot have embedded tickets");
    }

    @Override
    public WinCalculator getWinCalculator() {
        return gluecksspiraleWinCalculator;
    }

}
