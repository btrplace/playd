package org.btrplace.playd.service;

import org.btrplace.playd.Main;
import org.btrplace.playd.model.UseCase;
import org.mongojack.JacksonDBCollection;
import org.bson.types.ObjectId;
import org.mongojack.WriteResult;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Fabien Hermenier
 */
@Path("/store")
public class Store {

    private static JacksonDBCollection<UseCase, String> getJacksonDBCollection() {
        return JacksonDBCollection.wrap(Main.mongoDB.getCollection(UseCase.class.getSimpleName().toLowerCase()), UseCase.class, String.class);
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(UseCase uc) {
        WriteResult<UseCase, String> result = getJacksonDBCollection().insert(uc);
        String id = result.getSavedId();
        try {
            return Response.created(new URI(id)).build();
        } catch (URISyntaxException ex) {
            System.err.println(ex.getMessage());
        }
        return Response.serverError().build();
    }

    @Path("/{key}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("key") String key) {
        if (!ObjectId.isValid(key)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        UseCase uc = getJacksonDBCollection().findOneById(key);
        if (uc == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        uc.hit();
        getJacksonDBCollection().updateById(key, uc);
        return Response.ok(uc).build();
    }

}
