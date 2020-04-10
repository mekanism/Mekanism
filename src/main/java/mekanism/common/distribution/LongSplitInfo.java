package mekanism.common.distribution;

public class LongSplitInfo extends SplitInfo<Long> {

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
        return amountPerTarget + (remainder > 0 ? 1 : 0);
    }

    @Override
    public void updateRemainder() {
        remainder = Math.max(0, remainder - 1);
    }

    @Override
    public Long getTotalSent() {
        return sentSoFar;
    }
}