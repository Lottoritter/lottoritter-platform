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

import java.util.Currency;

import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

/**
 * @author Ulrich Cech
 */
public class CurrencyConverter extends TypeConverter implements SimpleValueConverter {

    @Override
    public Object decode(Class<?> aClass, Object o, MappedField mappedField) {
        if (o == null) {
            return null;
        }
        String currencyCode = o.toString();
        return Currency.getInstance(currencyCode);
    }

    @Override
    public Object encode(Object value, MappedField optionalExtraInfo) {
        if (value == null) {
            return null;
        }
        return ((Currency) value).getCurrencyCode();
    }

    @Override
    protected boolean isSupported(final Class c, final MappedField optionalExtraInfo) {
        return "Currency".equals(c.getSimpleName());
    }
}
