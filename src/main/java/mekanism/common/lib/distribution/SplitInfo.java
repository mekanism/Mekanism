package mekanism.common.lib.distribution;

public abstract class SplitInfo<TYPE extends Number & Comparable<TYPE>> {

    protected int toSplitAmong;
    public boolean amountPerChanged = false;
    protected boolean decrementTargets = true;

    protected SplitInfo(int totalTargets) {
        this.toSplitAmong = totalTargets;
    }

    public abstract void send(TYPE amountNeeded);

    public abstract TYPE getShareAmount();

    public abstract TYPE getRemainderAmount();

    public abstract TYPE getUnsent();

    public abstract boolean isZero(TYPE value);

    public abstract TYPE getTotalSent();
}