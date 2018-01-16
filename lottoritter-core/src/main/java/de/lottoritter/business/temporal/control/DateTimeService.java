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

import javax.enterprise.context.ApplicationScoped;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

import static java.time.temporal.ChronoField.MILLI_OF_SECOND;

/**
 * Helper methods for various date manipulation.<br/>
 * NO static utility methods here, mainly because of testing/mocking.
 *
 * @author Ulrich Cech
 */
@ApplicationScoped
public class DateTimeService {

    private static final int MAX_HOUR = 23;
    private static final int MAX_MINUTE = 59;
    private static final int MAX_SECOND = 59;
    private static final int MAX_MILLISECOND = 999;



    public ZonedDateTime getFutureDate(ZonedDateTime timestamp, int numberOfDays, boolean endOfDay) {
        ZonedDateTime futureDateTime = timestamp.plusDays(numberOfDays);
        if (endOfDay) {
            futureDateTime = futureDateTime.withHour(MAX_HOUR).withMinute(MAX_MINUTE).withSecond(MAX_SECOND)
                    .with(MILLI_OF_SECOND, 999).withNano(MAX_MILLISECOND);
        }
        return futureDateTime;
    }

    public ZonedDateTime getDateTimeNowEurope() {
        return ZonedDateTime.now(ZoneId.of("Europe/Paris"));
    }

    public ZonedDateTime getDateTimeNowUTC() {
        return ZonedDateTime.now(ZoneId.of("UTC")); // ZonedDateTime.now(ZoneOffset.UTC)
    }

    public ZonedDateTime getBeginningTimestampOfMonthUTC(final ZonedDateTime zonedDateTime) {
        ZonedDateTime zonedDateTimeToConvert = zonedDateTime;
        zonedDateTimeToConvert = zonedDateTimeToConvert.toInstant().atZone(ZoneId.of("UTC"));
        zonedDateTimeToConvert = zonedDateTimeToConvert.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return zonedDateTimeToConvert;
    }

    public ZonedDateTime getEndingTimestampOfMonthUTC(ZonedDateTime zonedDateTime) {
        ZonedDateTime zonedDateTimeToConvert = zonedDateTime;
        zonedDateTimeToConvert = zonedDateTimeToConvert.toInstant().atZone(ZoneId.of("UTC"));
        zonedDateTimeToConvert = zonedDateTimeToConvert.plus(1, ChronoUnit.MONTHS);
        zonedDateTimeToConvert = zonedDateTimeToConvert.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        zonedDateTimeToConvert = zonedDateTimeToConvert.minus(1, ChronoUnit.SECONDS);
        return zonedDateTimeToConvert;
    }

    public ZonedDateTime getBeginningTimestampOfWeekUTC(ZonedDateTime zonedDateTime) {
        ZonedDateTime zonedDateTimeToConvert = zonedDateTime;
        zonedDateTimeToConvert = zonedDateTimeToConvert.toInstant().atZone(ZoneId.of("UTC"));
        int minusDays = zonedDateTimeToConvert.getDayOfWeek().getValue() - 1;
        zonedDateTimeToConvert = zonedDateTimeToConvert.minusDays(minusDays);
        zonedDateTimeToConvert = zonedDateTimeToConvert.withHour(0).withMinute(0).withSecond(0).withNano(0);
        return zonedDateTimeToConvert;
    }

    public ZonedDateTime getEndingTimestampOfWeekUTC(ZonedDateTime zonedDateTime) {
        ZonedDateTime zonedDateTimeToConvert = zonedDateTime;
        zonedDateTimeToConvert = zonedDateTimeToConvert.toInstant().atZone(ZoneId.of("UTC"));
        int plusDays = 8 - zonedDateTimeToConvert.getDayOfWeek().getValue();
        zonedDateTimeToConvert = zonedDateTimeToConvert.plusDays(plusDays);
        zonedDateTimeToConvert = zonedDateTimeToConvert.withHour(0).withMinute(0).withSecond(0).withNano(0);
        zonedDateTimeToConvert = zonedDateTimeToConvert.minus(1, ChronoUnit.SECONDS);
        return zonedDateTimeToConvert;
    }

    public ZonedDateTime getBeginningTimestampOfDayUTC(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().atZone(ZoneId.of("UTC")).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public ZonedDateTime getEndingTimestampOfDayUTC(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant()
                .atZone(ZoneId.of("UTC"))
                .plusDays(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0)
                .minus(1, ChronoUnit.SECONDS);
    }

    public ZonedDateTime convertDateToZonedDateTimeEurope(Date date) {
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Paris"));
    }

    public ZonedDateTime getBeginningOfCurrentYear() {
        return getDateTimeNowUTC().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public ZonedDateTime getEndingOfCurrentYear() {
        ZonedDateTime dateTimeNowUTC = getDateTimeNowUTC();
        dateTimeNowUTC = dateTimeNowUTC.withYear(dateTimeNowUTC.getYear() + 1).withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return dateTimeNowUTC.minusSeconds(1);
    }

    public String getMonthNameFor(ZonedDateTime dateTime) {
        return dateTime.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMANY);
    }
}
