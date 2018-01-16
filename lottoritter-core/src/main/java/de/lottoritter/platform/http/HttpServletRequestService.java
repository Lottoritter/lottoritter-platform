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
package de.lottoritter.platform.http;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * A service for HttpServletRequest.
 *
 * @author Ulrich Cech
 */
@RequestScoped
public class HttpServletRequestService {


    @Inject
    HttpServletRequest httpServletRequest;


    public String getIpAddressFromRequest() {
        String ipAddress = httpServletRequest.getHeader("X-Forwarded-For");
        if (ipAddress != null) {
            ipAddress = ipAddress.split("\\s*,\\s*", 2)[0];
        } else {
            ipAddress = httpServletRequest.getRemoteAddr();
        }
        return ipAddress;
    }

    public String getUserAgentFromRequest() {
        String userAgent = httpServletRequest.getHeader("user-agent");
        if (userAgent == null) {
            userAgent = httpServletRequest.getRemoteAddr();
        }
        return userAgent;
    }


}
