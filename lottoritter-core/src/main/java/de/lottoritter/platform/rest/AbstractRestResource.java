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
package de.lottoritter.platform.rest;

import de.lottoritter.business.configuration.control.Configurable;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import java.util.List;
import java.util.Locale;

/**
 * Abstract superclass for all REST-Resources.
 *
 * @author Ulrich Cech
 */
public abstract class AbstractRestResource {

    public static final String MEDIA_TYPE_JSON_STRING = "application/json;charset=UTF-8"; // have to be public
    public static final String MEDIA_TYPE_XML_STRING = "application/xml;charset=UTF-8"; // have to be public

    private static final MediaType MEDIA_TYPE_JSON = new MediaType("application", "json", "UTF-8");
    private static final MediaType MEDIA_TYPE_XML = new MediaType("application", "xml", "UTF-8");

    private static List<Variant> acceptableMediaTypes =
            Variant.mediaTypes(MEDIA_TYPE_JSON, MEDIA_TYPE_XML).build();


    @Context
    protected UriInfo info;

    @Context
    protected HttpHeaders httpHeaders;

    @Context
    protected HttpServletRequest httpServletRequest;

    @Context
    protected HttpServletResponse httpServletResponse;

    @Context
    protected Request request;

    @Inject
    @Configurable(value = "server_base_uri", defaultValue = "http://localhost:8080")
    protected Instance<String> configServerBaseUri;



    protected String getHeaderValue(final String headerKey) {
        return httpHeaders.getHeaderString(headerKey);
    }

    public Locale getRequestLocale() {
        return httpServletRequest.getLocale();
    }

    protected MediaType getNegotiatedMediaType() {
        final Variant selectedMediaType = request.selectVariant(acceptableMediaTypes);
        if (selectedMediaType == null) {
            return MEDIA_TYPE_JSON;
        }
        return selectedMediaType.getMediaType();
    }

}
