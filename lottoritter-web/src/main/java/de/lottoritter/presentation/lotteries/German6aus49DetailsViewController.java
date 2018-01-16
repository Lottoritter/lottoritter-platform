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
package de.lottoritter.presentation.lotteries;

import de.lottoritter.business.lotteries.DrawingType;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.platform.ResourceBundleRepository;
import de.lottoritter.presentation.AbstractViewController;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;

/**
 * @author Christopher Schmidt
 */
@Named
@ViewScoped
public class German6aus49DetailsViewController extends AbstractViewController implements Serializable {

    private static final long serialVersionUID = -1826300121978408247L;

    private ResourceBundleRepository resourceBundleRepository;


    public German6aus49DetailsViewController() {
    }


    @PostConstruct
    private void init() {
        resourceBundleRepository = new ResourceBundleRepository();
    }

    public String getDrawingDays(MainTicket ticket) {
        DrawingType type = DrawingType.fromType(ticket.getDrawingType());
        if (type != null) {
            if (type.equals(DrawingType.GERMAN6AUS49WE)) {
                return resourceBundleRepository.getLocalized(DrawingType.GERMAN6AUS49WE, Locale.GERMAN);
            }
            if (type.equals(DrawingType.GERMAN6AUS49SA)) {
                return resourceBundleRepository.getLocalized(DrawingType.GERMAN6AUS49SA, Locale.GERMAN);
            }
            if (type.equals(DrawingType.GERMAN6AUS49WESA)) {
                return resourceBundleRepository.getLocalized(DrawingType.GERMAN6AUS49WESA, Locale.GERMAN);
            }
        }
        return null;
    }
}
