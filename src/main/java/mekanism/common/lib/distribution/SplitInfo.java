package mekanism.common.lib.distribution;

public abstract class SplitInfo {

    /**
     * Number of targets to split the contents among.
     */
    protected int toSplitAmong;
    /**
     * Represents whether the amount per target distribution has changed. This may happen if a target doesn't need as much as we are willing to offer it in the split.
     */
    protected boolean amountPerChanged = false;

    protected SplitInfo(int totalTargets) {
        this.toSplitAmong = totalTargets;
    }

    /**
     * Marks the given amount as being accounted for and "sent". Decrements {@link #getUnsent() how much we have left to send} and increments
     * {@link #getTotalSent() how much we have sent}.
     *
     * @param amountNeeded     Amount needed by the target and that we are accounting as having been sent to the target.
     * @param decrementTargets Whether this method should reduce the number of targets to split among, and recalculate how much we can provide each target.
     */
    public abstract void send(long amountNeeded, boolean decrementTargets);

    /**
     * {@return the "share" each target should get when distributing in an even split}
     */
    public abstract long getShareAmount();

    /**
     * Gets the "share" including a potential remainder that targets should get when handling remainders. This is used for actually sending providing the split share to
     * any targets that can accept more than we are able to offer in an even split. In general this number will either be equal to {@link #getShareAmount()} or greater
     * than it by one while we still have an excess remainder.
     *
     * @return the "share" plus any potential remainder.
     */
    public abstract long getRemainderAmount();

    /**
     * {@return the amount of contents that has not been sent anywhere yet}
     */
    public abstract long getUnsent();

    /**
     * {@return the total amount of contents that have been sent}
     */
    public abstract long getTotalSent();
}