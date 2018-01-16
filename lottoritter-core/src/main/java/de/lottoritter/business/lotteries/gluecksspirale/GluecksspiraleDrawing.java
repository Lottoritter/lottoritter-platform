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
package de.lottoritter.business.lotteries.gluecksspirale;

import de.lottoritter.business.lotteries.Drawing;
import org.mongodb.morphia.annotations.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ulrich Cech
 */
@Entity(value = "drawings", noClassnameStored = false) // important to store the classname here
public class GluecksspiraleDrawing extends Drawing {

    public static class NumberForRank {
        private int rank;
        private Integer[][] numbers = new Integer[2][];


        public NumberForRank() {
        }

        public NumberForRank(int rank) {
            this.rank = rank;
        }


        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public Integer[][] getNumbers() {
            return numbers;
        }

        public void setNumbers(Integer[][] numbers) {
            this.numbers = numbers;
        }
    }


    private static final long serialVersionUID = -5925461479134434971L;

    private List<NumberForRank> gluecksspiraleNumbers = new ArrayList<>(7);


    public GluecksspiraleDrawing() {
        setLotteryIdentifier(GluecksspiraleLottery.IDENTIFIER);
        for (int i = 0; i < 7; i++) {
            gluecksspiraleNumbers.add(new NumberForRank(i + 1));
        }
    }

    public void addGluecksspiraleNumber(int rank, int position, String number) {
        char[] numberAsChar = new char[number.length()];
        number.getChars(0, number.length(), numberAsChar, 0);
        Integer[] numbers = new Integer[numberAsChar.length];
        for (int i = 0; i < numberAsChar.length; i++) {
            numbers[i] = Integer.valueOf("" + numberAsChar[i]);
        }
        if ((gluecksspiraleNumbers.size() < rank) || (gluecksspiraleNumbers.get(rank - 1) == null)) {
            gluecksspiraleNumbers.add(rank-1, new NumberForRank(rank));
        }
        gluecksspiraleNumbers.get(rank-1).getNumbers()[position-1] = numbers;
    }

    public Integer[] getGluecksspiraleNumber(int rank, int position) {
        return gluecksspiraleNumbers.get(rank-1).getNumbers()[position-1];
    }

    public String getGluecksspiraleNumbersFormatted(int rank, int position) {
        return Arrays.toString(gluecksspiraleNumbers.get(rank-1).getNumbers()[position-1]);
    }

    public List<NumberForRank> getGluecksspiraleNumbers() {
        return gluecksspiraleNumbers;
    }
}
