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
        int sent = 0;
        int toSplitAmong = totalTargets;
        int amountPer = amountToSplit / toSplitAmong;
        boolean amountPerChanged = false;

        //Simulate addition
        for (TARGET target : availableTargets) {
            Map<EnumFacing, HANDLER> wrappers = target.getHandlers();
            for (Entry<EnumFacing, HANDLER> entry : wrappers.entrySet()) {
                EnumFacing side = entry.getKey();
                int amountNeeded = target.simulate(entry.getValue(), side, toSend);
                boolean canGive = amountNeeded <= amountPer;
                //Add the amount
                target.addAmount(side, amountNeeded, canGive);
                if (canGive) {
                    //If we are giving it, then lower the amount we are checking/splitting
                    amountToSplit -= amountNeeded;
                    toSplitAmong--;
                    //Only recalculate it if it is not willing to accept/doesn't want the
                    // full per side split
                    if (amountNeeded != amountPer && toSplitAmong != 0) {
                        amountPer = amountToSplit / toSplitAmong;
                        amountPerChanged = true;
                    }
                }
            }
        }

        //Only run this if we changed the amountPer from when we first ran things
        while (amountPerChanged) {
            amountPerChanged = false;
            int amountPerLast = amountPer;
            for (TARGET target : availableTargets) {
                //Use an iterator rather than a copy of the keyset of the needed submap
                // This allows for us to remove it once we find it without  having to
                // start looping again or make a large number of copies of the set
                Iterator<Entry<EnumFacing, Integer>> iterator = target.getNeededIterator();
                while (iterator.hasNext()) {
                    Entry<EnumFacing, Integer> needInfo = iterator.next();
                    Integer amountNeeded = needInfo.getValue();
                    if (amountNeeded <= amountPer) {
                        target.addGiven(needInfo.getKey(), amountNeeded);
                        //Remove it as it no longer valid
                        iterator.remove();
                        //Adjust the energy split
                        amountToSplit -= amountNeeded;
                        toSplitAmong--;
                        //Only recalculate it if it is not willing to accept/doesn't want the
                        // full per side split
                        if (amountNeeded != amountPer && toSplitAmong != 0) {
                            amountPer = amountToSplit / toSplitAmong;
                            if (!amountPerChanged && amountPer != amountPerLast) {
                                //We changed our amount so set it back to true so that we know we need
                                // to loop over things again
                                amountPerChanged = true;
                                //Continue checking things in case we happen to be
                                // getting things in a bad order so that we don't recheck
                                // the same values many times
                            }
                        }
                    }
                }
            }
        }

        //Give them all the energy we calculated they deserve/want
        for (TARGET target : availableTargets) {
            sent += target.sendGivenWithDefault(amountPer);
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
        double energyToSplit = energyToSend;
        int toSplitAmong = totalHandlers;
        double amountPer = energyToSplit / toSplitAmong;
        boolean amountPerChanged = false;

        //Simulate addition
        for (EnergyAcceptorTarget target : availableHandlers) {
            Map<EnumFacing, EnergyAcceptorWrapper> wrappers = target.getHandlers();
            for (Entry<EnumFacing, EnergyAcceptorWrapper> entry : wrappers.entrySet()) {
                EnumFacing side = entry.getKey();
                double amountNeeded = target.simulate(entry.getValue(), side, energyToSend);
                boolean canGive = amountNeeded <= amountPer;
                //Add the amount
                target.addAmount(side, amountNeeded, canGive);
                if (canGive) {
                    //If we are giving it, then lower the amount we are checking/splitting
                    energyToSplit -= amountNeeded;
                    toSplitAmong--;
                    //Only recalculate it if it is not willing to accept/doesn't want the
                    // full per side split
                    if (amountNeeded != amountPer && toSplitAmong != 0) {
                        amountPer = energyToSplit / toSplitAmong;
                        amountPerChanged = true;
                    }
                }
            }
        }

        //Only run this if we changed the amountPer from when we first ran things
        while (amountPerChanged) {
            amountPerChanged = false;
            double amountPerLast = amountPer;
            for (EnergyAcceptorTarget target : availableHandlers) {
                //Use an iterator rather than a copy of the keyset of the needed submap
                // This allows for us to remove it once we find it without  having to
                // start looping again or make a large number of copies of the set
                Iterator<Entry<EnumFacing, Double>> iterator = target.getNeededIterator();
                while (iterator.hasNext()) {
                    Entry<EnumFacing, Double> needInfo = iterator.next();
                    Double amountNeeded = needInfo.getValue();
                    if (amountNeeded <= amountPer) {
                        target.addGiven(needInfo.getKey(), amountNeeded);
                        //Remove it as it no longer valid
                        iterator.remove();
                        //Adjust the energy split
                        energyToSplit -= amountNeeded;
                        toSplitAmong--;
                        //Only recalculate it if it is not willing to accept/doesn't want the
                        // full per side split
                        if (amountNeeded != amountPer && toSplitAmong != 0) {
                            amountPer = energyToSplit / toSplitAmong;
                            if (!amountPerChanged && amountPer != amountPerLast) {
                                //We changed our amount so set it back to true so that we know we need
                                // to loop over things again
                                amountPerChanged = true;
                                //Continue checking things in case we happen to be
                                // getting things in a bad order so that we don't recheck
                                // the same values many times
                            }
                        }
                    }
                }
            }
        }

        //Give them all the energy we calculated they deserve/want
        for (EnergyAcceptorTarget target : availableHandlers) {
            sent += target.sendGivenWithDefault(amountPer);
        }

        return sent;
    }
}