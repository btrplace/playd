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

        String model = "{\\\"mapping\\\":{\\\"onlineNodes\\\":{\\\"0\\\":{\\\"runningVMs\\\":[],\\\"sleepingVMs\\\":[]},\\\"1\\\":{\\\"runningVMs\\\":[],\\\"sleepingVMs\\\":[]},\\\"2\\\":{\\\"runningVMs\\\":[2],\\\"sleepingVMs\\\":[]},\\\"3\\\":{\\\"runningVMs\\\":[4],\\\"sleepingVMs\\\":[]},\\\"4\\\":{\\\"runningVMs\\\":[],\\\"sleepingVMs\\\":[]},\\\"5\\\":{\\\"runningVMs\\\":[0,7],\\\"sleepingVMs\\\":[]},\\\"6\\\":{\\\"runningVMs\\\":[5,6],\\\"sleepingVMs\\\":[]},\\\"7\\\":{\\\"runningVMs\\\":[1,3],\\\"sleepingVMs\\\":[]}},\\\"offlineNodes\\\":[],\\\"readyVMs\\\":[]},\\\"views\\\":[{\\\"id\\\":\\\"shareableResource\\\",\\\"rcId\\\":\\\"cpu\\\",\\\"nodes\\\":{\\\"0\\\":6,\\\"1\\\":6,\\\"2\\\":6,\\\"3\\\":6,\\\"4\\\":6,\\\"5\\\":6,\\\"6\\\":6,\\\"7\\\":6},\\\"vms\\\":{\\\"0\\\":2,\\\"1\\\":3,\\\"2\\\":3,\\\"3\\\":2,\\\"4\\\":2,\\\"5\\\":3,\\\"6\\\":2,\\\"7\\\":2},\\\"defCapacity\\\":6,\\\"defConsumption\\\":6},{\\\"id\\\":\\\"shareableResource\\\",\\\"rcId\\\":\\\"mem\\\",\\\"nodes\\\":{\\\"0\\\":6,\\\"1\\\":6,\\\"2\\\":6,\\\"3\\\":6,\\\"4\\\":6,\\\"5\\\":6,\\\"6\\\":6,\\\"7\\\":6},\\\"vms\\\":{\\\"0\\\":2,\\\"1\\\":3,\\\"2\\\":2,\\\"3\\\":2,\\\"4\\\":3,\\\"5\\\":2,\\\"6\\\":2,\\\"7\\\":2},\\\"defCapacity\\\":6,\\\"defConsumption\\\":6},{\\\"id\\\":\\\"ns\\\",\\\"type\\\":\\\"vm\\\",\\\"map\\\":{\\\"sandbox.VM1\\\":0,\\\"sandbox.VM2\\\":1,\\\"sandbox.VM3\\\":2,\\\"sandbox.VM4\\\":3,\\\"sandbox.VM5\\\":4,\\\"sandbox.VM6\\\":5,\\\"sandbox.VM7\\\":6,\\\"sandbox.VM8\\\":7}},{\\\"id\\\":\\\"ns\\\",\\\"type\\\":\\\"node\\\",\\\"map\\\":{\\\"@N1\\\":0,\\\"@N2\\\":1,\\\"@N3\\\":2,\\\"@N4\\\":3,\\\"@N5\\\":4,\\\"@N6\\\":5,\\\"@N7\\\":6,\\\"@N8\\\":7}}]}";
        String script = "namespace sandbox;\\n\\n" +
                "VM[1..8] : myVMs;\\n\\n" +
                ">>spread({VM1, VM5});\\n" +
                "ban(VM3, @N1);\\n" +
                ">>maxOnline(@N[1..8], 4);";
        String desc = "This use case simulates a server decommissioning operation on 3 servers facing server-based licence restrictions";
        //Mock
        useCases.add(new UseCase("a2dfLx","Server decommissioning against server-based licensing", desc, model, script));
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
