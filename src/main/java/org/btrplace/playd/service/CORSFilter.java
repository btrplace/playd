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
                .header("Access-Control-Allow-Methods", "API, GET, POST, PUT, UPDATE, OPTIONS")
                .header("Access-Control-Max-Age", "151200")
                .header("Access-Control-Allow-Headers", "x-requested-by, x-requested-with, content-type, accept");

        String crunchifyRequestHeader = req.getHeaderValue("Access-Control-Request-Headers");

        if (null != crunchifyRequestHeader) {
            System.out.println("allowed headers: " + crunchifyRequestHeader);
            crunchifyResponseBuilder.header("Access-Control-Allow-Headers", crunchifyRequestHeader);
        } else {
            System.out.println("No fancy headers");
        }

        cres.setResponse(crunchifyResponseBuilder.build());
        return cres;
    }

}
