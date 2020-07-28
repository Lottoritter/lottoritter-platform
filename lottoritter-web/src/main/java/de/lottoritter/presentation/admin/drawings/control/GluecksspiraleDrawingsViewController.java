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
import de.lottoritter.business.lotteries.gluecksspirale.GluecksspiraleDrawing;
import de.lottoritter.business.temporal.control.DateTimeService;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Named
@ViewScoped
public class GluecksspiraleDrawingsViewController implements Serializable {

    // https://www.sachsenlotto.de/portal/zahlen-quoten/gewinnzahlen/download-archiv/gewinnzahlen_download.jsp

    private static final Logger logger = Logger.getLogger(GluecksspiraleDrawingsViewController.class.getName());

    private static final long serialVersionUID = -5928128964973198271L;

    @Inject
    Datastore datastore;

    @Inject
    DateTimeService dateTimeService;

    @Inject
    DrawingsResultController drawingsResultController;

    @Inject
    LotteryManager lotteryManager;

    private List<GluecksspiraleDrawing> allDrawings;

    private GluecksspiraleDrawing newGluecksspiraleDrawing = new GluecksspiraleDrawing();

    private transient UploadedFile newUploadedGluecksspiraleResultFile;

    private transient Part file;


    public GluecksspiraleDrawingsViewController() {
    }


    @PostConstruct
    private void init() {
        allDrawings = datastore.createQuery(GluecksspiraleDrawing.class).field("lotteryIdentifier").equal("gluecksspirale").asList();
        allDrawings.sort(Comparator.comparing(Drawing::getDate));
    }


