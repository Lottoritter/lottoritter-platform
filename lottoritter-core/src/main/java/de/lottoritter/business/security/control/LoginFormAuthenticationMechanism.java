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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.AutoApplySession;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.authentication.mechanism.http.LoginToContinue;
import javax.security.enterprise.authentication.mechanism.http.RememberMe;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Ulrich Cech
 */
@AutoApplySession // For "Is user already logged-in?"
@RememberMe(
        cookieMaxAgeSeconds = 60 * 60 * 24 * 14, // 14 days
        cookieSecureOnly = false, // set to true if login uses HTTPS
        isRememberMeExpression = "#{self.isRememberMe(httpMessageContext)}"
)
@LoginToContinue(
        loginPage = "/login.xhtml?continue=true",
        errorPage = "",
        useForwardToLogin = false
)
@ApplicationScoped
public class LoginFormAuthenticationMechanism implements HttpAuthenticationMechanism {

    @Inject
    IdentityStore identityStore;


    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response,
                                                HttpMessageContext httpMessageContext) throws AuthenticationException {
        Credential credential = httpMessageContext.getAuthParameters().getCredential();
        if (credential != null) {
            return httpMessageContext.notifyContainerAboutLogin(this.identityStore.validate(credential));
        } else {
            return httpMessageContext.doNothing();
        }
    }

    // this was called on @RememberMe annotations
    public Boolean isRememberMe(HttpMessageContext httpMessageContext) {
        return httpMessageContext.getAuthParameters().isRememberMe();
    }

    // this Workaround is needed for Weld bug; at least in Weld 2.3.2, because default methods are not intercepted
//    @Override
//    public void cleanSubject(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
//        HttpAuthenticationMechanism.super.cleanSubject(request, response, httpMessageContext);
//    }
}
