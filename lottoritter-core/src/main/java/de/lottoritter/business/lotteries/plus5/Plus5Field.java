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
package de.lottoritter.business.lotteries.plus5;

import de.lottoritter.business.lotteries.Field;
import de.lottoritter.business.lotteries.MainTicket;
import org.mongodb.morphia.annotations.Transient;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
@XmlRootElement
public class Plus5Field extends Field {

    private static final long serialVersionUID = 6274579058275959623L;

    @Transient
    private MainTicket parentTicket;


    public Plus5Field() {
    }


    @Override
    public boolean isValid() {
        if ((getSelectedNumbers() == null) || ((getSelectedNumbers() != null) && (getSelectedNumbers().isEmpty()))) {
            return true;
        }
        return isValidFilled();
    }

    @Override
    public boolean isValidFilled() {
        if ((getSelectedNumbers() != null) && (getSelectedNumbers().size() == Plus5Lottery.SELECTABLE_NUMBERS)) {
            for (Integer selectedNumber : getSelectedNumbers()) {
                if ((selectedNumber == null) || (selectedNumber < 0) || (selectedNumber > Plus5Lottery.HIGHEST_SELECTABLE_NUMBER)) {
                    return false;
                }
            }
            final List<Integer> ticketNumber = Arrays.asList(parentTicket.getNumber());
            for (int i = 0; i < ticketNumber.size(); i++) {
                if (! Objects.equals(ticketNumber.get(i), getSelectedNumbers().get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void setParentTicket(MainTicket parentTicket) {
        this.parentTicket = parentTicket;
    }

    public MainTicket getParentTicket() {
        return parentTicket;
    }
}
