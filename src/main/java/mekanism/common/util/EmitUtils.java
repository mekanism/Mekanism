package mekanism.common.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.base.target.EnergyAcceptorTarget;
import mekanism.common.base.target.IntegerTypeTarget;
import net.minecraft.util.EnumFacing;

public class EmitUtils {

    /**
     * @param <HANDLER> The handler of our target.
     * @param <EXTRA> Any extra information we may need
     * @param <TARGET> The emitter target
     * @param availableTargets The targets to distribute toSend fairly among.
     * @param totalTargets The total number of targets. Note: this number is bigger than availableTargets.size if any
     * targets have more than one acceptor.
     * @param amountToSplit The amount to split between all the targets
     * @param toSend Any extra information such as gas stack or fluid stack.
     * @return The amount that actually got sent.
     */
    public static <HANDLER, EXTRA, TARGET extends IntegerTypeTarget<HANDLER, EXTRA>> int sendToAcceptors(
          Set<TARGET> availableTargets, int totalTargets, int amountToSplit, EXTRA toSend) {
        if (availableTargets.isEmpty() || totalTargets == 0) {
            return 0;
        }
        IntegerSplitInfo splitInfo = new IntegerSplitInfo(amountToSplit, totalTargets);

        //Simulate addition
        for (TARGET target : availableTargets) {
            Map<EnumFacing, HANDLER> wrappers = target.getHandlers();
            for (Entry<EnumFacing, HANDLER> entry : wrappers.entrySet()) {
                EnumFacing side = entry.getKey();
                int amountNeeded = target.simulate(entry.getValue(), side, toSend);
                boolean canGive = amountNeeded <= splitInfo.amountPer;
                //Add the amount
                target.addAmount(side, amountNeeded, canGive);
                if (canGive) {
                    splitInfo.remove(amountNeeded);
                }
            }
        }

        //Only run this if we changed the amountPer from when we first ran things
        while (splitInfo.amountPerChanged) {
            splitInfo.amountPerChanged = false;
            for (TARGET target : availableTargets) {
                //Use an iterator rather than a copy of the keyset of the needed submap
                // This allows for us to remove it once we find it without  having to
                // start looping again or make a large number of copies of the set
                Iterator<Entry<EnumFacing, Integer>> iterator = target.getNeededIterator();
                //TODO: Can pass this inner responsibility to the target by giving it access to the splitInfo
                while (iterator.hasNext()) {
                    Entry<EnumFacing, Integer> needInfo = iterator.next();
                    int amountNeeded = needInfo.getValue();
                    if (amountNeeded <= splitInfo.amountPer) {
                        target.addGiven(needInfo.getKey(), amountNeeded);
                        //Remove it as it no longer valid
                        iterator.remove();
                        //Remove this amount from the split calculation
                        splitInfo.remove(amountNeeded);
                        //Continue checking things in case we happen to be
                        // getting things in a bad order so that we don't recheck
                        // the same values many times
                    }
                }
            }
        }

        //Give them the amount we calculated they deserve/want
        int sent = 0;
        for (TARGET target : availableTargets) {
            sent += target.sendGivenWithDefault(splitInfo.amountPer);
        }
        return sent;
    }

    /**
     * @param availableHandlers The EnergyAcceptorWrapper targets to send energy fairly to.
     * @param totalHandlers The total number of targets. Note: this number is bigger than availableHandlers.size if any
     * targets have more than one acceptor.
     * @param energyToSend The amount of energy to attempt to send
     * @return The amount that actually got sent
     */
    public static double sendToAcceptors(Set<EnergyAcceptorTarget> availableHandlers, int totalHandlers,
          double energyToSend) {
        if (availableHandlers.isEmpty() || totalHandlers == 0) {
            return 0;
        }
        double sent = 0;
        DoubleSplitInfo splitInfo = new DoubleSplitInfo(energyToSend, totalHandlers);

        //Simulate addition
        for (EnergyAcceptorTarget target : availableHandlers) {
            Map<EnumFacing, EnergyAcceptorWrapper> wrappers = target.getHandlers();
            for (Entry<EnumFacing, EnergyAcceptorWrapper> entry : wrappers.entrySet()) {
                EnumFacing side = entry.getKey();
                double amountNeeded = target.simulate(entry.getValue(), side, energyToSend);
                boolean canGive = amountNeeded <= splitInfo.amountPer;
                //Add the amount
                target.addAmount(side, amountNeeded, canGive);
                if (canGive) {
                    splitInfo.remove(amountNeeded);
                }
            }
        }

        //Only run this if we changed the amountPer from when we first ran things
        while (splitInfo.amountPerChanged) {
            splitInfo.amountPerChanged = false;
            for (EnergyAcceptorTarget target : availableHandlers) {
                //Use an iterator rather than a copy of the keyset of the needed submap
                // This allows for us to remove it once we find it without  having to
                // start looping again or make a large number of copies of the set
                Iterator<Entry<EnumFacing, Double>> iterator = target.getNeededIterator();
                while (iterator.hasNext()) {
                    Entry<EnumFacing, Double> needInfo = iterator.next();
                    double amountNeeded = needInfo.getValue();
                    if (amountNeeded <= splitInfo.amountPer) {
                        target.addGiven(needInfo.getKey(), amountNeeded);
                        //Remove it as it no longer valid
                        iterator.remove();
                        //Remove this amount from the split calculation
                        splitInfo.remove(amountNeeded);
                    }
                }
            }
        }

        //Give them all the energy we calculated they deserve/want
        for (EnergyAcceptorTarget target : availableHandlers) {
            sent += target.sendGivenWithDefault(splitInfo.amountPer);
        }

        return sent;
    }

    private static class IntegerSplitInfo {
        private int amountToSplit;
        private int toSplitAmong;
        //AmountPer is the one that needs to be int or double
        private int amountPer;
        private boolean amountPerChanged = false;

        //Amount to split also should be int or double
        private IntegerSplitInfo(int amountToSplit, int totalTargets) {
            this.amountToSplit = amountToSplit;
            this.toSplitAmong = totalTargets;
            this.amountPer = amountToSplit / toSplitAmong;
        }

        private void remove(int amountNeeded) {
            //If we are giving it, then lower the amount we are checking/splitting
            amountToSplit -= amountNeeded;
            toSplitAmong--;
            //Only recalculate it if it is not willing to accept/doesn't want the
            // full per side split
            if (amountNeeded != amountPer && toSplitAmong != 0) {
                int amountPerLast = amountPer;
                amountPer = amountToSplit / toSplitAmong;
                if (!amountPerChanged && amountPer != amountPerLast) {
                    amountPerChanged = true;
                }
            }
        }
    }

    private static class DoubleSplitInfo {
        private double amountToSplit;
        private int toSplitAmong;
        private double amountPer;
        private boolean amountPerChanged = false;

        private DoubleSplitInfo(double amountToSplit, int totalTargets) {
            this.amountToSplit = amountToSplit;
            this.toSplitAmong = totalTargets;
            this.amountPer = amountToSplit / toSplitAmong;
        }

        private void remove(double amountNeeded) {
            //If we are giving it, then lower the amount we are checking/splitting
            amountToSplit -= amountNeeded;
            toSplitAmong--;
            //Only recalculate it if it is not willing to accept/doesn't want the
            // full per side split
            if (amountNeeded != amountPer && toSplitAmong != 0) {
                double amountPerLast = amountPer;
                amountPer = amountToSplit / toSplitAmong;
                if (!amountPerChanged && amountPer != amountPerLast) {
                    amountPerChanged = true;
                }
            }
        }
    }
}