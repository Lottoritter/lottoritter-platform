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

import de.lottoritter.business.temporal.entity.ZonedDateTimeEurope;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author Christopher Schmidt
 */
public class ZonedDateTimeUTCtoEuropeConverter extends TypeConverter implements SimpleValueConverter {


    public ZonedDateTimeUTCtoEuropeConverter() {
        super(ZonedDateTime.class);
    }


    /**
     * Returns a date for UTC.
     */
    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null || !(value instanceof ZonedDateTime)) {
            return null;
        }
        ZonedDateTime zonedDateTimeEurope = (ZonedDateTime) value;
        Date date = new Date();
        date.setTime(zonedDateTimeEurope.toInstant().toEpochMilli());
        return date;
    }

    @Override
    public Object decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (fromDBObject == null || !(fromDBObject instanceof Date)) {
            return null;
        }
        Date date = (Date) fromDBObject;
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.of("UTC"));
        if (optionalExtraInfo != null && optionalExtraInfo.getField().isAnnotationPresent(ZonedDateTimeEurope.class)) {
            zonedDateTime = zonedDateTime.toInstant().atZone(ZoneId.of("Europe/Paris"));
        }
        return zonedDateTime;
    }

}
