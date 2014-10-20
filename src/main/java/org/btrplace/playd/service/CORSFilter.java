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
    public ContainerResponse filter(final ContainerRequest requestContext, final ContainerResponse cres) {
        cres.getHttpHeaders().add("Access-Control-Allow-Origin", "*");
        cres.getHttpHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        cres.getHttpHeaders().add("Access-Control-Max-Age", "1209600");

        String reqHead = requestContext.getHeaderValue("Access-Control-Request-Headers");
        if(null != reqHead && !reqHead.equals("")){
            cres.getHttpHeaders().add("Access-Control-Allow-Headers", reqHead);
        }

        return cres;
    }

}
