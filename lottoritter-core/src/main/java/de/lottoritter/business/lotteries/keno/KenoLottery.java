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
package de.lottoritter.business.lotteries.keno;

import de.lottoritter.business.lotteries.DrawingType;
import de.lottoritter.business.lotteries.Jackpot;
import de.lottoritter.business.lotteries.Lottery;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.WinCalculator;
import de.lottoritter.business.lotteries.plus5.Plus5Lottery;
import de.lottoritter.business.lotteries.plus5.Plus5Ticket;

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
public class KenoLottery extends Lottery implements Serializable {

    private static final long serialVersionUID = 3871481812404598886L;

    public static final String IDENTIFIER = "keno";

    public static final int HIGHEST_SELECTABLE_NUMBER = 70;
    public static final int MAX_FIELDS_PER_TICKET = 5;
    public static final int MAX_SELECTABLE_NUMBERS = 10;
    public static final int MIN_SELECTABLE_NUMBERS = 2;


    @EJB
    KenoWinCalculator kenoWinCalculator;

    public KenoLottery() {
    }


    @Override
    public List<String> getPossibleAdditionalLotteries() {
        return Collections.singletonList(Plus5Lottery.IDENTIFIER);
    }

    @Override
    public Jackpot getCurrentJackpot() {
        return KenoJackpot.getCurrent(datastore);
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
        if (currentTime.isBefore(LocalTime.of(18,30))) {
            drawingDate = baseTimestamp.withHour(19).withMinute(10).withSecond(0).withNano(0);
        } else {
            drawingDate = baseTimestamp.plusDays(1).withHour(19).withMinute(10).withSecond(0).withNano(0);
        }
        return drawingDate;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Ticket createNewEmbeddedTicket(MainTicket parentTicket, String newAdditionalLottery) {
        if (newAdditionalLottery.equals(Plus5Lottery.IDENTIFIER)) {
            return new Plus5Ticket(parentTicket);
        }
        return null;
    }

    @Override
    public WinCalculator getWinCalculator() {
        return kenoWinCalculator;
    }

}
