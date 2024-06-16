package mekanism.common.lib.distribution;

import mekanism.api.math.ULong;
import mekanism.api.math.Unsigned;

public class LongSplitInfo extends SplitInfo<@Unsigned Long> {

    private @Unsigned long amountToSplit;
    private @Unsigned long amountPerTarget;
    private @Unsigned long sentSoFar;
    private @Unsigned long remainder;

    public LongSplitInfo(@Unsigned long amountToSplit, int totalTargets) {
        super(totalTargets);
        this.amountToSplit = amountToSplit;
        amountPerTarget = toSplitAmong == 0 ? 0 : Long.divideUnsigned(amountToSplit, toSplitAmong);
        remainder = toSplitAmong == 0 ? 0 : Long.remainderUnsigned(amountToSplit, toSplitAmong);
    }

    @Override
    public void send(Long amountNeeded) {
        //If we are giving it, then lower the amount we are checking/splitting
        amountToSplit -= amountNeeded;
        sentSoFar += amountNeeded;
        if (!decrementTargets) {
            //If we are not decrementing targets, then don't remove that as a valid target, or update how much there is per target
            long difference = amountNeeded - amountPerTarget;
            if (difference > 0) {
                //If we removed more than we have per target, we need to remove the excess from our remainder
                remainder -= difference;
            }
            return;
        }
        toSplitAmong--;
        //Only recalculate it if it is not willing to accept/doesn't want the
        // full per side split
        if (amountNeeded != amountPerTarget && toSplitAmong != 0) {
            long amountPerLast = amountPerTarget;
            amountPerTarget = Long.divideUnsigned(amountToSplit, toSplitAmong);
            remainder = Long.remainderUnsigned(amountToSplit, toSplitAmong);
            if (!amountPerChanged && amountPerTarget != amountPerLast) {
                amountPerChanged = true;
            }
        }
    }

    @Override
    public Long getShareAmount() {
        return amountPerTarget;
    }

    @Override
    public Long getRemainderAmount() {
        if (toSplitAmong != 0 && remainder != 0) {
            //If we have a remainder, be willing to provide a single unit as the remainder
            // so that we split the remainder more evenly across the targets.
            return amountPerTarget + 1;
        }
        return amountPerTarget;
    }

    @Override
    public Long getUnsent() {
        return remainder;
    }

    @Override
    public boolean isZero(Long value) {
        return value == 0;
    }

    @Override
    public Long getTotalSent() {
        return sentSoFar;
    }
}