package mekanism.common.util;

import mekanism.api.math.MathUtils;
import mekanism.common.lib.distribution.IntegerSplitInfo;
import mekanism.common.lib.distribution.LongSplitInfo;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import org.jetbrains.annotations.Nullable;

public class EmitUtils {//TODO: Make things work with primitives more directly rather than having to do the boxing and unboxing

    private EmitUtils() {
    }

    /**
     * @param <HANDLER>        The handler of our target.
     * @param <TYPE>           The type of the number
     * @param <EXTRA>          Any extra information we may need.
     * @param <TARGET>         The emitter target.
     * @param availableTargets The targets to distribute toSend fairly among.
     * @param splitInfo        Information containing the split.
     * @param toSend           Any extra information such as gas stack or fluid stack.
     *
     * @return The amount that actually got sent.
     */
    private static <HANDLER, EXTRA, TARGET extends Target<HANDLER, EXTRA>> long sendToAcceptors(
          TARGET availableTargets, SplitInfo splitInfo, EXTRA toSend) {
        if (availableTargets.getHandlerCount() == 0) {
            return splitInfo.getTotalSent();
        }

        //Simulate addition, sending when the requested amount is less than the amountPer
        // splitInfo gets adjusted to account for how much is actually sent
        availableTargets.sendPossible(toSend, splitInfo);

        //Only run this if we changed the amountPer from when we first/last ran things
        while (splitInfo.amountPerChanged) {
            splitInfo.amountPerChanged = false;
            //splitInfo gets adjusted to account for how much is actually sent,
            // and if amountPer got changed again, and we need to rerun this
            availableTargets.shiftNeeded(splitInfo);
        }

        //Evenly distribute the remaining amount we have to give between all targets and handlers
        // splitInfo gets adjusted to account for how much is actually sent
        availableTargets.sendRemainingSplit(splitInfo);
        return splitInfo.getTotalSent();
    }

    /**
     * @param <HANDLER>        The handler of our target.
     * @param <EXTRA>          Any extra information we may need
     * @param <TARGET>         The emitter target
     * @param availableTargets The targets to distribute toSend fairly among.
     * @param amountToSplit    The amount to split between all the targets
     * @param toSend           Any extra information such as gas stack or fluid stack.
     *
     * @return The amount that actually got sent.
     */
    public static <HANDLER, EXTRA, TARGET extends Target<HANDLER, EXTRA>> int sendToAcceptors(@Nullable TARGET availableTargets, int amountToSplit, EXTRA toSend) {
        if (availableTargets == null || availableTargets.getHandlerCount() == 0) {
            return 0;
        }
        return MathUtils.clampToInt(sendToAcceptors(availableTargets, new IntegerSplitInfo(amountToSplit, availableTargets.getHandlerCount()), toSend));
    }

    /**
     * @param <HANDLER>        The handler of our target.
     * @param <EXTRA>          Any extra information we may need
     * @param <TARGET>         The emitter target
     * @param availableTargets The targets to distribute toSend fairly among.
     * @param amountToSplit    The amount to split between all the targets
     * @param toSend           Any extra information such as gas stack or fluid stack.
     *
     * @return The amount that actually got sent.
     */
    public static <HANDLER, EXTRA, TARGET extends Target<HANDLER, EXTRA>> long sendToAcceptors(@Nullable TARGET availableTargets, long amountToSplit, EXTRA toSend) {
        if (availableTargets == null || availableTargets.getHandlerCount() == 0) {
            return 0;
        }
        return sendToAcceptors(availableTargets, new LongSplitInfo(amountToSplit, availableTargets.getHandlerCount()), toSend);
    }
}