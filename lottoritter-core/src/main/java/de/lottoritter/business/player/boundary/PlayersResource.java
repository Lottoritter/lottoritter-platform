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
package de.lottoritter.business.player.boundary;

import de.lottoritter.business.player.control.UserRepository;
import de.lottoritter.platform.rest.AbstractRestResource;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * REST-Resource for player.
 *
 * @author Ulrich Cech
 */
@Path("player")
@Produces({AbstractRestResource.MEDIA_TYPE_JSON_STRING, AbstractRestResource.MEDIA_TYPE_XML_STRING})
@Consumes({AbstractRestResource.MEDIA_TYPE_JSON_STRING, AbstractRestResource.MEDIA_TYPE_XML_STRING})
@RequestScoped
public class PlayersResource extends AbstractRestResource {

    @Inject
    UserRepository userRepository;


    public PlayersResource() {
    }


    /**
     * Activates the user account after registration.<br/>
     * The GET is important, so that the user can simple click on the link to activate the account.
     *
     * @param code the activation code from the registration process
     */
    @GET
    @Path("activation")
    public Response activateAccount(@QueryParam("code") final String code) {
        if (!userRepository.activateAccount(code)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return buildRedirectResponse(configServerBaseUri.get() + "/regactsuccess.xhtml");
    }
    
    @GET
    @Path("emailapproved")
    public Response approveEmail(@QueryParam("code") final String code) {
        if (!userRepository.approveEmail(code)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return buildRedirectResponse(configServerBaseUri.get() + "/regactsuccess.xhtml"); // TODO: cschmidt: add new facelet for this   
    }

    private Response buildRedirectResponse(final String uriString) {
        Response response = null;
        try {
            response = Response.temporaryRedirect(new URI(uriString)).build();
        } catch (URISyntaxException ignore) {}
        return response;
    }

}
