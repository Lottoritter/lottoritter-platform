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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ejb.Singleton;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * @author Christopher Schmidt
 */
@Singleton
public class LotteryManager {

    @Inject
    private Instance<Lottery> lotteries;

    public List<Lottery> getLotteries() {
        List<Lottery> result = new ArrayList<>();
        for (Lottery lottery : lotteries) {
            result.add(lottery);
        }
        return result;
    }

    public Lottery getLotteryByIdentifier(String lotteryIdentifier) {
        return getLotteries().stream().filter(l -> Objects.equals(l.getIdentifier(), lotteryIdentifier)).findFirst().get();
    }
}
