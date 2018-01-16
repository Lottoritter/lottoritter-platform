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
package de.lottoritter.business.lotteries.crawler;

import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotJackpot;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.mongodb.morphia.Datastore;

import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christopher Schmidt
 */
@Stateless
@DependsOn("DBConnection")
public class EuroJackpotCrawler {

    private static final Logger logger = Logger.getLogger(EuroJackpotCrawler.class.getName());

    private static final String URL = "https://www.lotto-hh.de/eurojackpot/spielschein_eurojackpot/spielschein_eurojackpot.jsp";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:48.0) Gecko/20100101 Firefox/48.0";
    private static final String REFERRER = "https://www.google.com";

    @Inject
    private Datastore datastore;

    @Schedule(hour = "*", minute = "*/30")
    public void crawl() throws IOException {
        try {
            Document document = Jsoup.connect(URL).userAgent(USER_AGENT).referrer(REFERRER).get();
            Elements select = document.select(".jackpot>span");
            int newJackPotInMillions = 10;
            if ((select != null) && !select.isEmpty() && (select.get(1) != null)) {
                String jackpotHeightNumberInMillions = select.get(1).text();
                try {
                    newJackPotInMillions = Integer.parseInt(jackpotHeightNumberInMillions);
                } catch (NumberFormatException ignore) {}
            }
            EuroJackpotJackpot current = EuroJackpotJackpot.getCurrent(datastore);
            datastore.update(
                    datastore.createQuery(EuroJackpotJackpot.class).field("_id").equal(current.getId()),
                    datastore.createUpdateOperations(EuroJackpotJackpot.class).set("jackpotInMillions", newJackPotInMillions)
            );
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
