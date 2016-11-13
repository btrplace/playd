package org.btrplace.playd.service;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.ScriptBuilder;
import org.btrplace.btrpsl.ScriptBuilderException;
import org.btrplace.btrpsl.constraint.*;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.ModelConverter;
import org.btrplace.json.plan.ReconfigurationPlanConverter;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.model.view.network.Network;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.playd.model.JSONErrorReporter;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ChocoScheduler;
import org.btrplace.scheduler.choco.DefaultChocoScheduler;
import org.btrplace.scheduler.choco.duration.ConstantActionDuration;
import org.btrplace.scheduler.choco.duration.DurationEvaluators;
import org.btrplace.scheduler.choco.duration.LinearToAResourceActionDuration;

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

    private static ConstraintsCatalog catalog = makeCatalog();

    private static DurationEvaluators durations = makeDurations();

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response solve(String in) {
        ModelConverter moc = new ModelConverter();
        ReconfigurationPlanConverter rpc = new ReconfigurationPlanConverter();
        Model mo;
        if (in == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing 'instance' parameter").build();
        }
        JSONObject json;
        JSONObject params;
        try {
            json = parse(in);
            mo = moc.fromJSON((JSONObject) json.get("model"));
            params = (JSONObject) json.get("params");
            if (params.get("network").equals(Boolean.TRUE)) {
                System.err.println("network");
                withMigrationScheduling(mo);
            } else {
                Network n = Network.get(mo);
                System.out.println(n);
                if (n != null) {
                    mo.detach(n);
                }
            }
            //Preconditions check
            if (mo.getMapping().getNbNodes() > 8 || mo.getMapping().getNbVMs() > 20) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Instances cannot exceed 8 nodes and 20 VMs").build();
            }
        } catch (JSONConverterException | ParseException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }

        ChocoScheduler scheduler = null;
        try {
            String source = json.get("script").toString();
            ScriptBuilder scrBuilder = new ScriptBuilder(mo);
            scrBuilder.setConstraintsCatalog(catalog);
            scrBuilder.setErrorReporterBuilder(new JSONErrorReporter.Builder());
            Script s = scrBuilder.build(source);
            scheduler = new DefaultChocoScheduler();

            //scheduler.setDurationEvaluators(makeDurations());
            scheduler.doOptimize(params.get("optimise").equals(Boolean.TRUE));
            scheduler.doRepair(params.get("repair").equals(Boolean.TRUE));
            scheduler.setTimeLimit(3);

            ReconfigurationPlan p = scheduler.solve(mo, s.getConstraints());
            if (p == null) {
                return Response.noContent().build();
            }
            System.out.println(p);
            return Response.ok(rpc.toJSON(p)).build();
        } catch (ScriptBuilderException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrorReporter().toString()).build();
        } catch (SchedulerException | JSONConverterException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        } finally {
            if (scheduler != null) {
                System.out.println(scheduler.getStatistics());
            }
        }
    }

    private void withMigrationScheduling(Model mo) {
        ShareableResource mem =ShareableResource.get(mo, "mem");
        Network.createDefaultNetwork(mo, 10000);
        for (VM v : mo.getMapping().getAllVMs()) {
            mo.getAttributes().put(v, "memUsed", mem.getConsumption(v) * 1000); // 8 GiB
            mo.getAttributes().put(v, "hotDirtySize", 56); // 56 MiB
            mo.getAttributes().put(v, "hotDirtyDuration", 2); // 2 sec.
            mo.getAttributes().put(v, "coldDirtyRate", 22.6); // 22.6 MiB/sec.
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

    /**
     * Build the catalog of supported constraints.
     * @return the catalog
     */
    private static ConstraintsCatalog makeCatalog() {
        DefaultConstraintsCatalog c = new DefaultConstraintsCatalog();
        c.add(new AmongBuilder());
        c.add(new BanBuilder());
        c.add(new ResourceCapacityBuilder());
        c.add(new RunningCapacityBuilder());
        c.add(new FenceBuilder());
        c.add(new GatherBuilder());
        c.add(new LonelyBuilder());
        c.add(new OfflineBuilder());
        c.add(new OnlineBuilder());
        c.add(new QuarantineBuilder());
        c.add(new RootBuilder());
        c.add(new SplitBuilder());
        c.add(new SplitAmongBuilder());
        c.add(new SpreadBuilder());
        c.add(new SeqBuilder());
        c.add(new MaxOnlineBuilder());
        c.add(new NoDelayBuilder());
        c.add(new PreserveBuilder());
        return c;
    }

    private static DurationEvaluators makeDurations() {
        DurationEvaluators dev = DurationEvaluators.newBundle();
        //dev.register(MigrateVM.class, new LinearToAResourceActionDuration<>("mem", 1.0, 0));
        //dev.register(BootNode.class, new ConstantActionDuration<>(3));
        //dev.register(ShutdownNode.class, new ConstantActionDuration<>(3));
        return dev;
    }
}
