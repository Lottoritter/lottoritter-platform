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

import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotLottery;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Lottery;
import de.lottoritter.business.temporal.control.DateTimeService;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ulrich Cech
 */
public class LotteryTest {

    @Test
    public void getEndingDateForCancelledPermaTicket() {
        Lottery cut = new German6aus49Lottery();
        DateTimeService mockedDateTimeService = mock(DateTimeService.class);
        when(mockedDateTimeService.getDateTimeNowUTC()).thenReturn(ZonedDateTime.of(2017, 6, 22, 0, 0, 0 ,0, ZoneId.of("UTC")));
        cut.dateTimeService = mockedDateTimeService;
        MainTicket mockedTicket = mock(MainTicket.class);
        ZonedDateTime startingDate = ZonedDateTime.of(2017, 6, 7, 18, 0, 0 ,0, ZoneId.of("Europe/Paris"));
        when(mockedTicket.getDurationOrBillingPeriod()).thenReturn(2);
        when(mockedTicket.getStartingDate()).thenReturn(startingDate);
        when(mockedTicket.getDrawingType()).thenReturn("german6aus49wesa");
        when(mockedTicket.getDurationOrBillingPeriod()).thenReturn(2);
        ZonedDateTime expected = ZonedDateTime.of(2017, 7, 1, 19, 0, 0 ,0, ZoneId.of("Europe/Paris"));
        assertThat(cut.calculateEndingDateForCancelledPermaTicket(mockedTicket), CoreMatchers.is(expected));

        when(mockedTicket.getDrawingType()).thenReturn("german6aus49we");
        expected = ZonedDateTime.of(2017, 6, 28, 18, 0, 0 ,0, ZoneId.of("Europe/Paris"));
        assertThat(cut.calculateEndingDateForCancelledPermaTicket(mockedTicket), CoreMatchers.is(expected));

        when(mockedTicket.getDrawingType()).thenReturn("german6aus49sa");
        expected = ZonedDateTime.of(2017, 7, 1, 19, 0, 0 ,0, ZoneId.of("Europe/Paris"));
        assertThat(cut.calculateEndingDateForCancelledPermaTicket(mockedTicket), CoreMatchers.is(expected));

        startingDate = ZonedDateTime.of(2017, 6, 10, 19, 0, 0 ,0, ZoneId.of("Europe/Paris"));
        when(mockedTicket.getStartingDate()).thenReturn(startingDate);
        when(mockedTicket.getDrawingType()).thenReturn("german6aus49wesa");
        expected = ZonedDateTime.of(2017, 7, 5, 18, 0, 0 ,0, ZoneId.of("Europe/Paris"));
        assertThat(cut.calculateEndingDateForCancelledPermaTicket(mockedTicket), CoreMatchers.is(expected));

        when(mockedTicket.getDrawingType()).thenReturn("german6aus49sa");
        expected = ZonedDateTime.of(2017, 7, 1, 19, 0, 0 ,0, ZoneId.of("Europe/Paris"));
        assertThat(cut.calculateEndingDateForCancelledPermaTicket(mockedTicket), CoreMatchers.is(expected));


        cut = new EuroJackpotLottery();
        cut.dateTimeService = mockedDateTimeService;
        startingDate = ZonedDateTime.of(2017, 6, 9, 20, 0, 0 ,0, ZoneId.of("Europe/Paris"));
        when(mockedTicket.getStartingDate()).thenReturn(startingDate);
        when(mockedTicket.getDrawingType()).thenReturn(DrawingType.EUROJACKPOTFR.getTypeAsString());
        expected = ZonedDateTime.of(2017, 6, 30, 20, 0, 0 ,0, ZoneId.of("Europe/Paris"));
        assertThat(cut.calculateEndingDateForCancelledPermaTicket(mockedTicket), CoreMatchers.is(expected));
    }




    @Test
    public void getNextClosingDate() throws Exception {
    }

    @Test
    public void getNextDrawing() throws Exception {
    }

    @Test
    public void getPossibleAdditionalLotteries() throws Exception {
    }

    @Test
    public void getIdentifier() throws Exception {
    }

    @Test
    public void createNewEmbeddedTicket() throws Exception {
    }

    @Test
    public void getWinCalculator() throws Exception {
    }

    @Test
    public void setTicketStateAfterSubscriptionCancelled() throws Exception {
    }

    @Test
    public void getMaxNumberOfDrawingsPerWeek() throws Exception {
    }

    @Test
    public void getDateTimeNow() throws Exception {
    }

    @Test
    public void getDateTimeService() throws Exception {
    }

    @Test
    public void getCurrentJackpot() throws Exception {
    }

}