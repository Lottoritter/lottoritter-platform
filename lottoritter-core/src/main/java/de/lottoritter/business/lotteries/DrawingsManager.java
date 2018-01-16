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
package de.lottoritter.business.lotteries;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;

/**
 * @author Ulrich Cech
 */
@Singleton
public class DrawingsManager {

    @Inject
    Datastore datastore;

    @Inject
    LotteryManager lotteryManager;


    public DrawingsManager() {
    }



    public Map<ZonedDateTime, Drawing> getDrawingForLotteryAndTimeRange(final String lotteryIdentifier,
                                                                        ZonedDateTime minStartingDate,
                                                                        ZonedDateTime endingDate) {
        Query<Drawing> query = datastore.createQuery(Drawing.class);
        query.and(
                query.criteria("lotteryIdentifier").equal(lotteryIdentifier),
                query.criteria("date").greaterThanOrEq(minStartingDate),
                query.criteria("date").lessThanOrEq(endingDate)
        );
        query.order(Sort.ascending("date"));
        final List<Drawing> drawings = query.asList();
        return drawings.stream().collect(Collectors.toMap(Drawing::getDate, Function.identity()));
    }

    public Drawing getDrawingForLotteryAndTimestamp(String lotteryIdentifier, ZonedDateTime timestamp) {
        Query<Drawing> query = datastore.createQuery(Drawing.class);
        query.and(
                query.criteria("lotteryIdentifier").equal(lotteryIdentifier),
                query.criteria("date").equal(timestamp)
        );
        return query.get();
    }
}
