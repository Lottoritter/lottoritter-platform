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
package de.lottoritter.business.security.control;

import de.lottoritter.platform.ResourceBundleRepository;
import de.lottoritter.platform.cdi.CDIBeanService;

import javax.ejb.ApplicationException;
import java.util.Locale;

/**
 * @author Ulrich Cech
 */
@ApplicationException
public class LoginFailedException extends RuntimeException {

    private static final long serialVersionUID = -377985735943962595L;

    private Locale locale;


    public LoginFailedException(Locale locale) {
        this.locale = locale;
    }


    @Override
    public String getMessage() {
        final ResourceBundleRepository resourceBundleRepository =
                CDIBeanService.getInstance().getCDIBean(ResourceBundleRepository.class);
        return resourceBundleRepository.getLocalized(LoginFailedException.class.getName(), "message", locale);
    }

}
