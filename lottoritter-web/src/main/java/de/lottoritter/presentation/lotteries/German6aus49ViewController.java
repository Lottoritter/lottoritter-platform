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
package de.lottoritter.presentation.lotteries;

import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Lottery;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Ticket;
import de.lottoritter.business.payment.control.PriceListController;
import de.lottoritter.business.payment.entity.PriceList;
import de.lottoritter.business.shoppingcart.control.ShoppingCartSession;
import de.lottoritter.platform.ResourceBundleRepository;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Named
@SessionScoped
public class German6aus49ViewController implements Serializable {

    private static final long serialVersionUID = -3161229585166466916L;

    private static final Logger logger = Logger.getLogger(German6aus49ViewController.class.getName());

    @Inject
    ShoppingCartSession shoppingCartSession;

    @Inject
    PriceListController priceListController;

    @Inject
    German6aus49Lottery german6aus49Lottery;

    @Inject
    ResourceBundleRepository resourceBundleRepository;


    private String currentTicketAsJson;

    private PriceList priceList;


    public German6aus49ViewController() {
    }


    @PostConstruct
    private void init() {
        this.priceList = priceListController.getPriceListForLottery(German6aus49Lottery.IDENTIFIER);
    }

    public void addTicketToShoppingCart() throws IOException {
        try {
            shoppingCartSession.addTicket(Ticket.fromString(currentTicketAsJson, German6aus49Ticket.class));
            FacesContext.getCurrentInstance().getExternalContext().redirect("/shoppingcart");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(ex.getMessage()));
        }
    }

    public String getCurrentTicketAsJson() {
        return currentTicketAsJson;
    }

    public void setCurrentTicketAsJson(String currentTicketAsJson) {
        this.currentTicketAsJson = currentTicketAsJson;
    }

    public PriceList getPriceList() {
        return priceList;
    }

    public String getCurrentJackpotLabel() {
        final int jackpotInMillions = german6aus49Lottery.getCurrentJackpot().getJackpotInMillions();
        final Locale currentLocale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        if (jackpotInMillions != 0) {
            return String.valueOf(jackpotInMillions)
                    + " "
                    + resourceBundleRepository.getDefaultLocalized("lottery.jackpot.mio", currentLocale)
                    + " € "
                    + resourceBundleRepository.getDefaultLocalized("lottery.jackpot", currentLocale);
        } else {
            return resourceBundleRepository.getDefaultLocalized("lottery.jackpot.not_available", currentLocale);
        }
    }



    public String getNextClosingDate() {
        return german6aus49Lottery.getNextClosingDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}