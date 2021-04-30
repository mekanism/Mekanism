package mekanism.common.lib.distribution.handler;

public class SpecificAmountIntegerHandler extends IntegerHandler {

    private int toAccept;

    public SpecificAmountIntegerHandler(int toAccept) {
        this.toAccept = toAccept;
    }

    @Override
    public int perform(int amountOffered, boolean isSimulate) {
        int amountToTake = Math.min(amountOffered, toAccept);
        if (!isSimulate) {
            accept(amountToTake);
            toAccept -= amountToTake;
        }
        return amountToTake;
    }
}