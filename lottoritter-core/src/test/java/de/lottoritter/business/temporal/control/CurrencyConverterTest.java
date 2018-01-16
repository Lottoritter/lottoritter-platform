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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import java.util.Currency;
import java.util.Locale;

import org.junit.Test;

import com.mongodb.BasicDBObject;

/**
 * @author Ulrich Cech
 */
public class CurrencyConverterTest {

    @Test
    public void testEncode() {
        CurrencyConverter cut = new CurrencyConverter();
        final Object encode = cut.encode(Currency.getInstance(Locale.GERMANY));
        assertThat(encode.toString(), is("EUR"));
    }

    @Test
    public void testDecode() {
        CurrencyConverter cut = new CurrencyConverter();
        final Object decode = cut.decode(Currency.class, "EUR");
        assertThat(decode instanceof Currency, is(true));
        assertThat(((Currency) decode).getCurrencyCode(), is("EUR"));
    }

}