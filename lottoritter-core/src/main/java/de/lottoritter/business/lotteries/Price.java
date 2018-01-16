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
package de.lottoritter.business.lotteries;

import java.io.Serializable;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Christopher Schmidt
 */
public class Price implements Serializable {

    private static final long serialVersionUID = 8600435736779619492L;

    private int amountInCent;

    private Currency currency;


    public Price() {
    }

    public Price(int amountInCent) {
        this(amountInCent, Currency.getInstance(Locale.GERMANY));
    }

    public Price(int amountInCent, Currency currency) {
        this.amountInCent = amountInCent;
        this.currency = currency;
    }


    public int getAmountInCent() {
        return amountInCent;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public String toString() {
        return amountInCent + " " + currency.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Price)) return false;
        Price price = (Price) o;
        return amountInCent == price.amountInCent &&
                Objects.equals(currency, price.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amountInCent, currency);
    }
}
