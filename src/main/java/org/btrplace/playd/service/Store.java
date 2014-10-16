package org.btrplace.playd.service;

import org.btrplace.playd.model.UseCase;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
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
        useCases.add(new UseCase("decommissioning", "deco", "", "namespace sandbox;\\n\\n" +
                "VM[1..8] : myVMs;\\n\\n" +
                ">>spread({VM1, VM5});\\n" +
                "ban(VM3, @N1);\\n" +
                ">>maxOnline(@N[1..8], 5);"));
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> list() {
        List<String> titles = new ArrayList<>(useCases.size());
        for (UseCase uc : useCases) {
            titles.add(uc.title());
        }
        return titles;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") String id) {
        for (UseCase uc : useCases) {
            if (uc.title().equals(id)) {
                return Response.ok(uc.toJson()).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
