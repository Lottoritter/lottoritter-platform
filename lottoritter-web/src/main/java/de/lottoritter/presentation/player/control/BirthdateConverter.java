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
package de.lottoritter.presentation.player.control;

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
public class BirthdateConverter implements Converter {

    private static final Logger logger = Logger.getLogger(BirthdateConverter.class.getName());

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);


    public BirthdateConverter() {
        dateFormat.setLenient(false);
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        ZonedDateTime retValue = null;
        if (value != null) {
            try {
                retValue = dateFormat.parse(value).toInstant().atZone(ZoneId.of("Europe/Paris")).withHour(12);
            } catch (ParseException ex) {
                logger.log(Level.SEVERE, "Error while parsing the birthdate.", ex);
                retValue = null;
            }
        }
        return retValue;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        String retValue = "";
        if ((value != null) && (value instanceof ZonedDateTime)) {
            retValue = ((ZonedDateTime) value).format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY));
        } else {
            retValue = "";
        }
        return retValue;
    }
}
