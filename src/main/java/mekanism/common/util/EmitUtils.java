package mekanism.common.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.base.SplitInfo;
import mekanism.common.base.SplitInfo.DoubleSplitInfo;
import mekanism.common.base.SplitInfo.IntegerSplitInfo;
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
        SplitInfo<Integer> splitInfo = new IntegerSplitInfo(amountToSplit, totalTargets);

        //Simulate addition
        for (TARGET target : availableTargets) {
            Map<EnumFacing, HANDLER> wrappers = target.getHandlers();
            for (Entry<EnumFacing, HANDLER> entry : wrappers.entrySet()) {
                EnumFacing side = entry.getKey();
                int amountNeeded = target.simulate(entry.getValue(), side, toSend);
                boolean canGive = amountNeeded <= splitInfo.getAmountPer();
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
                target.shiftNeeded(splitInfo);
            }
        }

        //Give them the amount we calculated they deserve/want
        int sent = 0;
        for (TARGET target : availableTargets) {
            sent += target.sendGivenWithDefault(splitInfo.getAmountPer());
        }
        return sent;
    }

    /**
     * @param availableTargets The EnergyAcceptorWrapper targets to send energy fairly to.
     * @param totalTargets The total number of targets. Note: this number is bigger than availableTargets.size if any
     * targets have more than one acceptor.
     * @param amountToSplit The amount of energy to attempt to send
     * @return The amount that actually got sent
     */
    public static double sendToAcceptors(Set<EnergyAcceptorTarget> availableTargets, int totalTargets,
          double amountToSplit) {
        if (availableTargets.isEmpty() || totalTargets == 0) {
            return 0;
        }
        SplitInfo<Double> splitInfo = new DoubleSplitInfo(amountToSplit, totalTargets);

        //Simulate addition
        for (EnergyAcceptorTarget target : availableTargets) {
            Map<EnumFacing, EnergyAcceptorWrapper> wrappers = target.getHandlers();
            for (Entry<EnumFacing, EnergyAcceptorWrapper> entry : wrappers.entrySet()) {
                EnumFacing side = entry.getKey();
                double amountNeeded = target.simulate(entry.getValue(), side, amountToSplit);
                boolean canGive = amountNeeded <= splitInfo.getAmountPer();
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
            for (EnergyAcceptorTarget target : availableTargets) {
                target.shiftNeeded(splitInfo);
            }
        }

        //Give them all the energy we calculated they deserve/want
        double sent = 0;
        for (EnergyAcceptorTarget target : availableTargets) {
            sent += target.sendGivenWithDefault(splitInfo.getAmountPer());
        }
        return sent;
    }
}