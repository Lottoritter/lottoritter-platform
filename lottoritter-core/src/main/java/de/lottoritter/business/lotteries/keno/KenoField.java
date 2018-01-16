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

import de.lottoritter.business.lotteries.Field;

import javax.xml.bind.annotation.XmlRootElement;

import static de.lottoritter.business.lotteries.keno.KenoLottery.HIGHEST_SELECTABLE_NUMBER;
import static de.lottoritter.business.lotteries.keno.KenoLottery.MAX_SELECTABLE_NUMBERS;
import static de.lottoritter.business.lotteries.keno.KenoLottery.MIN_SELECTABLE_NUMBERS;


/**
 * @author Ulrich Cech
 */
@XmlRootElement
public class KenoField extends Field {

    private static final long serialVersionUID = 3800461884679833020L;

    public KenoField() {
    }

    public KenoField(int fieldNumber, Integer[] newNumbers) {
        super(fieldNumber, newNumbers);
    }

    public boolean isValid() {
        return normalNumbersValid();
    }

    private boolean normalNumbersValid() {
        if ((getSelectedNumbers() == null) || ((getSelectedNumbers() != null) && (getSelectedNumbers().isEmpty()))) {
            return true;
        }
        return normalNumbersValidFilled();
    }

    @Override
    public boolean isValidFilled() {
        return normalNumbersValidFilled();
    }

    private boolean normalNumbersValidFilled() {
        if ((getSelectedNumbers() != null) && (getSelectedNumbers().size() <= MAX_SELECTABLE_NUMBERS && getSelectedNumbers().size() >= MIN_SELECTABLE_NUMBERS)) {
            for (Integer selectedNumber : getSelectedNumbers()) {
                if (selectedNumber < 1 || selectedNumber > HIGHEST_SELECTABLE_NUMBER) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

}
