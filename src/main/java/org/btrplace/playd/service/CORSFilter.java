package org.btrplace.playd.service;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * @author Fabien Hermenier
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(final ContainerRequest req, final ContainerResponse cres) {
        Response.ResponseBuilder crunchifyResponseBuilder = Response.fromResponse(cres.getResponse());

        // *(allow from all servers) OR http://crunchify.com/ OR http://example.com/
        crunchifyResponseBuilder.header("Access-Control-Allow-Origin", "*")
                // As a part of the response to a request, which HTTP methods can be used during the actual request.
                .header("Access-Control-Allow-Methods", "API, GET, POST, PUT, UPDATE, OPTIONS")
                        // How long the results of a request can be cached in a result cache.
                .header("Access-Control-Max-Age", "151200")
                        // As part of the response to a request, which HTTP headers can be used during the actual request.
                .header("Access-Control-Allow-Headers", "x-requested-with,Content-Type");

        String crunchifyRequestHeader = req.getHeaderValue("Access-Control-Request-Headers");

        if (null != crunchifyRequestHeader && !crunchifyRequestHeader.equals(null)) {
            crunchifyResponseBuilder.header("Access-Control-Allow-Headers", crunchifyRequestHeader);
        }

        cres.setResponse(crunchifyResponseBuilder.build());
        return cres;
    }

}
