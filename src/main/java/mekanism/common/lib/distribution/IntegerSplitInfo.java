package mekanism.common.lib.distribution;

import mekanism.api.math.MathUtils;

public class IntegerSplitInfo extends SplitInfo {

    private int amountToSplit;
    private int amountPerTarget;
    private int sentSoFar;
    private int remainder;

    public IntegerSplitInfo(int amountToSplit, int totalTargets) {
        super(totalTargets);
        this.amountToSplit = amountToSplit;
        amountPerTarget = toSplitAmong == 0 ? 0 : amountToSplit / toSplitAmong;
        remainder = toSplitAmong == 0 ? 0 : amountToSplit % toSplitAmong;
    }

    @Override
    public void send(long amountNeeded) {
        //If we are giving it, then lower the amount we are checking/splitting
        int amountNeededInt = MathUtils.clampToInt(amountNeeded);
        amountToSplit -= amountNeededInt;
        sentSoFar += amountNeededInt;
        if (!decrementTargets) {
            //If we are not decrementing targets, then don't remove that as a valid target, or update how much there is per target
            int difference = amountNeededInt - amountPerTarget;
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
            int amountPerLast = amountPerTarget;
            amountPerTarget = amountToSplit / toSplitAmong;
            remainder = amountToSplit % toSplitAmong;
            if (!amountPerChanged && amountPerTarget != amountPerLast) {
                amountPerChanged = true;
            }
        }
    }

    @Override
    public long getShareAmount() {
        //TODO: Should we make this return a + 1 if there is a remainder, so that we can factor out those cases that can accept exactly amountPerTarget + 1
        // while doing our initial loop rather than handling it via getRemainderAmount?
        return amountPerTarget;
    }

    @Override
    public long getRemainderAmount() {
        if (toSplitAmong != 0 && remainder > 0) {
            //If we have a remainder, be willing to provide a single unit as the remainder
            // so that we split the remainder more evenly across the targets.
            return amountPerTarget + 1;
        }
        return amountPerTarget;
    }

    @Override
    public long getUnsent() {
        return amountToSplit;
    }

    @Override
    public long getTotalSent() {
        return sentSoFar;
    }
}