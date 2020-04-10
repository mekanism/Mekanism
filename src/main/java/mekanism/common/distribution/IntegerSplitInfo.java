package mekanism.common.distribution;

public class IntegerSplitInfo extends SplitInfo<Integer> {

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
    public void send(Integer amountNeeded) {
        //If we are giving it, then lower the amount we are checking/splitting
        amountToSplit -= amountNeeded;
        sentSoFar += amountNeeded;
        toSplitAmong--;
        //Only recalculate it if it is not willing to accept/doesn't want the
        // full per side split
        if (amountNeeded != amountPerTarget && toSplitAmong != 0) {
            int amountPerLast = amountPerTarget;
            amountPerTarget = amountToSplit / toSplitAmong;
            if (!amountPerChanged && amountPerTarget != amountPerLast) {
                amountPerChanged = true;
            }
        }
    }

    @Override
    public Integer getShareAmount() {
        return amountPerTarget;
    }

    @Override
    public Integer getRemainderAmount() {
        return amountPerTarget + (remainder > 0 ? 1 : 0);
    }

    @Override
    public void updateRemainder() {
        remainder = Math.max(0, remainder - 1);
    }

    @Override
    public Integer getTotalSent() {
        return sentSoFar;
    }
}