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

import de.lottoritter.business.lotteries.keno.KenoDrawing;
import de.lottoritter.business.lotteries.keno.KenoLottery;
import de.lottoritter.business.temporal.control.DateTimeService;
import de.lottoritter.platform.persistence.FongoDbPersistenceTest;
import de.lottoritter.presentation.admin.drawings.control.KenoDrawingCrawler;
import org.junit.Ignore;
import org.junit.Test;
import org.mongodb.morphia.query.Query;

import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Ulrich Cech
 */
@Ignore
public class KenoDrawingCrawlerTest extends FongoDbPersistenceTest {

    @Test
    public void testCrawlKenoDrawings() throws Exception {
        KenoDrawingCrawler cut = new KenoDrawingCrawler();
        cut.datastore = getDatastore();
        cut.dateTimeService = new DateTimeService();
        cut.crawlKenoDrawings();
        final Query<KenoDrawing> query = getDatastore().createQuery(KenoDrawing.class).disableValidation();
        final List<KenoDrawing> importedDrawings = query.asList();
        assertThat(importedDrawings, notNullValue());
        assertThat(importedDrawings.get(0).getLotteryIdentifier(), is(KenoLottery.IDENTIFIER));
        assertThat(importedDrawings.size(), greaterThanOrEqualTo(1369));
    }

}