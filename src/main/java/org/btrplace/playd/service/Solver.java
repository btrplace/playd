package org.btrplace.playd.service;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.*;
import org.btrplace.btrpsl.*;
import org.btrplace.btrpsl.constraint.*;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.ModelConverter;
import org.btrplace.json.plan.ReconfigurationPlanConverter;
import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.model.view.network.*;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.*;
import org.btrplace.playd.model.JSONErrorReporter;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.*;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
@Path("/solve")
public class Solver {

    private static ConstraintsCatalog catalog = makeCatalog();

    public static final int BANDWIDTH = 10000;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response solve(String in) {
        /*ReconfigurationPlanConverter rpc = new ReconfigurationPlanConverter();

        try {
            JSONObject json = parse(in);
            JSONObject params = (JSONObject) json.get("params");
            Instance i = newInstance(json);
            //Preconditions check
            if (i.getModel().getMapping().getNbNodes() > 8 || i.getModel().getMapping().getNbVMs() > 20) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Instances cannot exceed 8 nodes and 20 VMs").build();
            }

            ReconfigurationPlan p;
            if (params.get("network").equals(Boolean.TRUE)) {
                p = withMigrationScheduling(i, params);
            } else {
                p = withoutMigrationScheduling(i, params);
            }

            if (p == null) {
                return Response.noContent().build();
            }
            return Response.ok(rpc.toJSON(p)).build();
        } catch (JSONConverterException | ParseException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (ScriptBuilderException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getErrorReporter().toString()).build();
        } catch (SchedulerException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }*/
        return Response.ok().build();
    }


    private ReconfigurationPlan withoutMigrationScheduling(Instance i, JSONObject params) {
        //Detach the view because of a bug I don't solve now
        Model mo = i.getModel();
        if (Network.get(mo) != null) {
            mo.detach(Network.get(mo));
        }
        //First solve
        ReconfigurationPlan p = solve(i, getParams(params));
        //Now we compute the mostly exact durations, and redo.
        Network net = new Network();
        Switch mainSwitch = net.newSwitch(); // Main non-blocking switch
        for (Node n : i.getModel().getMapping().getAllNodes()) {
            net.connect(BANDWIDTH, mainSwitch, n); // Connect all nodes with 1Gbit/s links
        }
        Parameters ps = getParams(params);
        List<MigrateVM> migs = p.getActions().stream().
                filter(a -> a instanceof MigrateVM).map(a -> (MigrateVM) a).collect(Collectors.toList());
        ps.getDurationEvaluators().register(MigrateVM.class, new MigrationEvaluator(i.getModel(), migs));

        //Force the destinations
        Model result = p.getResult();
        Model src = p.getOrigin();
        for (Action a : p.getActions()) {
            if (a instanceof MigrateVM) {
                i.getSatConstraints().add(new Fence(((MigrateVM)a).getVM(), Collections.singleton(((MigrateVM)a).getDestinationNode())));
            }
        }
        for (VM vm : mo.getMapping().getRunningVMs()) {
            if (src.getMapping().getVMLocation(vm).equals(result.getMapping().getVMLocation(vm))) {
                i.getSatConstraints().add(new Root(vm));
            }
        }
        return solve(i, getParams(params));
    }

    private Instance newInstance(JSONObject json) throws JSONConverterException, ScriptBuilderException {
        ModelConverter moc = new ModelConverter();
        Model mo = moc.fromJSON((JSONObject) json.get("model"));
        String source = json.get("script").toString();
        ScriptBuilder scrBuilder = new ScriptBuilder(mo);
        scrBuilder.setConstraintsCatalog(catalog);
        scrBuilder.setErrorReporterBuilder(new JSONErrorReporter.Builder());
        Script s = scrBuilder.build(source);
        Set<SatConstraint> cstrs = s.getConstraints();

        ShareableResource mem = ShareableResource.get(mo, "mem");
        for (VM v : mo.getMapping().getAllVMs()) {
            mo.getAttributes().put(v, "memUsed", mem.getConsumption(v) * 1000); // 8 GiB
            mo.getAttributes().put(v, "hotDirtySize", 56); // 56 MiB
            mo.getAttributes().put(v, "hotDirtyDuration", 2); // 2 sec.
            mo.getAttributes().put(v, "coldDirtyRate", 22.6); // 22.6 MiB/sec.
        }

        return new Instance(mo, cstrs, new MinMTTR());
    }

    private Parameters getParams(JSONObject json) {
        Parameters ps = new DefaultParameters();
        ps.doOptimize(json.get("optimise").equals(Boolean.TRUE));
        ps.doRepair(json.get("repair").equals(Boolean.TRUE));
        ps.setTimeLimit(3);
        return ps;
    }

    private ReconfigurationPlan solve(Instance i, Parameters ps) {
        DefaultChocoScheduler scheduler = new DefaultChocoScheduler(ps);
        try {
            ReconfigurationPlan p = scheduler.solve(i);
            if (p != null) {
                System.out.println(p);
            }
            return p;
        } finally {
            System.out.println(scheduler.getStatistics());
        }
    }

    private ReconfigurationPlan withMigrationScheduling(Instance i, JSONObject params) {
        Network.createDefaultNetwork(i.getModel(), 10000);
        return solve(i, getParams(params));
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
}
