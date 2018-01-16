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
package de.lottoritter.presentation.security;

import de.lottoritter.business.player.entity.RoleId;
import de.lottoritter.business.player.control.UserSession;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Authentication Filter for the Web-Application. All pages under /sec/* are secured and need a valid logged in user.
 * If there is no user logged in, the user is redirected to the login page, and after successful login, the user
 * is further redirected to the original requested page.
 *
 * @author Ulrich Cech
 */
@WebFilter(filterName = "WebAuthenticationAdminFilter", urlPatterns = {"/admin/*"},
        dispatcherTypes={DispatcherType.REQUEST, DispatcherType.FORWARD})
public class WebAuthenticationAdminFilter implements Filter {

    @Inject
    Instance<UserSession> userSession;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }


    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        UserSession userSession = this.userSession.get();
        if (userSession.isLoggedIn() && userSession.getPlayer().hasRole(RoleId.ADMIN)) {
            chain.doFilter(request, response);
        } else {
            String contextPath = httpServletRequest.getContextPath();
            httpServletRequest.getSession().setAttribute("url", httpServletRequest.getRequestURI());
            httpServletResponse.sendRedirect(contextPath + "/login.xhtml");
        }
    }

    @Override
    public void destroy() {
    }

}