    public List<GluecksspiraleDrawing> getAllGluecksspiraleDrawings() {
        return getAllDrawings();
    }

// TODO: can be reactivated if Primefaces-Bug is solved
//    public void fileUploadListener(FileUploadEvent event) {
//        setNewUploadedGluecksspiraleResultFile(event.getFile());
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(getNewUploadedGluecksspiraleResultFile().getInputstream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if ((line.length() > 0) && StringUtils.isNumeric("" + line.charAt(0))) {
//                    final GluecksspiraleDrawing gluecksspiraleDrawing = new GluecksspiraleDrawing();
//                    final String[] elements = line.split(";");
//
//                    final String[] drawingDateElements = elements[0].split("\\.");
//                    gluecksspiraleDrawing.setDate(dateTimeService.getDateTimeNowEurope()
//                            .withYear(Integer.parseInt(drawingDateElements[2]))
//                            .withMonth(Integer.parseInt(drawingDateElements[1]))
//                            .withDayOfMonth(Integer.parseInt(drawingDateElements[0]))
//                            .withHour(19).withMinute(0).withSecond(0).withNano(0));
//
//                    gluecksspiraleDrawing.addGluecksspiraleNumber(1, 1, elements[1].trim());
//                    gluecksspiraleDrawing.addGluecksspiraleNumber(2, 1, elements[2].trim());
//                    gluecksspiraleDrawing.addGluecksspiraleNumber(3, 1, elements[3].trim());
//                    gluecksspiraleDrawing.addGluecksspiraleNumber(4, 1, elements[4].trim());
//                    gluecksspiraleDrawing.addGluecksspiraleNumber(5, 1, elements[5].trim());
//
//                    final String[] numbersOfRank6 = elements[6].split(",");
//                    gluecksspiraleDrawing.addGluecksspiraleNumber(6, 1, numbersOfRank6[0].trim());
//                    gluecksspiraleDrawing.addGluecksspiraleNumber(6, 2, numbersOfRank6[1].trim());
//
//                    final String[] numbersOfRank7 = elements[7].split(",");
//                    gluecksspiraleDrawing.addGluecksspiraleNumber(7, 1, numbersOfRank7[0].trim());
//                    if (numbersOfRank7.length > 1) {
//                        gluecksspiraleDrawing.addGluecksspiraleNumber(7, 2, numbersOfRank7[1].trim());
//                    }
//
//                    final Query<EuroJackpotDrawing> query = datastore.createQuery(EuroJackpotDrawing.class).disableValidation();
//                    query.and(
//                            query.criteria("date").equal(gluecksspiraleDrawing.getDate().toInstant().atZone(ZoneId.of("UTC"))),
//                            query.criteria("className").equal(GluecksspiraleDrawing.class.getName())
//                    );
//                    if (query.get() == null) {
//                        datastore.save(gluecksspiraleDrawing);
//                    }
//
//                }
//            }
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, e.getMessage(), e);
//        }
//    }


    public void upload() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if ((line.length() > 0) && StringUtils.isNumeric("" + line.charAt(0))) {
                    final GluecksspiraleDrawing gluecksspiraleDrawing = new GluecksspiraleDrawing();
                    final String[] elements = line.split(";");

                    final String[] drawingDateElements = elements[0].split("\\.");
                    gluecksspiraleDrawing.setDate(dateTimeService.getDateTimeNowEurope()
                            .withYear(Integer.parseInt(drawingDateElements[2]))
                            .withMonth(Integer.parseInt(drawingDateElements[1]))
                            .withDayOfMonth(Integer.parseInt(drawingDateElements[0]))
                            .withHour(19).withMinute(0).withSecond(0).withNano(0));

                    gluecksspiraleDrawing.addGluecksspiraleNumber(1, 1, elements[1].trim());
                    gluecksspiraleDrawing.addGluecksspiraleNumber(2, 1, elements[2].trim());
                    gluecksspiraleDrawing.addGluecksspiraleNumber(3, 1, elements[3].trim());
                    gluecksspiraleDrawing.addGluecksspiraleNumber(4, 1, elements[4].trim());
                    gluecksspiraleDrawing.addGluecksspiraleNumber(5, 1, elements[5].trim());

                    final String[] numbersOfRank6 = elements[6].split(",");
                    gluecksspiraleDrawing.addGluecksspiraleNumber(6, 1, numbersOfRank6[0].trim());
                    gluecksspiraleDrawing.addGluecksspiraleNumber(6, 2, numbersOfRank6[1].trim());

                    final String[] numbersOfRank7 = elements[7].split(",");
                    gluecksspiraleDrawing.addGluecksspiraleNumber(7, 1, numbersOfRank7[0].trim());
                    if (numbersOfRank7.length > 1) {
                        gluecksspiraleDrawing.addGluecksspiraleNumber(7, 2, numbersOfRank7[1].trim());
                    }

                    final Query<EuroJackpotDrawing> query = datastore.createQuery(EuroJackpotDrawing.class)
                            .disableValidation();
                    query.and(
                            query.criteria("date").equal(gluecksspiraleDrawing.getDate().toInstant().atZone(ZoneId.of("UTC"))),
                            query.criteria("className").equal(GluecksspiraleDrawing.class.getName())
                    );
                    if (query.get() == null) {
                        datastore.save(gluecksspiraleDrawing);
                    }

                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void calculateWinners(final String lotteryIdentifier, ZonedDateTime timestampOfDrawing) {
        final Lottery lottery = lotteryManager.getLotteryByIdentifier(lotteryIdentifier);
        drawingsResultController.executeCalculation(lottery, timestampOfDrawing);
    }

    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Import Completed"));
    }


    public List<GluecksspiraleDrawing> getAllDrawings() {
        return allDrawings;
    }

    public void setAllDrawings(List<GluecksspiraleDrawing> allDrawings) {
        this.allDrawings = allDrawings;
    }

    public GluecksspiraleDrawing getNewGluecksspiraleDrawing() {
        return newGluecksspiraleDrawing;
    }

    public void setNewGluecksspiraleDrawing(GluecksspiraleDrawing newGluecksspiraleDrawing) {
        this.newGluecksspiraleDrawing = newGluecksspiraleDrawing;
    }


    public UploadedFile getNewUploadedGluecksspiraleResultFile() {
        return newUploadedGluecksspiraleResultFile;
    }

    public void setNewUploadedGluecksspiraleResultFile(UploadedFile newUploadedGluecksspiraleResultFile) {
        this.newUploadedGluecksspiraleResultFile = newUploadedGluecksspiraleResultFile;
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }
}