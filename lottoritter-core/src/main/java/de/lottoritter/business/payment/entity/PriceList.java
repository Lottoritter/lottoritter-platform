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
package de.lottoritter.business.payment.entity;

import de.lottoritter.business.temporal.entity.ZonedDateTimeEurope;
import de.lottoritter.platform.persistence.PersistentEntity;
import org.mongodb.morphia.annotations.Entity;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
@Entity(value = "pricelist", noClassnameStored = true)
public class PriceList extends PersistentEntity {

    private static final long serialVersionUID = 1604333801105805318L;

    private String lotteryIdentifier;

    private int pricePerField;

    private int feeFirstDrawing;

    private int feeSecondDrawing;

    private int feeGluecksspirale;

    private int feePlus5;

    private int priceSuper6;

    private int priceSpiel77;

    private int priceGluecksspirale;

    private int pricePlus5;

    @ZonedDateTimeEurope
    private ZonedDateTime validFrom;


    public PriceList() {
    }


    public String getLotteryIdentifier() {
        return lotteryIdentifier;
    }

    public void setLotteryIdentifier(String lotteryIdentifier) {
        this.lotteryIdentifier = lotteryIdentifier;
    }

    public int getPricePerField() {
        return pricePerField;
    }

    public void setPricePerField(int pricePerField) {
        this.pricePerField = pricePerField;
    }

    public int getFeeFirstDrawing() {
        return feeFirstDrawing;
    }

    public void setFeeFirstDrawing(int feeFirstDrawing) {
        this.feeFirstDrawing = feeFirstDrawing;
    }

    public int getFeeSecondDrawing() {
        return feeSecondDrawing;
    }

    public void setFeeSecondDrawing(int feeSecondDrawing) {
        this.feeSecondDrawing = feeSecondDrawing;
    }

    public int getPriceSuper6() {
        return priceSuper6;
    }

    public void setPriceSuper6(int priceSuper6) {
        this.priceSuper6 = priceSuper6;
    }

    public int getPriceSpiel77() {
        return priceSpiel77;
    }

    public void setPriceSpiel77(int priceSpiel77) {
        this.priceSpiel77 = priceSpiel77;
    }

    public int getPriceGluecksspirale() {
        return priceGluecksspirale;
    }

    public void setPriceGluecksspirale(int priceGluecksspirale) {
        this.priceGluecksspirale = priceGluecksspirale;
    }

    public int getFeeGluecksspirale() {
        return feeGluecksspirale;
    }

    public void setFeeGluecksspirale(int feeGluecksspirale) {
        this.feeGluecksspirale = feeGluecksspirale;
    }

    public int getFeePlus5() {
        return feePlus5;
    }

    public void setFeePlus5(int feePlus5) {
        this.feePlus5 = feePlus5;
    }

    public int getPricePlus5() {
        return pricePlus5;
    }

    public void setPricePlus5(int pricePlus5) {
        this.pricePlus5 = pricePlus5;
    }

    public ZonedDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(ZonedDateTime validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PriceList)) return false;
        if (!super.equals(o)) return false;
        PriceList priceList = (PriceList) o;
        return pricePerField == priceList.pricePerField &&
                feeFirstDrawing == priceList.feeFirstDrawing &&
                feeSecondDrawing == priceList.feeSecondDrawing &&
                feeGluecksspirale == priceList.feeGluecksspirale &&
                feePlus5 == priceList.feePlus5 &&
                priceSuper6 == priceList.priceSuper6 &&
                priceSpiel77 == priceList.priceSpiel77 &&
                priceGluecksspirale == priceList.priceGluecksspirale &&
                pricePlus5 == priceList.pricePlus5 &&
                Objects.equals(lotteryIdentifier, priceList.lotteryIdentifier) &&
                Objects.equals(validFrom, priceList.validFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lotteryIdentifier, pricePerField, feeFirstDrawing, feeSecondDrawing,
                feeGluecksspirale, feePlus5, priceSuper6, priceSpiel77, priceGluecksspirale, pricePlus5, validFrom);
    }
}
