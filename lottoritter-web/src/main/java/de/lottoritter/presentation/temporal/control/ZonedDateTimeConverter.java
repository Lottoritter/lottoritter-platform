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
package de.lottoritter.presentation.temporal.control;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
public class ZonedDateTimeConverter implements Converter {

    private static final Logger logger = Logger.getLogger(ZonedDateTimeConverter.class.getName());

    private static final String DEFAULT_PATTERN = "dd.MM.yyyy HH:mm";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_PATTERN, Locale.GERMANY);

    public ZonedDateTimeConverter() {
        dateFormat.setLenient(false);
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        ZonedDateTime retValue = null;
        if (value != null) {
            try {
                dateFormat.applyPattern(getPattern(component));
                retValue = dateFormat.parse(value).toInstant().atZone(ZoneId.of("Europe/Paris"));
            } catch (ParseException ex) {
                logger.log(Level.SEVERE, "Error while converting the ZomedDateTime", ex);
                retValue = null;
            }
        }
        return retValue;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String retValue = "";
        if ((value != null) && (value instanceof ZonedDateTime)) {
            retValue = ((ZonedDateTime) value).toInstant().atZone(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern(getPattern(component), Locale.GERMANY));
        } else {
            retValue = "";
        }
        return retValue;
    }

    private String getPattern(UIComponent component) {
        String pattern = (String) component.getAttributes().get("pattern");
        if (pattern == null) {
            pattern = DEFAULT_PATTERN;
        }
        return pattern;
    }
}
