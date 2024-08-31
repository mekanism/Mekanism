package mekanism.common.lib.distribution.handler;

/**
 * Created by Thiakil on 30/04/2021.
 */
public abstract class LongHandler {

    private long accepted;

    protected void accept(long amount) {
        accepted += amount;
    }

    public long getAccepted() {
        return accepted;
    }

    public abstract long perform(long amountOffered, boolean isSimulate);
}