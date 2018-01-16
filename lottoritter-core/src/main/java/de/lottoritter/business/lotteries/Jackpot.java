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

import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotJackpot;
import de.lottoritter.platform.persistence.PersistentEntity;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;

import com.mongodb.ReadPreference;


/**
 * @author Christopher Schmidt
 */
@Entity("jackpots")
public abstract class Jackpot extends PersistentEntity {

    private static final long serialVersionUID = -2079455575717438527L;


    @Embedded
    private int jackpotInMillions;

    public int getJackpotInMillions() {
        return jackpotInMillions;
    }

    public void setJackpotInMillions(int jackpotInMillions) {
        this.jackpotInMillions = jackpotInMillions;
    }

}
