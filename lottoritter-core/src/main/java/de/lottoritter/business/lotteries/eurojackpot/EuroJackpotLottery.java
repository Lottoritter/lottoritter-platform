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
package de.lottoritter.business.lotteries.eurojackpot;

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
 * @author Christopher Schmidt
 */
@Singleton
@Named
public class EuroJackpotLottery extends Lottery implements Serializable {

    private static final long serialVersionUID = 859378722307086950L;

    public static final String IDENTIFIER = "euroJackpot";

    public static final int HIGHEST_SELECTABLE_NUMBER = 50;
    public static final int MAX_FIELDS_PER_TICKET = 12;
    public static final int SELECTABLE_NUMBERS = 5;
    public static final int HIGHEST_SELECTABLE_ADDITIONAL_NUMBER = 10;
    public static final int SELECTABLE_ADDITIONAL_NUMBERS = 2;


    @EJB
    EuroJackpotWinCalculator euroJackpotWinCalculator;

    public EuroJackpotLottery() {
    }


    @Override
    public List<String> getPossibleAdditionalLotteries() {
        return Collections.emptyList();
    }

    @Override
    public Jackpot getCurrentJackpot() {
        return EuroJackpotJackpot.getCurrent(datastore);
    }

    @Override
    public ZonedDateTime getNextClosingDate() {
        ZonedDateTime nextDrawing = getNextDrawing(null, null);
        nextDrawing = nextDrawing.withHour(18).withMinute(30);
        return nextDrawing;
    }

    @Override
    public ZonedDateTime getNextDrawing(final ZonedDateTime current, final DrawingType drawingType) {
        ZonedDateTime baseTimestamp = current;
        if (baseTimestamp == null) {
            baseTimestamp = getDateTimeNow();
        }
        LocalTime currentTime = baseTimestamp.toLocalTime();
        ZonedDateTime drawingDate;
        final int weekDay = baseTimestamp.getDayOfWeek().getValue();
        // current time is before drawing on friday => drawing for friday
        if ((weekDay < 5) || (weekDay == 5 && currentTime.isBefore(LocalTime.of(18,30)))) {
            drawingDate = baseTimestamp.plusDays(5 - weekDay).withHour(20).withMinute(0).withSecond(0).withNano(0);
        // current time is after drawing on friday => drawing for next friday
        } else {
            drawingDate = baseTimestamp.plusDays(5 + (7 - weekDay)).withHour(20).withMinute(0).withSecond(0).withNano(0);
        }
        return drawingDate;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Ticket createNewEmbeddedTicket(MainTicket parentTicket, String newAdditionalLotteries) {
        return null;
    }

    @Override
    public WinCalculator getWinCalculator() {
        return euroJackpotWinCalculator;
    }

}
