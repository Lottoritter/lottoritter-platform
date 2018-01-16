package de.lottoritter.business.payment.control;

import de.lottoritter.business.lotteries.Price;

import javax.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Ulrich Cech
 */
@ApplicationScoped
public class CurrencyFormatter {

    public String convertCentToEuroFormatted(int amountInCent) {
        Price price = new Price(amountInCent);
        return convertCentToEuroFormatted(price);
    }

    public String convertCentToEuroFormatted(final Price price) {
        BigDecimal convertedPrice;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
        nf.setMinimumFractionDigits(2);
        DecimalFormat df = (DecimalFormat) nf;
        convertedPrice = new BigDecimal(BigInteger.valueOf(price.getAmountInCent())).divide(new BigDecimal("100.00"), 2,
                BigDecimal.ROUND_CEILING);
        return df.format(convertedPrice) + " " + price.getCurrency().getCurrencyCode();
    }

}
