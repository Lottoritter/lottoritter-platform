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
package de.lottoritter.business.lotteries.german6aus49;

import com.mongodb.ReadPreference;
import de.lottoritter.business.lotteries.Jackpot;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;


/**
 * @author Christopher Schmidt
 */
@Entity("jackpots")
public class German6aus49Jackpot extends Jackpot {

    private static final long serialVersionUID = 2060721828742896084L;


    public German6aus49Jackpot() {
    }


    public static German6aus49Jackpot getCurrent(final Datastore datastore) {
        final Query<German6aus49Jackpot> query = datastore.createQuery(German6aus49Jackpot.class)
                .disableValidation()
                .filter("className", German6aus49Jackpot.class.getName());
        FindOptions findOptions = new FindOptions().readPreference(ReadPreference.secondary());
        return query.get(findOptions);
    }
}
