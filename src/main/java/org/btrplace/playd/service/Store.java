package org.btrplace.playd.service;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.bson.types.ObjectId;
import org.btrplace.playd.Main;
import org.btrplace.playd.model.UseCase;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;

import javax.ws.rs.*;
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

    @POST
    public Response add(String in) {
        JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
        try {
            JSONObject o = (JSONObject) p.parse(in);
            UseCase uc = new UseCase();
            uc.setTitle(o.getAsString("title"));
            uc.setDescription(o.getAsString("description"));
            uc.setScript(o.getAsString("script"));
            uc.setModel(o.getAsString("model"));
            uc.setHits(0);
            uc.setLastHit(System.currentTimeMillis());
            WriteResult<UseCase, String> result = getJacksonDBCollection().insert(uc);
            String id = result.getSavedId();
            return Response.ok(id).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


    @Path("{key}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("key") String key) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().build();
        }

    }

}
