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
import de.lottoritter.business.lotteries.german6aus49.German6aus49Drawing;
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
public class German6aus49DrawingCrawler {

    private static final Logger logger = Logger.getLogger(German6aus49DrawingCrawler.class.getName());

    private static final String URL = "http://www.lottozahlenonline.de/statistik/beide-spieltage/lottozahlen-archiv.php?j=";
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
    public void crawl6aus49Drawings() {
        if (runJobs.get()) {
            final ObjectId activityLogObjectId = new ObjectId();
            activityLogController.saveActivityLog(activityLogObjectId, ActivityType.DRAWING_CRAWLER_GERMAN6AUS49, "text", "Starting German6aus49 drawing crawler.");
            try {
                progress = 0;
                int start = 2012;
                int end = dateTimeService.getDateTimeNowEurope().getYear();
                int progressIncrement = 100 / (end - start);
                for (int i = start; i <= end; i++) {
                    progress += progressIncrement;
                    Document document = Jsoup.connect(URL + i).userAgent(USER_AGENT).referrer(REFERRER).get();
                    Elements select = document.getElementsByClass("zahlensuche_rahmen");
                    for (Element element : select) {
                        String drawingDate = element.select("div.zahlensuche_datum").text();
                        if ((drawingDate == null) || (drawingDate.length() == 0)) {
                            continue;
                        }
                        Elements numberDivs = element.getElementsByClass("zahlensuche_zahl");
                        List<Integer> numbers = new ArrayList<>(5);
                        for (Element numberDiv : numberDivs) {
                            numbers.add(Integer.parseInt(numberDiv.text()));
                        }
                        Elements zzNumberDivs = element.getElementsByClass("zahlensuche_zz");
                        final German6aus49Drawing german6aus49Drawing = new German6aus49Drawing();
                        for (Element zzNumberDiv : zzNumberDivs) {
                            german6aus49Drawing.setSuperzahl(Integer.parseInt(zzNumberDiv.text()));
                        }
                        final String[] split = drawingDate.split("\\.");
                        german6aus49Drawing.setDate(dateTimeService.getDateTimeNowEurope()
                                .withYear(Integer.parseInt(split[2])).withMonth(Integer.parseInt(split[1]))
                                .withDayOfMonth(Integer.parseInt(split[0])).withHour(18).withMinute(0).withSecond(0).withNano(0));
                        if (german6aus49Drawing.getDate().getDayOfWeek().getValue() == 6) {
                            german6aus49Drawing.setDate(german6aus49Drawing.getDate().withHour(19));
                        }
                        german6aus49Drawing.setNumbers(numbers.toArray(new Integer[5]));
                        final Query<German6aus49Drawing> query = datastore.createQuery(German6aus49Drawing.class).disableValidation();
                        query.and(
                                query.criteria("date").equal(german6aus49Drawing.getDate()),
                                query.criteria("className").equal(German6aus49Drawing.class.getName())
                        );
                        if (query.get() == null) {
                            datastore.save(german6aus49Drawing);
                        }
                    }
                }
                activityLogController.saveActivityLog(activityLogObjectId, ActivityType.DRAWING_CRAWLER_GERMAN6AUS49, "text", "Finished German6aus49 drawing crawler.");
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
