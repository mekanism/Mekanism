package mekanism.common.lib.distribution;

public class IntegerSplitInfo extends SplitInfo<Integer> {

    private int amountToSplit;
    private int amountPerTarget;
    private int sentSoFar;

    public IntegerSplitInfo(int amountToSplit, int totalTargets) {
        super(totalTargets);
        this.amountToSplit = amountToSplit;
        amountPerTarget = toSplitAmong == 0 ? 0 : amountToSplit / toSplitAmong;
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
        //Add to the remainder amount the entire remainder so that we try to use it up if we can
        // The remainder then if it cannot be fully accepted slowly shrinks across each target we are distributing to
        //TODO: Evaluate making a more even distribution of the remainder
        return toSplitAmong == 0 ? amountPerTarget : amountPerTarget + (amountToSplit % toSplitAmong);
    }

    @Override
    public Integer getTotalSent() {
        return sentSoFar;
    }
}