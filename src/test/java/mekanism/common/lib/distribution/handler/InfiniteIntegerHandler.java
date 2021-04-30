package mekanism.common.lib.distribution.handler;

public class InfiniteIntegerHandler extends IntegerHandler {

    @Override
    public int perform(int amountOffered, boolean isSimulate) {
        if (!isSimulate) {
            accept(amountOffered);
        }
        return amountOffered;
    }
}