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

import de.lottoritter.business.lotteries.Drawing;
import org.mongodb.morphia.annotations.Entity;

import java.util.Objects;

/**
 * @author Ulrich Cech
 */
@Entity(value = "drawings", noClassnameStored = false) // important to store the classname here
public class German6aus49Drawing extends Drawing {

    private static final long serialVersionUID = -6409582982100826770L;

    private Integer superzahl;


    public German6aus49Drawing() {
        setLotteryIdentifier(German6aus49Lottery.IDENTIFIER);
        setNumbers(new Integer[6]);
    }


    public Integer getSuperzahl() {
        return superzahl;
    }

    public void setSuperzahl(Integer superzahl) {
        this.superzahl = superzahl;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof German6aus49Drawing)) return false;
        if (!super.equals(o)) return false;
        German6aus49Drawing that = (German6aus49Drawing) o;
        return Objects.equals(superzahl, that.superzahl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), superzahl);
    }
}
