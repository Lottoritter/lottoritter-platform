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

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

import de.lottoritter.business.lotteries.german6aus49.German6aus49Lottery;

/**
 * @author Ulrich Cech
 */
@Singleton
public class DrawingTypeManager {


    // german6aus49
    private static final Integer[] GERMAN6aus49WE = new Integer[] {0,0,1,0,0,0,0};
    private static final Integer[] GERMAN6aus49SA = new Integer[] {0,0,0,0,0,1,0};
    private static final Integer[] GERMAN6aus49WESA = new Integer[] {0,0,1,0,0,1,0};

    private static Map<String, Integer[]> drawingTypeCache = new TreeMap<>();


    @PostConstruct
    private void init() {
        drawingTypeCache.put(German6aus49Lottery.IDENTIFIER + "we", GERMAN6aus49WE);
        drawingTypeCache.put(German6aus49Lottery.IDENTIFIER + "sa", GERMAN6aus49SA);
        drawingTypeCache.put(German6aus49Lottery.IDENTIFIER + "wesa", GERMAN6aus49WESA);
    }


    public Integer[] getDrawingTimeStructureFromString(String drawingTypeAsString) {
        return drawingTypeCache.get(drawingTypeAsString);
    }


}
