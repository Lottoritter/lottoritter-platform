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

import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotField;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Field;
import org.mongodb.morphia.annotations.Embedded;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Christopher Schmidt
 */
@XmlTransient //Prevents the mapping of a JavaBean property/type to XML representation
@XmlSeeAlso({German6aus49Field.class, EuroJackpotField.class})
public abstract class Field implements Serializable {

    private static final long serialVersionUID = -4856731354138493462L;

    @XmlElement
    private int fieldNumber;

    @Embedded
    @XmlElement
    private List<Integer> selectedNumbers = new ArrayList<>();

    @Embedded
    @XmlElement
    private List<Integer> selectedAdditionalNumbers = new ArrayList<>();


    public Field() {}

    public Field(int fieldNumber, Integer[] newNumbers) {
        this.fieldNumber = fieldNumber;
        selectedNumbers = Arrays.asList(newNumbers);
    }

    public Field(int fieldNumber, Integer[] newNumbers, Integer[] newAdditionalNumbers) {
        this.fieldNumber = fieldNumber;
        selectedNumbers = Arrays.asList(newNumbers);
        selectedAdditionalNumbers = Arrays.asList(newAdditionalNumbers);
        Collections.sort(selectedNumbers);
    }

    public abstract boolean isValid();

    public abstract boolean isValidFilled();

    public int getFieldNumber() {
        return fieldNumber;
    }

    public List<Integer> getSelectedNumbers() {
        return selectedNumbers;
    }

    public void setSelectedNumbers(List<Integer> selectedNumbers) {
        this.selectedNumbers = selectedNumbers;
    }

    public List<Integer> getSelectedAdditionalNumbers() {
        return selectedAdditionalNumbers;
    }

    public void setSelectedAdditionalNumbers(List<Integer> selectedAdditionalNumbers) {
        this.selectedAdditionalNumbers = selectedAdditionalNumbers;
    }
}
