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
package de.lottoritter.business.lotteries.keno;

import de.lottoritter.business.lotteries.Drawing;
import org.mongodb.morphia.annotations.Entity;

/**
 * @author Ulrich Cech
 */
@Entity(value = "drawings", noClassnameStored = false) // important to store the classname here
public class KenoDrawing extends Drawing {

    private static final long serialVersionUID = 1074584749875463174L;


    public KenoDrawing() {
        setLotteryIdentifier(KenoLottery.IDENTIFIER);
        setNumbers(new Integer[20]);
    }

    @Override
    public Integer[] getAdditionalNumbers() {
        return null;
    }

    @Override
    public void setAdditionalNumbers(Integer[] additionalNumbers) {
    }

}
