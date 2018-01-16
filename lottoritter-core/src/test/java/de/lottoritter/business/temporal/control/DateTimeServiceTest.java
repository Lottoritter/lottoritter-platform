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
package de.lottoritter.business.temporal.control;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Ulrich Cech
 */
public class DateTimeServiceTest {

    private static final long ACCEPTABLE_TIME_IN_MILLIS_DIFF = 1000L;

    @Test
    public void getFutureDate() {
        DateTimeService cut = new DateTimeService();
        final ZonedDateTime currentTimestamp = ZonedDateTime.of(2017, 2, 18, 0, 0, 0, 0, ZoneId.of("UTC"));
        final ZonedDateTime expectedNextDay = ZonedDateTime.of(2017, 2, 19, 0, 0, 0, 0, ZoneId.of("UTC"));
        final ZonedDateTime expectedEndOfNextDay = ZonedDateTime.of(2017, 2, 19, 23, 59, 59, 999, ZoneId.of("UTC"));
        assertThat(cut.getFutureDate(currentTimestamp, 1, false), is(expectedNextDay));
        assertThat(cut.getFutureDate(currentTimestamp, 1, true), is(expectedEndOfNextDay));
    }

    @Test
    public void getDateTimeNowEurope() {
        DateTimeService cut = new DateTimeService();
        long currentTime = System.currentTimeMillis();
        final ZonedDateTime dateTimeNowEurope = cut.getDateTimeNowEurope();
        assertThat(dateTimeNowEurope.getZone(), is(ZoneId.of("Europe/Paris")));
        assertThat(currentTime - dateTimeNowEurope.toEpochSecond() * 1000, lessThan(ACCEPTABLE_TIME_IN_MILLIS_DIFF));
    }

    @Test
    public void getDateTimeNowUTC() {
        DateTimeService cut = new DateTimeService();
        final ZonedDateTime dateTimeNowUTC = cut.getDateTimeNowUTC();
        long currentTime = System.currentTimeMillis();
        assertThat(dateTimeNowUTC.getZone(), is(ZoneId.of("UTC")));
        assertThat(currentTime - dateTimeNowUTC.toEpochSecond() * 1000, lessThan(ACCEPTABLE_TIME_IN_MILLIS_DIFF));
    }

    @Test
    public void getBeginningTimestampOfMonth() throws Exception {
        DateTimeService cut = new DateTimeService();
        ZonedDateTime expected = ZonedDateTime.now(ZoneId.of("UTC")).withYear(2017).withMonth(4).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime current = ZonedDateTime.of(2017, 4, 8, 12, 23, 45, 800, ZoneId.of("UTC"));
        assertThat(cut.getBeginningTimestampOfMonthUTC(current), is(expected));
    }

    @Test
    public void getEndingTimestampOfMonth() throws Exception {
        DateTimeService cut = new DateTimeService();
        ZonedDateTime expected = ZonedDateTime.now(ZoneId.of("UTC")).withYear(2017).withMonth(4).withDayOfMonth(30).withHour(23).withMinute(59).withSecond(59).withNano(0);
        ZonedDateTime current = ZonedDateTime.of(2017, 4, 8, 12, 23, 45, 800, ZoneId.of("UTC"));
        assertThat(cut.getEndingTimestampOfMonthUTC(current), is(expected));
    }

    @Test
    public void getBeginningTimestampOfMonthUTC() throws Exception {
        DateTimeService cut = new DateTimeService();
        ZonedDateTime expected = ZonedDateTime.now(ZoneId.of("UTC")).withYear(2017).withMonth(6).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime current = ZonedDateTime.of(2017, 6, 4, 12, 23, 45, 800, ZoneId.of("UTC"));
        final ZonedDateTime beginningTimestampOfMonthUTC = cut.getBeginningTimestampOfMonthUTC(current);
        assertThat(beginningTimestampOfMonthUTC, is(expected));
        assertThat(beginningTimestampOfMonthUTC.getZone(), is(ZoneId.of("UTC")));
    }

    @Test
    public void getEndingTimestampOfMonthUTC() throws Exception {
        DateTimeService cut = new DateTimeService();
        ZonedDateTime expected = ZonedDateTime.now(ZoneId.of("UTC")).withYear(2017).withMonth(5).withDayOfMonth(31).withHour(23).withMinute(59).withSecond(59).withNano(0);
        ZonedDateTime current = ZonedDateTime.of(2017, 5, 29, 12, 23, 45, 800, ZoneId.of("UTC"));
        assertThat(cut.getEndingTimestampOfMonthUTC(current), is(expected));
    }

    @Test
    public void getBeginningTimestampOfWeekUTC() throws Exception {
        DateTimeService cut = new DateTimeService();
        ZonedDateTime expected = ZonedDateTime.now(ZoneId.of("UTC")).withYear(2017).withMonth(6).withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime current = ZonedDateTime.of(2017, 6, 14, 12, 23, 45, 800, ZoneId.of("UTC"));
        ZonedDateTime beginningTimestampOfWeekUTC = cut.getBeginningTimestampOfWeekUTC(current);
        assertThat(beginningTimestampOfWeekUTC, is(expected));
        assertThat(beginningTimestampOfWeekUTC.getZone(), is(ZoneId.of("UTC")));

        expected = ZonedDateTime.now(ZoneId.of("UTC")).withYear(2017).withMonth(5).withDayOfMonth(29).withHour(0).withMinute(0).withSecond(0).withNano(0);
        current = ZonedDateTime.of(2017, 6, 1, 12, 23, 45, 800, ZoneId.of("UTC"));
        beginningTimestampOfWeekUTC = cut.getBeginningTimestampOfWeekUTC(current);
        assertThat(beginningTimestampOfWeekUTC, is(expected));
        assertThat(beginningTimestampOfWeekUTC.getZone(), is(ZoneId.of("UTC")));
    }

