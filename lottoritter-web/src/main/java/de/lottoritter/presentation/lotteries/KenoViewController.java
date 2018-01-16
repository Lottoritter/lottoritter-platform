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
import de.lottoritter.business.lotteries.keno.KenoLottery;
import de.lottoritter.business.lotteries.keno.KenoTicket;
import de.lottoritter.business.payment.control.PriceListController;
import de.lottoritter.business.payment.entity.PriceList;
import de.lottoritter.business.shoppingcart.control.ShoppingCartSession;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christopher Schmidt
 */
@Named
@SessionScoped
public class KenoViewController implements Serializable {

    private static final long serialVersionUID = -3161229585166466916L;

    private static final Logger logger = Logger.getLogger(KenoViewController.class.getName());


    @Inject
    ShoppingCartSession shoppingCartSession;

    @Inject
    PriceListController priceListController;

    @Inject
    KenoLottery kenoLottery;

    private String currentTicketAsJson;

    private PriceList priceList;


    public KenoViewController() {
    }


    @PostConstruct
    private void init() {
        this.priceList = priceListController.getPriceListForLottery(KenoLottery.IDENTIFIER);
    }

    public void addTicketToShoppingCart() throws IOException {
        try {
            shoppingCartSession.addTicket(Ticket.fromString(currentTicketAsJson, KenoTicket.class));
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

    public String getCurrentJackpotNumber() {
        return String.valueOf(kenoLottery.getCurrentJackpot().getJackpotInMillions());
    }

    public String getNextClosingDate() {
        return kenoLottery.getNextClosingDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}