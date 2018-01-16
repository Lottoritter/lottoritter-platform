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
package de.lottoritter.business.lotteries.german6aus49;

import de.lottoritter.business.lotteries.DrawingType;
import de.lottoritter.business.lotteries.Jackpot;
import de.lottoritter.business.lotteries.Lottery;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.WinCalculator;
import de.lottoritter.business.lotteries.gluecksspirale.GluecksspiraleLottery;
import de.lottoritter.business.lotteries.gluecksspirale.GluecksspiraleTicket;
import de.lottoritter.business.lotteries.spiel77.Spiel77Lottery;
import de.lottoritter.business.lotteries.spiel77.Spiel77Ticket;
import de.lottoritter.business.lotteries.super6.Super6Lottery;
import de.lottoritter.business.lotteries.super6.Super6Ticket;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static de.lottoritter.business.lotteries.DrawingType.GERMAN6AUS49SA;
import static de.lottoritter.business.lotteries.DrawingType.GERMAN6AUS49WE;
import static de.lottoritter.business.lotteries.DrawingType.GERMAN6AUS49WESA;

/**
 * @author Ulrich Cech
 */
@Singleton
@Named
public class German6aus49Lottery extends Lottery implements Serializable {

    private static final long serialVersionUID = -8986492315479419330L;

    public static final String IDENTIFIER = "german6aus49";

    public static final int HIGHEST_SELECTABLE_NUMBER = 49;
    public static final int MAX_FIELDS_PER_TICKET = 12;
    public static final int SELECTABLE_NUMBERS = 6;

    @EJB
    German6aus49WinCalculator german6aus49WinCalculator;


    public German6aus49Lottery() {
    }


    @Override
    public List<String> getPossibleAdditionalLotteries() {
        return Arrays.asList(Super6Lottery.IDENTIFIER, Spiel77Lottery.IDENTIFIER, GluecksspiraleLottery.IDENTIFIER);
    }

    @Override
    public Jackpot getCurrentJackpot() {
        return German6aus49Jackpot.getCurrent(datastore);
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
        if (drawingType != null) {
            if (drawingType == GERMAN6AUS49WESA) {
                return drawingDate;
            }
            if (drawingType == GERMAN6AUS49WE) {
                if (drawingDate.getDayOfWeek().getValue() == 3) {
                    return drawingDate;
                } else {
                    return getNextDrawing(drawingDate, drawingType);
                }
            }
            if (drawingType == GERMAN6AUS49SA) {
                if (drawingDate.getDayOfWeek().getValue() == 6) {
                    return drawingDate;
                } else {
                    return getNextDrawing(drawingDate, drawingType);
                }
            }
        } else {
            return drawingDate;
        }
        return null;
    }

    @Override
    public int getMaxNumberOfDrawingsPerWeek(DrawingType drawingType) {
        return (drawingType == DrawingType.GERMAN6AUS49WESA) ? 2 : 1;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Ticket createNewEmbeddedTicket(MainTicket mainTicket, String newAdditionalLottery) {
        if (newAdditionalLottery.equals(Super6Lottery.IDENTIFIER)) {
            return new Super6Ticket(mainTicket);
        }
        if (newAdditionalLottery.equals(Spiel77Lottery.IDENTIFIER)) {
            return new Spiel77Ticket(mainTicket);
        }
        if (newAdditionalLottery.equals(GluecksspiraleLottery.IDENTIFIER)) {
            return new GluecksspiraleTicket(mainTicket);
        }
        return null;
    }

    @Override
    public WinCalculator getWinCalculator() {
        return german6aus49WinCalculator;
    }

}
