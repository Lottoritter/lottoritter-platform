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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;
import org.mockito.Mockito;

import de.lottoritter.business.temporal.control.DateTimeService;

/**
 * @author Ulrich Cech
 */
public class GluecksspiraleLotteryTest {

    @Test
    public void getNextDrawing() throws Exception {
        GluecksspiraleLottery gluecksspiraleLottery = getGluecksspiraleLottery();

        // one hour to next drawing
        ZonedDateTime currentDate = ZonedDateTime.of(2017, 4, 8, 18, 0,
                0, 0, ZoneId.of("Europe/Paris"));
        ZonedDateTime expected = ZonedDateTime.of(2017, 4, 8, 19, 0,
                0, 0, ZoneId.of("Europe/Paris"));
        assertThat(gluecksspiraleLottery.getNextDrawing(currentDate, null), is(expected));

        // exact on drawing time (saturday)
        currentDate = ZonedDateTime.of(2017, 4, 8, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        expected = ZonedDateTime.of(2017, 4, 15, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(gluecksspiraleLottery.getNextDrawing(currentDate, null), is(expected));

        // one day before drawing
        currentDate = ZonedDateTime.of(2017, 4, 7, 16, 0,0, 0, ZoneId.of("Europe/Paris"));
        expected = ZonedDateTime.of(2017, 4, 8, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(gluecksspiraleLottery.getNextDrawing(currentDate, null), is(expected));

        // one day after the drawing
        currentDate = ZonedDateTime.of(2017, 4, 9, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        expected = ZonedDateTime.of(2017, 4, 15, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(gluecksspiraleLottery.getNextDrawing(currentDate, null), is(expected));

        // two days after the drawing
        currentDate = ZonedDateTime.of(2017, 4, 10, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        expected = ZonedDateTime.of(2017, 4, 15, 19, 0,0, 0, ZoneId.of("Europe/Paris"));
        assertThat(gluecksspiraleLottery.getNextDrawing(currentDate, null), is(expected));
    }

    private GluecksspiraleLottery getGluecksspiraleLottery() {
        GluecksspiraleLottery lottery = new GluecksspiraleLottery();
        final GluecksspiraleLottery spy = Mockito.spy(lottery);
        Mockito.when(spy.getDateTimeService()).thenReturn(new DateTimeService());
        return spy;
    }

}