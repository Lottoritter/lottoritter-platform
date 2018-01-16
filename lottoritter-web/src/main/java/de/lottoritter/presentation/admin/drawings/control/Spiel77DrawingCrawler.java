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
import de.lottoritter.business.lotteries.spiel77.Spiel77Drawing;
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
public class Spiel77DrawingCrawler {

    private static final Logger logger = Logger.getLogger(Spiel77DrawingCrawler.class.getName());

    private static final String URL = "http://www.dielottozahlende.net/lotto/spiel77/spiel77";
    private static final String URL_CURRENT_YEAR = "http://www.dielottozahlende.net/lotto/spiel77/zahlen.html";
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
    public void crawlSpiel77Drawings() {
        if (runJobs.get()) {
            final ObjectId activityLogObjectId = new ObjectId();
            activityLogController.saveActivityLog(activityLogObjectId, ActivityType.DRAWING_CRAWLER_SPIEL77, "text", "Starting Spiel77 drawing crawler.");
            try {
                progress = 0;
                int start = 12;
                int end = dateTimeService.getDateTimeNowEurope().getYear() - 2000;
                int progressIncrement = 100 / (end - start);
                for (int i = start; i <= end; i++) {
                    progress += progressIncrement;
                    Document document;
                    if (i != end) {
                        document = Jsoup.connect(URL + i + ".html").userAgent(USER_AGENT).referrer(REFERRER).get();
                    } else {
                        document = Jsoup.connect(URL_CURRENT_YEAR).userAgent(USER_AGENT).referrer(REFERRER).get();
                    }
                    Element select = document.getElementsByClass("spiel77Wrap").first();
                    final Elements results = select.getElementsByClass("item");
                    for (Element result : results) {
                        String drawingDate = result.getElementsByTag("h3").first().text();
                        final Elements numbersLIs = result.select("div.lottozahlen-ziehung ul>li");
                        List<Integer> numbers = new ArrayList<>(7);
                        for (Element numberLi : numbersLIs) {
                            numbers.add(Integer.parseInt(numberLi.text()));
                        }
                        final Spiel77Drawing spiel77Drawing = new Spiel77Drawing();
                        final String[] split = drawingDate.split(",");
                        final String[] splitDate = split[1].trim().split("\\.");
                        spiel77Drawing.setDate(dateTimeService.getDateTimeNowEurope()
                                .withYear(Integer.parseInt(splitDate[2])).withMonth(Integer.parseInt(splitDate[1]))
                                .withDayOfMonth(Integer.parseInt(splitDate[0])).withHour(18).withMinute(0).withSecond(0).withNano(0));
                        if (spiel77Drawing.getDate().getDayOfWeek().getValue() == 6) {
                            spiel77Drawing.setDate(spiel77Drawing.getDate().withHour(19));
                        }
                        spiel77Drawing.setNumbers(numbers.toArray(new Integer[7]));
                        final Query<Spiel77Drawing> query = datastore.createQuery(Spiel77Drawing.class).disableValidation();
                        query.and(
                                query.criteria("date").equal(spiel77Drawing.getDate()),
                                query.criteria("className").equal(Spiel77Drawing.class.getName())
                        );
                        if (query.get() == null) {
                            datastore.save(spiel77Drawing);
                        }
                    }
                }
                activityLogController.saveActivityLog(activityLogObjectId, ActivityType.DRAWING_CRAWLER_SPIEL77, "text", "Finished Spiel77 drawing crawler.");
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
