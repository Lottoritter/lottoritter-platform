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
package de.lottoritter.business.lotteries.spiel77;

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
public class Spiel77Lottery extends Lottery implements Serializable {

    private static final long serialVersionUID = -5860900052061812200L;

    public static final String IDENTIFIER = "spiel77";

    public static final int HIGHEST_SELECTABLE_NUMBER = 9;
    public static final int MAX_FIELDS_PER_TICKET = 1;
    public static final int SELECTABLE_NUMBERS = 7;

    @EJB
    Spiel77WinCalculator spiel77WinCalculator;


    public Spiel77Lottery() {
    }


    @Override
    public List<String> getPossibleAdditionalLotteries() {
        return Collections.emptyList();
    }

    @Override
    public Jackpot getCurrentJackpot() {
        return null; //German6aus49Jackpot.getCurrent(datastore);
    }

    @Override
    public ZonedDateTime getNextClosingDate() {
        ZonedDateTime nextDrawing = getNextDrawing(null, null);
        final int dayOfWeek = nextDrawing.getDayOfWeek().getValue();
        if (dayOfWeek == 3) {
            nextDrawing = nextDrawing.withHour(17).withMinute(45);
        } else if (dayOfWeek == 6) {
            nextDrawing = nextDrawing.withHour(18).withMinute(45);
        }
        return nextDrawing;
    }

    @Override
    public ZonedDateTime getNextDrawing(final ZonedDateTime startDate, final DrawingType drawingType) {
        ZonedDateTime baseTimestamp = startDate;
        if (baseTimestamp == null) {
            baseTimestamp = getDateTimeNow();
        }
        LocalTime currentTime = baseTimestamp.toLocalTime();
        ZonedDateTime drawingDate;
        final int weekDay = baseTimestamp.getDayOfWeek().getValue();
        // current time is before drawing on wednesday => drawing for wednesday
        if ((weekDay < 3) || (weekDay == 3 && currentTime.isBefore(LocalTime.of(17,45)))) {
            drawingDate = baseTimestamp.plusDays(3 - weekDay).withHour(18).withMinute(0).withSecond(0).withNano(0);
            // current time is after drawing on saturday  => drawing for wednesday
        } else if ((weekDay == 6 && currentTime.isAfter(LocalTime.of(18,45))) || (weekDay > 6)) {
            drawingDate = baseTimestamp.plusDays(10 - weekDay).withHour(18).withMinute(0).withSecond(0).withNano(0);
            // drawing for saturday
        } else {
            drawingDate = baseTimestamp.plusDays(6 - weekDay).withHour(19).withMinute(0).withSecond(0).withNano(0);
        }
        return drawingDate;
    }


    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Ticket createNewEmbeddedTicket(MainTicket parentTicket, String newAdditionalLotteries) {
        throw new UnsupportedOperationException("Spiel77 cannot have embedded tickets");
    }

    @Override
    public WinCalculator getWinCalculator() {
        return spiel77WinCalculator;
    }

}
