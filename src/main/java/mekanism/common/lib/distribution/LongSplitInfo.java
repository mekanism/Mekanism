package mekanism.common.lib.distribution;

public class LongSplitInfo extends SplitInfo {

    private long amountToSplit;
    private long amountPerTarget;
    private long sentSoFar;
    private long remainder;

    public LongSplitInfo(long amountToSplit, int totalTargets) {
        super(totalTargets);
        this.amountToSplit = amountToSplit;
        amountPerTarget = toSplitAmong == 0 ? 0 : amountToSplit / toSplitAmong;
        remainder = toSplitAmong == 0 ? 0 : amountToSplit % toSplitAmong;
    }

    @Override
    public void send(long amountNeeded) {
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
            amountPerTarget = amountToSplit / toSplitAmong;
            remainder = amountToSplit % toSplitAmong;
            if (!amountPerChanged && amountPerTarget != amountPerLast) {
                amountPerChanged = true;
            }
        }
    }

    @Override
    public long getShareAmount() {
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