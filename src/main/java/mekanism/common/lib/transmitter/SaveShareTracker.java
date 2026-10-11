package mekanism.common.lib.transmitter;

/// Tracks whether the save shares handed out to a network's transmitters still match the network's buffer, so they can be reused instead of recalculated.
public class SaveShareTracker {

    private boolean calculated;
    private long calculatedTime;
    private long expectedAmount;

    /// @param gameTime The current game time.
    /// @param amount   The amount currently in the network's buffer.
    ///
    /// @return `true` if the shares have never been calculated, were calculated on another tick, or the buffer changed by something other than a share being taken.
    public boolean needsRecalculation(long gameTime, long amount) {
        return !calculated || calculatedTime != gameTime || expectedAmount != amount;
    }

    public void recalculated(long gameTime, long amount) {
        calculated = true;
        calculatedTime = gameTime;
        expectedAmount = amount;
    }

    /// Marks that a transmitter took its share out of the network's buffer, so that is not mistaken for an outside change.
    public void shareTaken(long amount) {
        expectedAmount -= amount;
    }
}
