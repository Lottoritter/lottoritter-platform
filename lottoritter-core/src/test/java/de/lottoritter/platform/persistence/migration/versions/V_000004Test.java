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
package de.lottoritter.platform.persistence.migration.versions;

import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotLottery;
import de.lottoritter.business.payment.entity.PriceList;
import de.lottoritter.platform.persistence.FongoDbPersistenceTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Ulrich Cech
 */
public class V_000004Test extends FongoDbPersistenceTest {

    @Test
    public void executeMigration() throws Exception {
        V_000004 cut = new V_000004();
        cut.executeMigration(getDatastore());
        PriceList priceList = getDatastore().createQuery(PriceList.class).field("lotteryIdentifier").equal(EuroJackpotLottery.IDENTIFIER).get();
        assertThat(priceList, notNullValue());
        assertThat(priceList.getValidFrom(), is(cut.getInitialDate()));
        assertThat(priceList.getPricePerField(), is(200));
        assertThat(priceList.getPriceSuper6(), is(125));
        assertThat(priceList.getPriceSpiel77(), is(250));
        assertThat(priceList.getPriceGluecksspirale(), is(500));
        assertThat(priceList.getFeeFirstDrawing(), is(60));
        assertThat(priceList.getFeeSecondDrawing(), is(40));
    }

}