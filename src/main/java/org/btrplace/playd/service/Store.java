package org.btrplace.playd.service;

import org.btrplace.playd.model.UseCase;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
        useCases.add(new UseCase("server decommissioning", "the description", "foo"));
        useCases.add(new UseCase("foo", "foo desc", "foo instance"));
        useCases.add(new UseCase("bar", "bar desc", "bar instance"));
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> list() {
        List<String> titles = new ArrayList<>(useCases.size());
        for (UseCase uc : useCases) {
            titles.add(uc.title());
        }
        return titles;
    }
}