    @Test
    public void getEndingTimestampOfWeekUTC() throws Exception {
        DateTimeService cut = new DateTimeService();
        ZonedDateTime expected = ZonedDateTime.now(ZoneId.of("UTC")).withYear(2017).withMonth(6).withDayOfMonth(18).withHour(23).withMinute(59).withSecond(59).withNano(0);
        ZonedDateTime current = ZonedDateTime.of(2017, 6, 12, 12, 23, 45, 800, ZoneId.of("UTC"));
        ZonedDateTime endingTimestampOfWeekUTC = cut.getEndingTimestampOfWeekUTC(current);
        assertThat(endingTimestampOfWeekUTC, is(expected));
        assertThat(endingTimestampOfWeekUTC.getZone(), is(ZoneId.of("UTC")));

        expected = ZonedDateTime.now(ZoneId.of("UTC")).withYear(2017).withMonth(7).withDayOfMonth(2).withHour(23).withMinute(59).withSecond(59).withNano(0);
        current = ZonedDateTime.of(2017, 6, 28, 12, 23, 45, 800, ZoneId.of("UTC"));
        endingTimestampOfWeekUTC = cut.getEndingTimestampOfWeekUTC(current);
        assertThat(endingTimestampOfWeekUTC, is(expected));
        assertThat(endingTimestampOfWeekUTC.getZone(), is(ZoneId.of("UTC")));
    }

    @Test
    public void getBeginningTimestampOfDay() throws Exception {
        DateTimeService cut = new DateTimeService();
        ZonedDateTime expected = ZonedDateTime.now(ZoneId.of("UTC")).withYear(2017).withMonth(6).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime current = ZonedDateTime.of(2017, 6, 1, 12, 23, 45, 800, ZoneId.of("UTC"));
        assertThat(cut.getBeginningTimestampOfDayUTC(current), is(expected));
    }

    @Test
    public void getEndingTimestampOfDay() throws Exception {
        DateTimeService cut = new DateTimeService();
        ZonedDateTime expected = ZonedDateTime.now(ZoneId.of("UTC")).withYear(2017).withMonth(6).withDayOfMonth(1).withHour(23).withMinute(59).withSecond(59).withNano(0);
        ZonedDateTime current = ZonedDateTime.of(2017, 6, 1, 12, 23, 45, 800, ZoneId.of("UTC"));
        assertThat(cut.getEndingTimestampOfDayUTC(current), is(expected));
    }

    @Test
    public void getBeginningOfCurrentYear() {
        DateTimeService cut = new DateTimeService();
        final DateTimeService spyCut = Mockito.spy(cut);
        final ZonedDateTime now = cut.getDateTimeNowEurope();
        ZonedDateTime current = ZonedDateTime.of(now.getYear(), 6, 1, 12, 23, 45, 800, ZoneId.of("UTC"));
        ZonedDateTime expected = ZonedDateTime.of(now.getYear(), 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        when(spyCut.getDateTimeNowUTC()).thenReturn(current);
        assertThat(cut.getBeginningOfCurrentYear(), is(expected));
    }

    @Test
    public void getEndingOfCurrentYear() {
        DateTimeService cut = new DateTimeService();
        final DateTimeService spyCut = Mockito.spy(cut);
        final ZonedDateTime now = cut.getDateTimeNowEurope();
        ZonedDateTime current = ZonedDateTime.of(now.getYear(), 6, 1, 12, 23, 45, 800, ZoneId.of("UTC"));
        ZonedDateTime expected = ZonedDateTime.of(now.getYear(), 12, 31, 23, 59, 59, 0, ZoneId.of("UTC"));
        when(spyCut.getDateTimeNowUTC()).thenReturn(current);
        assertThat(cut.getEndingOfCurrentYear(), is(expected));
    }

    @Test
    public void testConvertDateToZonedDateTimeEurope() {
        DateTimeService cut = new DateTimeService();
        ZonedDateTime utc = ZonedDateTime.of(2017, 6, 19, 22, 0, 0, 0, ZoneId.of("UTC"));
        Date date = new Date(utc.toEpochSecond() * 1000L);
        ZonedDateTime expected = ZonedDateTime.of(2017, 6, 20, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
        assertThat(cut.convertDateToZonedDateTimeEurope(date), is(expected));
    }

    @Test
    public void getMonthNameFor() {
        DateTimeService cut = new DateTimeService();
        ZonedDateTime current = ZonedDateTime.of(2017, 6, 20, 12, 23, 45, 800, ZoneId.of("UTC"));
        assertThat(cut.getMonthNameFor(current), is("Juni"));
        current = ZonedDateTime.of(2017, 1, 20, 12, 23, 45, 800, ZoneId.of("UTC"));
        assertThat(cut.getMonthNameFor(current), is("Januar"));
    }

}