package buildcraft.api.mj;

/** Designates a machine that can receive power. */
public interface IMjReceiver extends IMjConnector {
    /** @return The number of microjoules that this receiver currently wants, and can accept. */
    long getPowerRequested();

    /** Receives power. You are encouraged to either:
     * <ul>
     * <li>Use up all power immediately, or when you next tick.
     * <li>Store all power in something like an {@link MjBattery} for later usage.
     * <li>Refuse all power (if you have no more work to do or your {@link MjBattery} is full).
     * </ul>
     * 
     * @param microJoules The number of micro joules to add.
     * @param simulate If true then just pretend you received power- don't actually change any of your internal state.
     * @return The excess power. */
    long receivePower(long microJoules, boolean simulate);
}
