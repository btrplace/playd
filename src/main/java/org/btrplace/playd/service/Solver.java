package org.btrplace.playd.service;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.ModelConverter;
import org.btrplace.json.plan.ActionConverter;
import org.btrplace.model.Model;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.playd.model.JSONErrorReporter;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;

/**
 * @author Fabien Hermenier
 */
@Path("/solve")
public class Solver {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response solve(String in) {
        ModelConverter moc = new ModelConverter();
        ActionConverter aoc = new ActionConverter();
        Model mo;
        if (in == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing 'instance' parameter").build();
        }
        JSONObject json;
        try {
            json = parse(in);
            System.out.println(json);
            mo = moc.fromJSON((JSONObject) json.get("model"));
            System.out.println(mo.getMapping());
            //Preconditions check
            if (mo.getMapping().getNbNodes() > 8 || mo.getMapping().getNbVMs() > 20) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Instances cannot exceed 8 nodes and 20 VMs").build();
            }
        } catch (JSONConverterException | ParseException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }

        try {
            String source = json.get("script").toString();
            ScriptBuilder scrBuilder = new ScriptBuilder(mo);
            scrBuilder.setErrorReporterBuilder(new JSONErrorReporter.Builder());
            Script s = scrBuilder.build(source);
            ChocoScheduler scheduler = new DefaultChocoScheduler();
            scheduler.doOptimize(false);
            scheduler.setTimeLimit(3);
            ReconfigurationPlan p = scheduler.solve(mo, s.getConstraints());
            if (p == null) {
                return Response.noContent().build();
            }
            return Response.ok(aoc.toJSON(p.getActions())).build();
        } catch (ScriptBuilderException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrorReporter().toString()).build();
        } catch (SchedulerException | JSONConverterException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    private JSONObject parse(String json) throws ParseException, JSONConverterException {
        JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
        Object o = p.parse(new StringReader(json));
        if (!(o instanceof JSONObject)) {
            throw new JSONConverterException("Unable to parse a JSON object");
        }
        return (JSONObject)o;
    }
}