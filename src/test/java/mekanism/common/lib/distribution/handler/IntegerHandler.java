package mekanism.common.lib.distribution.handler;

/**
 * Created by Thiakil on 30/04/2021.
 */
public abstract class IntegerHandler {

    private int accepted;

    protected void accept(int amount) {
        accepted += amount;
    }

    public int getAccepted() {
        return accepted;
    }

    public abstract int perform(int amountOffered, boolean isSimulate);
}