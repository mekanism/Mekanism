package mekanism.common.lib.distribution.handler;

public class PartialIntegerHandler extends IntegerHandler {

    @Override
    public int perform(int amountOffered, boolean isSimulate) {
        int amountToTake = amountOffered / 2;
        if (!isSimulate) {
            accept(amountToTake);
        }
        return amountToTake;
    }
}