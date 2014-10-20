package org.btrplace.playd.service;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.ext.Provider;

/**
 * @author Fabien Hermenier
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(final ContainerRequest requestContext,
                       final ContainerResponse cres) {
        cres.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
        cres.getHttpHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        cres.getHttpHeaders().add("Access-Control-Allow-Credentials", "true");
        cres.getHttpHeaders().add("Access-Control-Allow-Methods", "POST GET OPTIONS");
        cres.getHttpHeaders().add("Access-Control-Max-Age", "1209600");
        return cres;
    }

}
