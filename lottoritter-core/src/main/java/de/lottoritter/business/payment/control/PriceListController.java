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
package de.lottoritter.business.payment.control;

import de.lottoritter.business.payment.entity.PriceList;
import de.lottoritter.business.temporal.control.DateTimeService;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.ejb.Singleton;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author Ulrich Cech
 */
@Singleton
public class PriceListController implements Serializable {

    private static final long serialVersionUID = 7141204212338244369L;

    @Inject
    Datastore datastore;

    @Inject
    DateTimeService dateTimeService;


    public PriceListController() {
    }


    public PriceList getPriceListForLottery(String lotteryIdentifier) {
        final Query<PriceList> query = datastore.createQuery(PriceList.class).order("-validFrom");
        query.and(
                query.criteria("lotteryIdentifier").equal(lotteryIdentifier),
                query.criteria("validFrom").lessThan(dateTimeService.getDateTimeNowEurope()));
        return query.get();
    }

    public PriceList getPriceListForId(ObjectId priceListId) {
        return datastore.get(PriceList.class, priceListId);
    }

}
