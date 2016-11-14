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

    public CORSFilter() {
        System.err.println("Hop");
    }
    @Override
    public ContainerResponse filter(final ContainerRequest req, final ContainerResponse cres) {
        Response.ResponseBuilder resp = Response.fromResponse(cres.getResponse());
        resp.header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

        String reqHead = req.getHeaderValue("Access-Control-Request-Headers");
        System.out.println("Head:" + reqHead);
        if(null != reqHead && !reqHead.equals("")){
            resp.header("Access-Control-Allow-Headers", reqHead);
        }

        cres.setResponse(resp.build());
        return cres;
    }

}
