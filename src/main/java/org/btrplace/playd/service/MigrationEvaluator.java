package org.btrplace.playd.service;

import org.btrplace.model.Attributes;
import org.btrplace.model.Model;
import org.btrplace.model.VM;
import org.btrplace.model.view.network.Link;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Routing;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.scheduler.choco.duration.ActionDurationEvaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class MigrationEvaluator implements ActionDurationEvaluator<VM> {

    private Map<VM, Integer> bw;

    public MigrationEvaluator(Model mo, List<MigrateVM> migs) {
        bw = durations(Network.get(mo), migs);
    }
    private Map<VM, Integer> durations(Network n, List<MigrateVM> migs) {
        Map<Link, List<MigrateVM>> up = new HashMap<>();
        Map<Link, List<MigrateVM>> down = new HashMap<>();
        for (MigrateVM m : migs) {
            List<Link> path = n.getRouting().getPath(m.getSourceNode(), m.getDestinationNode());
            for (Link l : path) {
                Routing.LinkDirection dir = n.getRouting().getLinkDirection(m.getSourceNode(), m.getDestinationNode(), l);
                Map<Link, List<MigrateVM>> target = up;
                if (dir == Routing.LinkDirection.DOWNLINK) {
                    target = down;
                }
                List<MigrateVM> ms = target.get(l);
                if (ms == null) {
                    ms = new ArrayList<>();
                    target.put(l, ms);
                }
                ms.add(m);
            }
        }

        //The bandwidth (worst case)
        Map<Link, Integer> upBw = new HashMap<>();
        Map<Link, Integer> downBw = new HashMap<>();
        for (Map.Entry<Link, List<MigrateVM>> e : up.entrySet()) {
            upBw.put(e.getKey(), e.getKey().getCapacity() / up.get(e.getKey()).size());
        }

        for (Map.Entry<Link, List<MigrateVM>> e : down.entrySet()) {
            downBw.put(e.getKey(), e.getKey().getCapacity() / down.get(e.getKey()).size());
        }

        //Now, change the migration durations
        Map<VM, Integer> durations = new HashMap<>();

        for (MigrateVM m : migs) {
            VM v = m.getVM();
            int bw = Integer.MAX_VALUE;
            List<Link> path = n.getRouting().getPath(m.getSourceNode(), m.getDestinationNode());
            for (Link l : path) {
                Routing.LinkDirection dir = n.getRouting().getLinkDirection(m.getSourceNode(), m.getDestinationNode(), l);
                if (dir.equals(Routing.LinkDirection.UPLINK)) {
                    bw = Math.min(bw, upBw.get(l));
                } else {
                    bw = Math.min(bw, downBw.get(l));
                }
            }
        }
        return durations;
    }

    @Override
    public int evaluate(Model model, VM vm) {
        Attributes attrs = model.getAttributes();
        // Get attribute vars
        int memUsed = attrs.get(vm, "memUsed", -1);

        // Get VM memory activity attributes if defined, otherwise set an idle workload on the VM
        double hotDirtySize = attrs.get(vm, "hotDirtySize", 5.0);// Minimal observed value on idle VM
        double hotDirtyDuration = attrs.get(vm, "hotDirtyDuration", 2.0); // Minimal observed value on idle VM
        double coldDirtyRate = attrs.get(vm, "coldDirtyRate", 0.0);


        double durationMin;
        double durationColdPages;
        double durationHotPages;
        double durationTotal;

        // Cheat a bit, real is less than theoretical (8->9)
        double bandwidthOctet = bw.get(vm) / 9.0;

        // Estimate the duration for the current bandwidth
        durationMin = memUsed / bandwidthOctet;
        if (durationMin > hotDirtyDuration) {

            durationColdPages = (hotDirtySize + (durationMin - hotDirtyDuration) * coldDirtyRate) /
                    (bandwidthOctet - coldDirtyRate);
            durationHotPages = (hotDirtySize / bandwidthOctet * ((hotDirtySize / hotDirtyDuration) /
                    (bandwidthOctet - (hotDirtySize / hotDirtyDuration))));
            durationTotal = durationMin + durationColdPages + durationHotPages;
        } else {
            durationTotal = durationMin + (((hotDirtySize / hotDirtyDuration) * durationMin) /
                    (bandwidthOctet - (hotDirtySize / hotDirtyDuration)));
        }

        return (int) Math.max(1, Math.round(durationTotal));
    }
}
