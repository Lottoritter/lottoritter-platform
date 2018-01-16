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
import de.lottoritter.business.lotteries.keno.KenoDrawing;
import de.lottoritter.business.lotteries.keno.KenoLottery;
import de.lottoritter.business.lotteries.plus5.Plus5Drawing;
import de.lottoritter.business.lotteries.plus5.Plus5Lottery;
import de.lottoritter.business.temporal.control.DateTimeService;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

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
import java.util.stream.Collectors;

/**
 * @author Ulrich Cech
 */
@Named
@ViewScoped
public class KenoDrawingsViewController implements Serializable {

    private static final long serialVersionUID = 7333012943337322680L;

    @Inject
    Datastore datastore;

    @Inject
    DateTimeService dateTimeService;

    @Inject
    DrawingsResultController drawingsResultController;

    @Inject
    LotteryManager lotteryManager;

    @Inject
    KenoDrawingCrawler kenoDrawingCrawler;

    private List<Drawing> allDrawings;

    private KenoDrawing newKenoDrawing = new KenoDrawing();

    private Plus5Drawing newPlus5Drawing = new Plus5Drawing();


    public KenoDrawingsViewController() {
    }


    @PostConstruct
    private void init() {
        readAllDrawings();
    }


    void readAllDrawings() {
        final Query<Drawing> query = datastore.createQuery(Drawing.class).disableValidation();
        query.or(
                query.criteria("lotteryIdentifier").equal(KenoLottery.IDENTIFIER),
                query.criteria("lotteryIdentifier").equal(Plus5Lottery.IDENTIFIER)
        );
        allDrawings = query.asList();
    }

    public List<KenoDrawing> getAllKenoDrawings() {
        return allDrawings.stream().filter(d -> d.getClass() == KenoDrawing.class).sorted(Comparator.comparing(Drawing::getDate)).map(d -> (KenoDrawing) d).collect(Collectors.toList());
    }

    public List<Plus5Drawing> getAllPlus5Drawings() {
        return allDrawings.stream().filter(d -> d.getClass() == Plus5Drawing.class).sorted(Comparator.comparing(Drawing::getDate)).map(d -> (Plus5Drawing) d).collect(Collectors.toList());
    }

    public void saveNewKenoDrawing(final String formId) {
        datastore.save(newKenoDrawing);
        allDrawings.add(newKenoDrawing);
        newKenoDrawing = new KenoDrawing();
    }

    public void saveNewPlus5Drawing(final String formId) {
        datastore.save(newPlus5Drawing);
        allDrawings.add(newPlus5Drawing);
        newPlus5Drawing = new Plus5Drawing();
    }

    public void calculateWinners(final String lotteryIdentifier, ZonedDateTime timestampOfDrawing) {
        final Lottery lottery = lotteryManager.getLotteryByIdentifier(lotteryIdentifier);
        drawingsResultController.executeCalculation(lottery, timestampOfDrawing);
    }

    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Import Completed"));
        getKenoDrawingCrawler().setProgress(0);
    }


    public List<Drawing> getAllDrawings() {
        return allDrawings;
    }

    public void setAllDrawings(List<Drawing> allDrawings) {
        this.allDrawings = allDrawings;
    }

    public KenoDrawing getNewKenoDrawing() {
        return newKenoDrawing;
    }

    public void setNewKenoDrawing(KenoDrawing newKenoDrawing) {
        this.newKenoDrawing = newKenoDrawing;
    }

    public Plus5Drawing getNewPlus5Drawing() {
        return newPlus5Drawing;
    }

    public void setNewPlus5Drawing(Plus5Drawing newPlus5Drawing) {
        this.newPlus5Drawing = newPlus5Drawing;
    }

    public KenoDrawingCrawler getKenoDrawingCrawler() {
        return kenoDrawingCrawler;
    }

}
