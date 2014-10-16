package org.btrplace.playd.service;

import org.btrplace.playd.model.UseCase;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
@Path("/store")
public class Store {

    private List<UseCase> useCases;

    public Store() {
        useCases = new ArrayList<>();
        //Mock
        useCases.add(new UseCase("a2dfLx", "decommissioning", "deco", "", "namespace sandbox;\\n\\n" +
                "VM[1..8] : myVMs;\\n\\n" +
                ">>spread({VM1, VM5});\\n" +
                "ban(VM3, @N1);\\n" +
                ">>maxOnline(@N[1..8], 5);"));
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list() {
        StringBuilder res = new StringBuilder("[");
        for (Iterator<UseCase> ite = useCases.iterator(); ite.hasNext(); ) {
            res.append(ite.next().summary());
            if (ite.hasNext()) {
                res.append(",");
            }
        }
        return Response.ok(res.append("]").toString()).build();
    }

    @GET
    @Path("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("key") String k) {
        for (UseCase uc : useCases) {
            if (uc.key().equals(k)) {
                return Response.ok(uc.toJson()).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
