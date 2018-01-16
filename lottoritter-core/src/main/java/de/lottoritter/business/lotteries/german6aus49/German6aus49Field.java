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

import de.lottoritter.business.lotteries.Field;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ulrich Cech
 */
@XmlRootElement
public class German6aus49Field extends Field {

    private static final long serialVersionUID = 6533658263751895017L;


    public German6aus49Field() {
    }

    public German6aus49Field(int fieldNumber, Integer[] newNumbers) {
        super(fieldNumber, newNumbers);
    }


    public boolean isValid() {
        if ((getSelectedNumbers() == null) || ((getSelectedNumbers() != null) && (getSelectedNumbers().isEmpty()))) {
            return true;
        }
        return isValidFilled();
    }

    @Override
    public boolean isValidFilled() {
        if ((getSelectedNumbers() != null) && (getSelectedNumbers().size() == German6aus49Lottery.SELECTABLE_NUMBERS)) {
            for (Integer selectedNumber : getSelectedNumbers()) {
                if (selectedNumber < 1 || selectedNumber > German6aus49Lottery.HIGHEST_SELECTABLE_NUMBER) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
