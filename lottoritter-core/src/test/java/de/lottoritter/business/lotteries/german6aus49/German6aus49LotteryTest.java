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
import de.lottoritter.business.temporal.control.DateTimeService;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Ulrich Cech
 */
public class German6aus49LotteryTest {

    @Test
    public void testGetNextDrawingDate() {
        German6aus49Lottery german6aus49 = getGerman6aus49Lottery();

        // one hour to next drawing
        ZonedDateTime currentDate = ZonedDateTime.of(2017, 4, 5, 17, 0,
                0, 0, ZoneId.of("Europe/Paris"));
        ZonedDateTime expected = ZonedDateTime.of(2017, 4, 5, 18, 0,
                0, 0, ZoneId.of("Europe/Paris"));
        assertThat(german6aus49.getNextDrawing(currentDate, null), is(expected));

        // exact on drawing time (wednesday)
        currentDate = ZonedDateTime.of(2017, 4, 5, 18, 0,0, 0, ZoneId.of("Europe/Paris"));
        expected = ZonedDateTime.of(2017, 4, 8, 19, 0,
                0, 0, ZoneId.of("Europe/Paris"));
        assertThat(german6aus49.getNextDrawing(currentDate, null), is(expected));

        // one day before drawing
        currentDate = ZonedDateTime.of(2017, 4, 4, 16, 0,0, 0, ZoneId.of("Europe/Paris"));
        expected = ZonedDateTime.of(2017, 4, 5, 18, 0,
                0, 0, ZoneId.of("Europe/Paris"));
        assertThat(german6aus49.getNextDrawing(currentDate, null), is(expected));

        // one day after the drawing
        currentDate = ZonedDateTime.of(2017, 4, 9, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        expected = ZonedDateTime.of(2017, 4, 12, 18, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(german6aus49.getNextDrawing(currentDate, null), is(expected));

        // exact on drawing time (saturday)
        currentDate = ZonedDateTime.of(2017, 4, 12, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        expected = ZonedDateTime.of(2017, 4, 15, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(german6aus49.getNextDrawing(currentDate, null), is(expected));

        // one day after drawing
        currentDate = ZonedDateTime.of(2017, 4, 9, 16, 0,0, 0, ZoneId.of("Europe/Paris"));
        expected = ZonedDateTime.of(2017, 4, 12, 18, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(german6aus49.getNextDrawing(currentDate, null), is(expected));
    }

    private German6aus49Lottery getGerman6aus49Lottery() {
        German6aus49Lottery lottery = new German6aus49Lottery();
        final German6aus49Lottery spy = Mockito.spy(lottery);
        Mockito.when(spy.getDateTimeService()).thenReturn(new DateTimeService());
        return spy;
    }

    @Test
    public void testGetNextDrawingFor() {
        German6aus49Lottery spyGerman6aus49 = getGerman6aus49Lottery();
        Mockito.doReturn(ZonedDateTime.of(2017, 4, 11, 19, 0,0, 0, ZoneId.of("Europe/Paris"))).when(spyGerman6aus49).getDateTimeNow();
        ZonedDateTime expected = ZonedDateTime.of(2017, 4, 12, 18, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(spyGerman6aus49.getNextDrawing(null, DrawingType.GERMAN6AUS49WE), is(expected));

        Mockito.doReturn(ZonedDateTime.of(2017, 4, 12, 19, 0,0, 0, ZoneId.of("Europe/Paris"))).when(spyGerman6aus49).getDateTimeNow();
        expected = ZonedDateTime.of(2017, 4, 19, 18, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(spyGerman6aus49.getNextDrawing(null, DrawingType.GERMAN6AUS49WE), is(expected));

        Mockito.doReturn(ZonedDateTime.of(2017, 4, 12, 19, 0,0, 0, ZoneId.of("Europe/Paris"))).when(spyGerman6aus49).getDateTimeNow();
        expected = ZonedDateTime.of(2017, 4, 15, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(spyGerman6aus49.getNextDrawing(null, DrawingType.GERMAN6AUS49SA), is(expected));

        Mockito.doReturn(ZonedDateTime.of(2017, 4, 15, 19, 0,0, 0, ZoneId.of("Europe/Paris"))).when(spyGerman6aus49).getDateTimeNow();
        expected = ZonedDateTime.of(2017, 4, 22, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(spyGerman6aus49.getNextDrawing(null, DrawingType.GERMAN6AUS49SA), is(expected));
    }

}