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
package de.lottoritter.business.payment.boundary;

import de.lottoritter.business.lotteries.Price;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * @author Christopher Schmidt
 */
@ApplicationScoped
public class PriceFormatter {

    public String getPriceFormatted(Price price) {
        BigDecimal sum = new BigDecimal("0.00");
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
        nf.setMinimumFractionDigits(2);
        DecimalFormat df = (DecimalFormat) nf;
        if ((price != null) && (price.getAmountInCent() > 0)) {
            sum = new BigDecimal(BigInteger.valueOf(price.getAmountInCent())).divide(new BigDecimal("100.00"), 2,
                    BigDecimal.ROUND_CEILING);
            return df.format(sum) + " " + price.getCurrency().getCurrencyCode();
        } else {
            return df.format(sum) + " " + Currency.getInstance(Locale.GERMANY);
        }
    }
}
