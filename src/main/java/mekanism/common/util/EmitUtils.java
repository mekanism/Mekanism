package mekanism.common.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.common.base.SplitInfo;
import mekanism.common.base.SplitInfo.DoubleSplitInfo;
import mekanism.common.base.SplitInfo.IntegerSplitInfo;
import mekanism.common.base.target.EnergyAcceptorTarget;
import mekanism.common.base.target.IntegerTypeTarget;
import mekanism.common.base.target.Target;
import net.minecraft.util.EnumFacing;

public class EmitUtils {

    /**
     * @param <HANDLER> The handler of our target.
     * @param <TYPE> The type of the number
     * @param <EXTRA> Any extra information we may need.
     * @param <TARGET> The emitter target.
     * @param availableTargets The targets to distribute toSend fairly among.
     * @param totalTargets The total number of targets. Note: this number is bigger than availableTargets.size if any
     * targets have more than one acceptor.
     * @param splitInfo Information containing the split.
     * @param toSend Any extra information such as gas stack or fluid stack.
     * @param zero Zero value based on the
     * @return The amount that actually got sent.
     */
    public static <HANDLER, TYPE extends Number & Comparable<TYPE>, EXTRA, TARGET extends Target<HANDLER, TYPE, EXTRA>>
    TYPE sendToAcceptors(Set<TARGET> availableTargets, int totalTargets, SplitInfo<TYPE> splitInfo, EXTRA toSend,
          TYPE zero) {
        if (availableTargets.isEmpty() || totalTargets == 0) {
            return zero;
        }

        //Simulate addition
        for (TARGET target : availableTargets) {
            Map<EnumFacing, HANDLER> wrappers = target.getHandlers();
            for (Entry<EnumFacing, HANDLER> entry : wrappers.entrySet()) {
                EnumFacing side = entry.getKey();
                TYPE amountNeeded = target.simulate(entry.getValue(), side, toSend);
                int compared = amountNeeded.compareTo(splitInfo.getAmountPer());

                boolean canGive = compared <= 0;
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
        TYPE sent = zero;
        for (TARGET target : availableTargets) {
            sent = target.sendGivenWithDefault(sent, splitInfo.getAmountPer());
        }
        return sent;
    }

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
        return sendToAcceptors(availableTargets, totalTargets, new IntegerSplitInfo(amountToSplit, totalTargets),
              toSend,
              0);
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
        //toSend is 0 as we don't have any extra data
        return sendToAcceptors(availableTargets, totalTargets, new DoubleSplitInfo(amountToSplit, totalTargets), 0D,
              0D);
    }
}