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
package de.lottoritter.business.lotteries.eurojackpot;

import com.mongodb.ReadPreference;
import de.lottoritter.business.lotteries.Jackpot;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;


/**
 * @author Christopher Schmidt
 */
@Entity(value = "jackpots", noClassnameStored = false)
public class EuroJackpotJackpot extends Jackpot {

    private static final long serialVersionUID = -9057985885532546458L;


    public static EuroJackpotJackpot getCurrent(Datastore datastore) {
        Query<EuroJackpotJackpot> query = datastore.createQuery(EuroJackpotJackpot.class)
                .disableValidation()
                .filter("className", EuroJackpotJackpot.class.getName());
        FindOptions findOptions = new FindOptions().readPreference(ReadPreference.secondary());
        return query.get(findOptions);
    }
}
