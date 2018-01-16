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
package de.lottoritter.presentation.admin.drawings.control;

import de.lottoritter.business.drawings.control.DrawingsResultController;
import de.lottoritter.business.lotteries.Drawing;
import de.lottoritter.business.lotteries.Lottery;
import de.lottoritter.business.lotteries.LotteryManager;
import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotDrawing;
import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotLottery;
import de.lottoritter.business.temporal.control.DateTimeService;
import org.mongodb.morphia.Datastore;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * @author Ulrich Cech
 */
@Named
@ViewScoped
public class EurojackpotDrawingsViewController implements Serializable {

    private static final long serialVersionUID = 4463184518526072963L;

    @Inject
    Datastore datastore;

    @Inject
    DateTimeService dateTimeService;

    @Inject
    DrawingsResultController drawingsResultController;

    @Inject
    LotteryManager lotteryManager;

    @Inject
    EuroJackpotDrawingCrawler euroJackpotDrawingCrawler;

    private List<EuroJackpotDrawing> allDrawings;

    private EuroJackpotDrawing newEuroJackpotDrawing = new EuroJackpotDrawing();


    public EurojackpotDrawingsViewController() {
    }


    @PostConstruct
    private void init() {
        allDrawings = datastore.createQuery(EuroJackpotDrawing.class).field("lotteryIdentifier").equal(EuroJackpotLottery.IDENTIFIER).asList();
        allDrawings.sort(Comparator.comparing(Drawing::getDate));
    }


    public List<EuroJackpotDrawing> getAllEuroJackpotDrawings() {
        return getAllDrawings();
    }

    public void saveNewEuroJackpotDrawing(final String formId) {
        datastore.save(newEuroJackpotDrawing);
        allDrawings.add(newEuroJackpotDrawing);
        newEuroJackpotDrawing = new EuroJackpotDrawing();
    }

    public void calculateWinners(final String lotteryIdentifier, ZonedDateTime timestampOfDrawing) {
        final Lottery lottery = lotteryManager.getLotteryByIdentifier(lotteryIdentifier);
        drawingsResultController.executeCalculation(lottery, timestampOfDrawing);
    }

    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Import Completed"));
    }


    public List<EuroJackpotDrawing> getAllDrawings() {
        return allDrawings;
    }

    public void setAllDrawings(List<EuroJackpotDrawing> allDrawings) {
        this.allDrawings = allDrawings;
    }

    public EuroJackpotDrawing getNewEuroJackpotDrawing() {
        return newEuroJackpotDrawing;
    }

    public void setNewEuroJackpotDrawing(EuroJackpotDrawing newEuroJackpotDrawing) {
        this.newEuroJackpotDrawing = newEuroJackpotDrawing;
    }

    public EuroJackpotDrawingCrawler getEuroJackpotDrawingCrawler() {
        return euroJackpotDrawingCrawler;
    }
}
