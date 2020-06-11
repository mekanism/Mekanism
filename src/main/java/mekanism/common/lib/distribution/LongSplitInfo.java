package mekanism.common.lib.distribution;

public class LongSplitInfo extends SplitInfo<Long> {

    private long amountToSplit;
    private long amountPerTarget;
    private long sentSoFar;

    public LongSplitInfo(long amountToSplit, int totalTargets) {
        super(totalTargets);
        this.amountToSplit = amountToSplit;
        amountPerTarget = toSplitAmong == 0 ? 0 : amountToSplit / toSplitAmong;
    }

    @Override
    public void send(Long amountNeeded) {
        //If we are giving it, then lower the amount we are checking/splitting
        amountToSplit -= amountNeeded;
        sentSoFar += amountNeeded;
        toSplitAmong--;
        //Only recalculate it if it is not willing to accept/doesn't want the
        // full per side split
        if (amountNeeded != amountPerTarget && toSplitAmong != 0) {
            long amountPerLast = amountPerTarget;
            amountPerTarget = amountToSplit / toSplitAmong;
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
        //Add to the remainder amount the entire remainder so that we try to use it up if we can
        // The remainder then if it cannot be fully accepted slowly shrinks across each target we are distributing to
        //TODO: Evaluate making a more even distribution of the remainder
        return toSplitAmong == 0 ? amountPerTarget : amountPerTarget + (amountToSplit % toSplitAmong);
    }

    @Override
    public Long getTotalSent() {
        return sentSoFar;
    }
}