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
package de.lottoritter.business.payment.control;

import de.lottoritter.business.payment.entity.Bank;
import org.mongodb.morphia.Datastore;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.inject.Named;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Singleton
@Named
public class BankController {

    private static final Logger logger = Logger.getLogger(BankController.class.getName());

    @Inject
    Datastore datastore;


    public String getBICCodeFromIBAN(String iban) {
        try {
            String blz = iban.substring(4, 12);
            final Bank bank = datastore.find(Bank.class).field("blz").equal(blz).get();
            if (bank != null) {
                return bank.getBic();
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, MessageFormat.format("Could not extract the BIC from the IBAN <{0}>", iban), ex);
            return "";
        }
        return "";
    }

}
