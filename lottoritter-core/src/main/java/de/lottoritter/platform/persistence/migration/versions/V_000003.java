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

import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotJackpot;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Jackpot;
import de.lottoritter.platform.persistence.migration.control.Migrateable;
import org.mongodb.morphia.Datastore;

import java.io.IOException;

/**
 * Imports initial jackpots
 *
 * @author Christopher Schmidt
 */
@SuppressWarnings("unused")
public class V_000003 implements Migrateable {

    @Override
    public void executeMigration(Datastore datastore) throws IOException {
        German6aus49Jackpot german6aus49Jackpot = new German6aus49Jackpot();
        german6aus49Jackpot.setJackpotInMillions(1);

        EuroJackpotJackpot euroJackpotJackpot = new EuroJackpotJackpot();
        euroJackpotJackpot.setJackpotInMillions(1);

        datastore.save(german6aus49Jackpot);
        datastore.save(euroJackpotJackpot);
    }

}
