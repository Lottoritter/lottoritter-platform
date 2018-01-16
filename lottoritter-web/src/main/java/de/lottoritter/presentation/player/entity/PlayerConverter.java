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
package de.lottoritter.presentation.player.entity;

import de.lottoritter.business.player.control.UserRepository;
import de.lottoritter.business.player.entity.Player;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ulrich Cech
 */
@Named
public class PlayerConverter implements Converter {

    @Inject
    UserRepository playerRepository;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if ((value != null) && (value.trim().length() > 0)) {
            return playerRepository.findById(value);
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            return String.valueOf(((Player) value).getId());
        }
        else {
            return null;
        }
    }
}
