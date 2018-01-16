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
import de.lottoritter.business.lotteries.german6aus49.German6aus49Drawing;
import de.lottoritter.business.lotteries.spiel77.Spiel77Drawing;
import de.lottoritter.business.lotteries.super6.Super6Drawing;
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
public class German6aus49DrawingsViewController implements Serializable {

    private static final long serialVersionUID = -2967142967371989270L;

    @Inject
    Datastore datastore;

    @Inject
    DateTimeService dateTimeService;

    @Inject
    DrawingsResultController drawingsResultController;

    @Inject
    LotteryManager lotteryManager;

    @Inject
    German6aus49DrawingCrawler german6aus49DrawingCrawler;

    @Inject
    Super6DrawingCrawler super6DrawingCrawler;

    @Inject
    Spiel77DrawingCrawler spiel77DrawingCrawler;

    private List<Drawing> allDrawings;

    private German6aus49Drawing newGerman6aus49Drawing = new German6aus49Drawing();

    private Super6Drawing newSuper6Drawing = new Super6Drawing();

    private Spiel77Drawing newSpiel77Drawing = new Spiel77Drawing();


    public German6aus49DrawingsViewController() {
    }


    @PostConstruct
    private void init() {
        readAllDrawings();
    }


    void readAllDrawings() {
        final Query<Drawing> query = datastore.createQuery(Drawing.class).disableValidation();
        query.or(
                query.criteria("className").equal(German6aus49Drawing.class.getName()),
                query.criteria("className").equal(Super6Drawing.class.getName()),
                query.criteria("className").equal(Spiel77Drawing.class.getName())
        );
        allDrawings = query.asList();
    }

    public List<German6aus49Drawing> getAllGerman6aus49Drawings() {
        return allDrawings.stream().filter(d -> d.getClass() == German6aus49Drawing.class).sorted(Comparator.comparing(Drawing::getDate)).map(d -> (German6aus49Drawing) d).collect(Collectors.toList());
    }

    public List<Super6Drawing> getAllSuper6Drawings() {
        return allDrawings.stream().filter(d -> d.getClass() == Super6Drawing.class).sorted(Comparator.comparing(Drawing::getDate)).map(d -> (Super6Drawing) d).collect(Collectors.toList());
    }

    public List<Spiel77Drawing> getAllSpiel77Drawings() {
        return allDrawings.stream().filter(d -> d.getClass() == Spiel77Drawing.class).sorted(Comparator.comparing(Drawing::getDate)).map(d -> (Spiel77Drawing) d).collect(Collectors.toList());
    }

    public void saveNewGerman6aus49Drawing(final String formId) {
        datastore.save(newGerman6aus49Drawing);
        allDrawings.add(newGerman6aus49Drawing);
        newGerman6aus49Drawing = new German6aus49Drawing();
    }

    public void saveNewSuper6Drawing(final String formId) {
        datastore.save(newSuper6Drawing);
        allDrawings.add(newSuper6Drawing);
        newSuper6Drawing = new Super6Drawing();
    }

    public void saveNewSpiel77Drawing(final String formId) {
        datastore.save(newSpiel77Drawing);
        allDrawings.add(newSpiel77Drawing);
        newSpiel77Drawing = new Spiel77Drawing();
    }

    public void calculateWinners(final String lotteryIdentifier, ZonedDateTime timestampOfDrawing) {
        final Lottery lottery = lotteryManager.getLotteryByIdentifier(lotteryIdentifier);
        drawingsResultController.executeCalculation(lottery, timestampOfDrawing);
    }

    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Import Completed"));
    }


    public List<Drawing> getAllDrawings() {
        return allDrawings;
    }

    public void setAllDrawings(List<Drawing> allDrawings) {
        this.allDrawings = allDrawings;
    }

    public German6aus49Drawing getNewGerman6aus49Drawing() {
        return newGerman6aus49Drawing;
    }

    public void setNewGerman6aus49Drawing(German6aus49Drawing newGerman6aus49Drawing) {
        this.newGerman6aus49Drawing = newGerman6aus49Drawing;
    }

    public Super6Drawing getNewSuper6Drawing() {
        return newSuper6Drawing;
    }

    public void setNewSuper6Drawing(Super6Drawing newSuper6Drawing) {
        this.newSuper6Drawing = newSuper6Drawing;
    }

    public Spiel77Drawing getNewSpiel77Drawing() {
        return newSpiel77Drawing;
    }

    public void setNewSpiel77Drawing(Spiel77Drawing newSpiel77Drawing) {
        this.newSpiel77Drawing = newSpiel77Drawing;
    }

    public German6aus49DrawingCrawler getGerman6aus49DrawingCrawler() {
        return german6aus49DrawingCrawler;
    }

    public Super6DrawingCrawler getSuper6DrawingCrawler() {
        return super6DrawingCrawler;
    }

    public Spiel77DrawingCrawler getSpiel77DrawingCrawler() {
        return spiel77DrawingCrawler;
    }

}
