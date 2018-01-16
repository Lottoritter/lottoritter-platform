package de.lottoritter.business.payment.control;

import org.hamcrest.core.Is;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Ulrich Cech
 */
public class CurrencyFormatterTest {

    @Test
    public void convertCentToEuroFormatted() {
        CurrencyFormatter cut = new CurrencyFormatter();
        assertThat(cut.convertCentToEuroFormatted(18250), is("182,50 EUR"));
    }
}