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

import de.lottoritter.business.lotteries.Field;

import javax.xml.bind.annotation.XmlRootElement;

import static de.lottoritter.business.lotteries.eurojackpot.EuroJackpotLottery.HIGHEST_SELECTABLE_ADDITIONAL_NUMBER;
import static de.lottoritter.business.lotteries.eurojackpot.EuroJackpotLottery.HIGHEST_SELECTABLE_NUMBER;
import static de.lottoritter.business.lotteries.eurojackpot.EuroJackpotLottery.SELECTABLE_ADDITIONAL_NUMBERS;
import static de.lottoritter.business.lotteries.eurojackpot.EuroJackpotLottery.SELECTABLE_NUMBERS;

/**
 * @author Christopher Schmidt
 */
@XmlRootElement
public class EuroJackpotField extends Field {

    private static final long serialVersionUID = 4951199183554337639L;


    public EuroJackpotField() {
    }

    public EuroJackpotField(int fieldNumber, Integer[] newNumbers, Integer[] additionalNumbers) {
        super(fieldNumber, newNumbers, additionalNumbers);
    }

    public boolean isValid() {
        return normalNumbersValid() && additionalNumbersValid();
    }

    private boolean additionalNumbersValid() {
        if ((getSelectedAdditionalNumbers() == null) || ((getSelectedAdditionalNumbers() != null) && (getSelectedAdditionalNumbers().isEmpty()))) {
            return true;
        }
        return additionalNumbersValidFilled();
    }

    private boolean normalNumbersValid() {
        if ((getSelectedNumbers() == null) || ((getSelectedNumbers() != null) && (getSelectedNumbers().isEmpty()))) {
            return true;
        }
        return normalNumbersValidFilled();
    }

    @Override
    public boolean isValidFilled() {
        return normalNumbersValidFilled() && additionalNumbersValidFilled();
    }

    private boolean additionalNumbersValidFilled() {
        if ((getSelectedAdditionalNumbers() != null) && (getSelectedAdditionalNumbers().size() == SELECTABLE_ADDITIONAL_NUMBERS)) {
            for (Integer selectedNumber : getSelectedAdditionalNumbers()) {
                if (selectedNumber < 1 || selectedNumber > HIGHEST_SELECTABLE_ADDITIONAL_NUMBER) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean normalNumbersValidFilled() {
        if ((getSelectedNumbers() != null) && (getSelectedNumbers().size() == SELECTABLE_NUMBERS)) {
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
