package mekanism.common.lib.distribution.handler;

public class PartialLongHandler extends LongHandler {

    @Override
    public long perform(long amountOffered, boolean isSimulate) {
        long amountToTake = amountOffered / 2;
        if (!isSimulate) {
            accept(amountToTake);
        }
        return amountToTake;
    }
}