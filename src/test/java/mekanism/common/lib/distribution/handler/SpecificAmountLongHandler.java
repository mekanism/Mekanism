package mekanism.common.lib.distribution.handler;

public class SpecificAmountLongHandler extends LongHandler {

    private long toAccept;

    public SpecificAmountLongHandler(long toAccept) {
        this.toAccept = toAccept;
    }

    @Override
    public long perform(long amountOffered, boolean isSimulate) {
        long amountToTake = Math.min(amountOffered, toAccept);
        if (!isSimulate) {
            accept(amountToTake);
            toAccept -= amountToTake;
        }
        return amountToTake;
    }
}