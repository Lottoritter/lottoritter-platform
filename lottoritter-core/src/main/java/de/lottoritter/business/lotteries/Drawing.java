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
import java.util.Arrays;
import java.util.Objects;

import de.lottoritter.business.temporal.entity.ZonedDateTimeEurope;
import org.mongodb.morphia.annotations.Entity;

import de.lottoritter.platform.persistence.PersistentEntity;

/**
 * @author Ulrich Cech
 */
@Entity(value = "drawings", noClassnameStored = false) // important to store the classname here
public class Drawing extends PersistentEntity {
    private static final long serialVersionUID = -4289114311120738228L;

    private String lotteryIdentifier;

    @ZonedDateTimeEurope
    private ZonedDateTime date;

    private Integer[] numbers;

    private Integer[] additionalNumbers;


    public Drawing() {
    }


    public String getNumbersFormatted() {
        return Arrays.toString(getNumbers());
    }

    public boolean hasAdditionalNumbers() {
        return (additionalNumbers != null) && (additionalNumbers.length > 1);
    }


    public String getLotteryIdentifier() {
        return lotteryIdentifier;
    }

    public void setLotteryIdentifier(String lotteryIdentifier) {
        this.lotteryIdentifier = lotteryIdentifier;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public Integer[] getNumbers() {
        return numbers;
    }

    public void setNumbers(Integer[] numbers) {
        this.numbers = numbers;
    }

    public Integer[] getAdditionalNumbers() {
        return additionalNumbers;
    }

    public void setAdditionalNumbers(Integer[] additionalNumbers) {
        this.additionalNumbers = additionalNumbers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Drawing)) return false;
        if (!super.equals(o)) return false;
        Drawing drawing = (Drawing) o;
        return Objects.equals(lotteryIdentifier, drawing.lotteryIdentifier) &&
                Objects.equals(date, drawing.date) &&
                Arrays.equals(numbers, drawing.numbers) &&
                Arrays.equals(additionalNumbers, drawing.additionalNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lotteryIdentifier, date, numbers, additionalNumbers);
    }
}
