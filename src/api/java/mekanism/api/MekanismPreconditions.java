package mekanism.api;

//TODO - 26.1: Evaluate this class and if we want to do this differently. This is similar to TransferPreconditions
public final class MekanismPreconditions {

    private MekanismPreconditions() {
    }

    /**
     * Ensures the value is non-negative, throws otherwise.
     *
     * @throws IllegalArgumentException when value is negative.
     */
    public static void checkNonNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected value to be non-negative: " + value);
        }
    }
}