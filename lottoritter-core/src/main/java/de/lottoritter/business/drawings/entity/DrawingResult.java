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
package de.lottoritter.business.drawings.entity;

import de.lottoritter.business.lotteries.Drawing;
import de.lottoritter.business.lotteries.CalculatedField;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.platform.persistence.PersistentEntity;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
@Entity(value = "drawingResults", noClassnameStored = true)
public class DrawingResult extends PersistentEntity {
    private static final long serialVersionUID = -6037568783534748470L;

    private Drawing drawing;

    private Ticket ticket;

    @Embedded
    private List<CalculatedField> wonFields = new ArrayList<>();


    public DrawingResult() {
    }

    public DrawingResult(Drawing drawing, Ticket ticket, List<CalculatedField> wonFields) {
        this.drawing = drawing;
        this.ticket = ticket;
        this.wonFields = wonFields;
        this.ticket.setOmitValidation(true);
    }


    public Drawing getDrawing() {
        return drawing;
    }

    public void setDrawing(Drawing drawing) {
        this.drawing = drawing;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public List<CalculatedField> getWonFields() {
        return wonFields;
    }

    public void setWonFields(List<CalculatedField> wonFields) {
        this.wonFields = wonFields;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DrawingResult)) return false;
        if (!super.equals(o)) return false;
        DrawingResult that = (DrawingResult) o;
        return Objects.equals(drawing, that.drawing) &&
                Objects.equals(ticket, that.ticket) &&
                Objects.equals(wonFields, that.wonFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), drawing, ticket, wonFields);
    }
}
