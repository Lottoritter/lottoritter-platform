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

import de.lottoritter.business.lotteries.Drawing;
import org.mongodb.morphia.annotations.Entity;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ulrich Cech
 */
@Entity(value = "drawings", noClassnameStored = false) // important to store the classname here
public class EuroJackpotDrawing extends Drawing {

    private static final long serialVersionUID = 206746938560382714L;

    private Integer[] additionalNumbers;


    public EuroJackpotDrawing() {
        setLotteryIdentifier(EuroJackpotLottery.IDENTIFIER);
        setNumbers(new Integer[5]);
        setAdditionalNumbers(new Integer[2]);
    }

    public String getAdditionalNumbersFormatted() {
        return Arrays.stream(additionalNumbers).map(String::valueOf).collect(Collectors.joining(", "));
    }


    @Override
    public Integer[] getAdditionalNumbers() {
        return additionalNumbers;
    }

    @Override
    public void setAdditionalNumbers(Integer[] additionalNumbers) {
        this.additionalNumbers = additionalNumbers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EuroJackpotDrawing)) return false;
        if (!super.equals(o)) return false;
        EuroJackpotDrawing that = (EuroJackpotDrawing) o;
        return Arrays.equals(additionalNumbers, that.additionalNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), additionalNumbers);
    }
}
