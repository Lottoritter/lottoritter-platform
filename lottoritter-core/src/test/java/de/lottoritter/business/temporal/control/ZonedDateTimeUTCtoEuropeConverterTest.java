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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.Test;
import org.mongodb.morphia.mapping.MappedField;

import de.lottoritter.business.temporal.entity.ZonedDateTimeEurope;

/**
 * @author Ulrich Cech
 */
public class ZonedDateTimeUTCtoEuropeConverterTest {

    @Test
    public void testEncodingTheZonedDateToUTC() {
        ZonedDateTime testDateTime = ZonedDateTime.of(2017, 6, 20, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
        ZonedDateTimeUTCtoEuropeConverter cut = new ZonedDateTimeUTCtoEuropeConverter();
        final Object encode = cut.encode(testDateTime, null);
        assertThat(encode, instanceOf(Date.class));
        assertThat(((Date)encode).getTime(), is(testDateTime.toInstant().atZone(ZoneId.of("UTC")).toEpochSecond()*1000));
    }

    @Test
    public void testDecodeUTCtoZonedDateEurope() {
        ZonedDateTime expected = ZonedDateTime.of(2017, 6, 20, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
        ZonedDateTimeUTCtoEuropeConverter cut = new ZonedDateTimeUTCtoEuropeConverter();
        Field mockedField = mock(Field.class);
        when(mockedField.isAnnotationPresent(ZonedDateTimeEurope.class)).thenReturn(true);
        MappedField mockedMappedField = mock(MappedField.class);
        when(mockedMappedField.getField()).thenReturn(mockedField);
        Date date = new Date();
        date.setTime(1497909600000L);
        final Object decode = cut.decode(ZonedDateTime.class, date, mockedMappedField);
        assertThat(decode, instanceOf(ZonedDateTime.class));
        assertThat(decode, is(expected));
        assertThat(((ZonedDateTime)decode).getZone(), is(ZoneId.of("Europe/Paris")));
    }

}