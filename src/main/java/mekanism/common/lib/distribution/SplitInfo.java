package mekanism.common.lib.distribution;

public class SplitInfo {

    /// Represents whether the amount per target distribution has changed. This may happen if a target doesn't need as much as we are willing to offer it in the split.
    boolean amountPerChanged = false;
    /// Number of targets to split the contents among.
    private int toSplitAmong;
    private long amountToSplit;
    private long amountPerTarget;
    private long sentSoFar;
    private long remainder;

    public SplitInfo(long amountToSplit, int totalTargets) {
        this.toSplitAmong = totalTargets;
        this.amountToSplit = amountToSplit;
        amountPerTarget = toSplitAmong == 0 ? 0 : amountToSplit / toSplitAmong;
        remainder = toSplitAmong == 0 ? 0 : amountToSplit % toSplitAmong;
    }

    /// Marks the given amount as being accounted for and "sent". Decrements [`how much we have left to send`][#getUnsent()] and increments [`how much we have
    /// sent`][#getTotalSent()].
    ///
    /// @param amountNeeded     Amount needed by the target and that we are accounting as having been sent to the target.
    /// @param decrementTargets Whether this method should reduce the number of targets to split among, and recalculate how much we can provide each target.
    public void send(long amountNeeded, boolean decrementTargets) {
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

    /// {@return the "share" each target should get when distributing in an even split}
    public long getShareAmount() {
        //TODO: Should we make this return a + 1 if there is a remainder, so that we can factor out those cases that can accept exactly amountPerTarget + 1
        // while doing our initial loop rather than handling it via getRemainderAmount?
        return amountPerTarget;
    }

    /// Gets the "share" including a potential remainder that targets should get when handling remainders. This is used for actually sending providing the split share to
    /// any targets that can accept more than we are able to offer in an even split. In general this number will either be equal to [#getShareAmount()] or greater than it
    /// by one while we still have an excess remainder.
    ///
    /// @return the "share" plus any potential remainder.
    public long getRemainderAmount() {
        if (toSplitAmong != 0 && remainder > 0) {
            //If we have a remainder, be willing to provide a single unit as the remainder
            // so that we split the remainder more evenly across the targets.
            return amountPerTarget + 1;
        }
        return amountPerTarget;
    }

    /// {@return the amount of contents that has not been sent anywhere yet}
    public long getUnsent() {
        return amountToSplit;
    }

    /// {@return the total amount of contents that have been sent}
    public long getTotalSent() {
        return sentSoFar;
    }
}