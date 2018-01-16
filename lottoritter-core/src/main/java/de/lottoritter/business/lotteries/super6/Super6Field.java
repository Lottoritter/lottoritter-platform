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
package de.lottoritter.business.lotteries.super6;

import javax.xml.bind.annotation.XmlRootElement;

import de.lottoritter.business.lotteries.Field;
import de.lottoritter.business.lotteries.MainTicket;
import org.mongodb.morphia.annotations.Transient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
@XmlRootElement
public class Super6Field extends Field {

    private static final long serialVersionUID = -9151839712172442704L;

    @Transient
    private MainTicket parentTicket;


    public Super6Field() {
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
        if ((getSelectedNumbers() != null) && (getSelectedNumbers().size() == Super6Lottery.SELECTABLE_NUMBERS)) {
            for (Integer selectedNumber : getSelectedNumbers()) {
                if ((selectedNumber == null) || (selectedNumber < 0) || (selectedNumber > Super6Lottery.HIGHEST_SELECTABLE_NUMBER)) {
                    return false;
                }
            }
            final List<Integer> ticketNumber = Arrays.asList(parentTicket.getNumber());
            for (int i = 1; i < ticketNumber.size(); i++) {
                // be aware, that the super6 number is the ticket number without the first number, so the index always minus 1
                if (! Objects.equals(ticketNumber.get(i), getSelectedNumbers().get(i-1))) {
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
