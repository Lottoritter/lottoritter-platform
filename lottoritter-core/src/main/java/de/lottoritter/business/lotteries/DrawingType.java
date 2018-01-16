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

import java.time.ZonedDateTime;

/**
 * @author Ulrich Cech
 */
public enum DrawingType {
    GERMAN6AUS49WE("german6aus49we", new Integer[]{0, 0, 1, 0, 0, 0, 0}),
    GERMAN6AUS49SA("german6aus49sa", new Integer[]{0, 0, 0, 0, 0, 1, 0}),
    GERMAN6AUS49WESA("german6aus49wesa", new Integer[]{0, 0, 1, 0, 0, 1, 0}),
    EUROJACKPOTFR("eurojackpotfr", new Integer[]{0, 0, 0, 0, 1, 0, 0}),
    GLUECKSSPIRALESA("gluecksspiralesa", new Integer[]{0, 0, 0, 0, 0, 1, 0});


    private String typeAsString;

    private Integer[] weekDaysOfDrawing;


    DrawingType(String typeAsString, Integer[] weekDaysOfDrawing) {
        this.typeAsString = typeAsString;
        this.weekDaysOfDrawing = weekDaysOfDrawing;
    }

    public String getTypeAsString() {
        return typeAsString;
    }

    public Integer[] getWeekDaysOfDrawing() {
        return weekDaysOfDrawing;
    }

    public static DrawingType fromType(String type) {
        for (DrawingType drawingType : DrawingType.values()) {
            if (drawingType.getTypeAsString().equals(type)) {
                return drawingType;
            }
        }
        return null;
    }

    public static boolean isDrawingTypeForGermanGluecksspirale(String typeAsString) {
        DrawingType drawingType = fromType(typeAsString);
        return (drawingType == GERMAN6AUS49SA) || (drawingType == GERMAN6AUS49WESA);
    }

    public static boolean isValid(String drawing) {
        return (drawing != null) && (fromType(drawing) != null);
    }

    public boolean isValidForDate(ZonedDateTime date) {
        return getWeekDaysOfDrawing()[date.getDayOfWeek().getValue() - 1] != 0;
    }

}
