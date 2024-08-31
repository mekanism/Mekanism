package mekanism.common.util;

import mekanism.api.math.MathUtils;
import mekanism.common.lib.distribution.IntegerSplitInfo;
import mekanism.common.lib.distribution.LongSplitInfo;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import org.jetbrains.annotations.Nullable;

public class EmitUtils {

    private EmitUtils() {
    }

    /**
     * @param <HANDLER>        The handler of our target.
     * @param <RESOURCE>       Type of resource (e.g. Stack). Stack amounts ignored
     * @param <TARGET>         The emitter target.
     * @param availableTargets The targets to distribute toSend fairly among.
     * @param splitInfo        Information containing the split.
     * @param resource           Any extra information such as gas stack or fluid stack.
     *
     * @return The amount that actually got sent.
     */
    private static <HANDLER, RESOURCE, TARGET extends Target<HANDLER, RESOURCE>> long sendToAcceptors(
          TARGET availableTargets, SplitInfo splitInfo, RESOURCE resource) {
        if (availableTargets.getHandlerCount() == 0) {
            return splitInfo.getTotalSent();
        }

        //Simulate addition, sending when the requested amount is less than the amountPer
        // splitInfo gets adjusted to account for how much is actually sent
        availableTargets.sendPossible(resource, splitInfo);

        //Only run this if we changed the amountPer from when we first/last ran things
        while (splitInfo.amountPerChanged) {
            splitInfo.amountPerChanged = false;
            //splitInfo gets adjusted to account for how much is actually sent,
            // and if amountPer got changed again, and we need to rerun this
            availableTargets.shiftNeeded(resource, splitInfo);
        }

        //Evenly distribute the remaining amount we have to give between all targets and handlers
        // splitInfo gets adjusted to account for how much is actually sent
        availableTargets.sendRemainingSplit(resource, splitInfo);
        return splitInfo.getTotalSent();
    }

    /**
     * @param <HANDLER>        The handler of our target.
     * @param <RESOURCE>       Type of resource (e.g. Stack). Stack amounts ignored
     * @param <TARGET>         The emitter target
     * @param availableTargets The targets to distribute toSend fairly among.
     * @param amountToSplit    The amount to split between all the targets
     * @param toSend           Any extra information such as gas stack or fluid stack.
     *
     * @return The amount that actually got sent.
     */
    public static <HANDLER, RESOURCE, TARGET extends Target<HANDLER, RESOURCE>> int sendToAcceptors(@Nullable TARGET availableTargets, int amountToSplit, RESOURCE toSend) {
        if (availableTargets == null || availableTargets.getHandlerCount() == 0) {
            return 0;
        }
        return MathUtils.clampToInt(sendToAcceptors(availableTargets, new IntegerSplitInfo(amountToSplit, availableTargets.getHandlerCount()), toSend));
    }

    /**
     * @param <HANDLER>        The handler of our target.
     * @param <RESOURCE>       Type of resource (e.g. Stack). Stack amounts ignored
     * @param <TARGET>         The emitter target
     * @param availableTargets The targets to distribute toSend fairly among.
     * @param amountToSplit    The amount to split between all the targets
     * @param resource           Any extra information such as gas stack or fluid stack.
     *
     * @return The amount that actually got sent.
     */
    public static <HANDLER, RESOURCE, TARGET extends Target<HANDLER, RESOURCE>> long sendToAcceptors(@Nullable TARGET availableTargets, long amountToSplit, RESOURCE resource) {
        if (availableTargets == null || availableTargets.getHandlerCount() == 0) {
            return 0;
        }
        return sendToAcceptors(availableTargets, new LongSplitInfo(amountToSplit, availableTargets.getHandlerCount()), resource);
    }
}