package org.btrplace.playd.service;

import com.sun.jersey.api.core.HttpContext;
import org.bson.types.ObjectId;
import org.btrplace.playd.Main;
import org.btrplace.playd.model.UseCase;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
    @Produces(MediaType.TEXT_PLAIN)
    public Response add(@Context HttpContext context, UseCase uc) {
        WriteResult<UseCase, String> result = getJacksonDBCollection().insert(uc);
        String id = result.getSavedId();
        return Response.ok(id).build();
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
