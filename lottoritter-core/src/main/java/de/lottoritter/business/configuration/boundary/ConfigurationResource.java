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
package de.lottoritter.business.configuration.boundary;

import de.lottoritter.business.configuration.control.ConfigurationController;
import de.lottoritter.business.player.entity.RoleId;
import de.lottoritter.platform.rest.AbstractRestResource;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author Ulrich Cech
 */
@Path("platform/configuration")
@Produces({AbstractRestResource.MEDIA_TYPE_JSON_STRING, AbstractRestResource.MEDIA_TYPE_XML_STRING})
@Consumes({AbstractRestResource.MEDIA_TYPE_JSON_STRING, AbstractRestResource.MEDIA_TYPE_XML_STRING})
@RequestScoped
public class ConfigurationResource extends AbstractRestResource {

    @Inject
    ConfigurationController configurationController;


    public ConfigurationResource() {
    }


    /**
     * Triggers the configuration to update
     */
    @GET
    @Path("refresh")
    @RolesAllowed({RoleId.ADMIN_STRING})
    public Response refreshDatabaseConfiguration() {
        configurationController.refreshConfiguration();
        return Response.ok().build();
    }

}
