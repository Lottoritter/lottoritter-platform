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

import de.lottoritter.business.activitylog.control.ActivityLogController;
import de.lottoritter.business.activitylog.entity.ActivityType;
import de.lottoritter.business.configuration.control.Configurable;
import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotDrawing;
import de.lottoritter.business.temporal.control.DateTimeService;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Named
@Singleton
@Startup
@DependsOn("ConfigurationController")
public class EuroJackpotDrawingCrawler {

    // http://www.eurojackpot-zahlen.eu/eurojackpot-zahlenarchiv.php?j=2017
    private static final Logger logger = Logger.getLogger(EuroJackpotDrawingCrawler.class.getName());

    private static final String URL = "http://www.eurojackpot-zahlen.eu/eurojackpot-zahlenarchiv.php?j=";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:48.0) Gecko/20100101 Firefox/48.0";
    private static final String REFERRER = "https://www.google.com";

    @Inject
    private Datastore datastore;

    @Inject
    DateTimeService dateTimeService;

    @Inject
    ActivityLogController activityLogController;

    @Inject @Configurable(value = "runJobs", defaultValue = "false")
    Instance<Boolean> runJobs;

    private Integer progress;


    @Schedule(hour = "*", minute = "0", second = "0")
    public void crawlEuroJackpotDrawings() {
        if (runJobs.get()) {
            final ObjectId activityLogObjectId = new ObjectId();
            activityLogController.saveActivityLog(activityLogObjectId, ActivityType.DRAWING_CRAWLER_EUROJACKPOT, "text", "Starting EuroJackpot drawing crawler.");
            try {
                progress = 0;
                int start = 2012;
                int end = dateTimeService.getDateTimeNowEurope().getYear();
                int progressIncrement = 100 / (end - start);
                for (int i = start; i <= end; i++) {
                    progress += progressIncrement;
                    Document document = Jsoup.connect(URL + i).userAgent(USER_AGENT).referrer(REFERRER).get();
                    Elements select = document.getElementsByClass("zahlen_rahmen");
                    for (Element element : select) {
                        String drawingDate = element.select("div.zahlenarchiv_datum").text();
                        if ((drawingDate == null) || (drawingDate.length() == 0)) {
                            continue;
                        }
                        Elements numberDivs = element.getElementsByClass("zahlenarchiv_zahl");
                        List<Integer> numbers = new ArrayList<>(5);
                        for (Element numberDiv : numberDivs) {
                            numbers.add(Integer.parseInt(numberDiv.text()));
                        }
                        Elements zzNumberDivs = element.getElementsByClass("zahlenarchiv_zz");
                        List<Integer> additionalNumbers = new ArrayList<>(2);
                        for (Element zzNumberDiv : zzNumberDivs) {
                            additionalNumbers.add(Integer.parseInt(zzNumberDiv.text()));
                        }
                        final EuroJackpotDrawing euroJackpotDrawing = new EuroJackpotDrawing();
                        final String[] split = drawingDate.split("\\.");
                        euroJackpotDrawing.setDate(dateTimeService.getDateTimeNowEurope()
                                .withYear(Integer.parseInt(split[2])).withMonth(Integer.parseInt(split[1]))
                                .withDayOfMonth(Integer.parseInt(split[0])).withHour(20).withMinute(0).withSecond(0).withNano(0));
                        euroJackpotDrawing.setNumbers(numbers.toArray(new Integer[5]));
                        euroJackpotDrawing.setAdditionalNumbers(additionalNumbers.toArray(new Integer[2]));
                        final Query<EuroJackpotDrawing> query = datastore.createQuery(EuroJackpotDrawing.class).disableValidation();
                        query.and(
                                query.criteria("date").equal(euroJackpotDrawing.getDate().toInstant().atZone(ZoneId.of("UTC"))),
                                query.criteria("className").equal(EuroJackpotDrawing.class.getName())
                        );
                        if (query.get() == null) {
                            datastore.save(euroJackpotDrawing);
                        }
                    }
                }
                activityLogController.saveActivityLog(activityLogObjectId, ActivityType.DRAWING_CRAWLER_EUROJACKPOT, "text", "Finished EuroJackpot drawing crawler.");
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public Integer getProgress() {
        if (progress == null) {
            progress = 0;
        } else if (progress > 100) {
            progress = 100;
        }
        return progress;
    }

}
