package mekanism.common.lib.distribution.handler;

/**
 * Lies about how much it can actually accept when simulating. This is to simulate if multiple handlers end up affecting the same backing tank. As then during simulation
 * it will have reported it could accept more than we actually can.
 */
public class LyingAmountIntegerHandler extends SpecificAmountIntegerHandler {

    private final int amountToLieBy;

    public LyingAmountIntegerHandler(int toAccept, int amountToLieBy) {
        super(toAccept);
        this.amountToLieBy = amountToLieBy;
    }

    @Override
    public int perform(int amountOffered, boolean isSimulate) {
        int canAccept = super.perform(amountOffered, isSimulate);
        if (isSimulate) {
            //If we are simulating, "lie" and say we can accept more than we actually have room for
            return Math.min(amountOffered, canAccept + amountToLieBy);
        }
        return canAccept;
    }
}