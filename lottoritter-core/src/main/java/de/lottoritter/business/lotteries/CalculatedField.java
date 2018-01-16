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

/**
 * @author Ulrich Cech
 */
public class CalculatedField {

    private Field field;
    private List<CalculatedNumber> numbers = new ArrayList<>();
    private List<CalculatedNumber> additionalNumbers = new ArrayList<>();
    private WinningRank winningRank;

    public CalculatedField(Field field) {
        this.field = field;
    }


    public void addCalculatedNumber(CalculatedNumber numberWrapper) {
        numbers.add(numberWrapper);
    }

    public void addCalculatedAdditionalNumber(CalculatedNumber numberWrapper) {
        additionalNumbers.add(numberWrapper);
    }

    public Field getField() {
        return field;
    }

    public List<CalculatedNumber> getNumbers() {
        return numbers;
    }

    public List<CalculatedNumber> getAdditionalNumbers() {
        return additionalNumbers;
    }

    public WinningRank getWinningRank() {
        return winningRank;
    }

    public void setWinningRank(WinningRank winningRank) {
        this.winningRank = winningRank;
    }
}
