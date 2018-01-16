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
import de.lottoritter.platform.persistence.migration.control.Migrateable;
import org.mongodb.morphia.Datastore;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author Ulrich Cech
 */
@SuppressWarnings("unused")
public class V_000004 implements Migrateable {


    @Override
    public void executeMigration(Datastore datastore) {
        PriceList priceList = datastore.createQuery(PriceList.class).field("lotteryIdentifier").equal(EuroJackpotLottery.IDENTIFIER).get();
        if (priceList == null) {
            priceList = new PriceList();
        }
        priceList.setLotteryIdentifier(EuroJackpotLottery.IDENTIFIER);
        priceList.setPricePerField(200);
        priceList.setPriceSuper6(125);
        priceList.setPriceSpiel77(250);
        priceList.setPriceGluecksspirale(500);
        priceList.setFeeGluecksspirale(60);
        priceList.setFeeFirstDrawing(60);
        priceList.setFeeSecondDrawing(40);
        priceList.setValidFrom(getInitialDate());
        datastore.save(priceList);
    }

    ZonedDateTime getInitialDate() {
        return ZonedDateTime.of(2017, 2, 18, 0, 0, 0, 0, ZoneId.of("Europe/Paris"));
    }

}
