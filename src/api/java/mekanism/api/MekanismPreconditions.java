package mekanism.api;

public final class MekanismPreconditions {

    private MekanismPreconditions() {
    }

    /// Ensures the value is non-negative, throws otherwise.
    ///
    /// @throws IllegalArgumentException when value is negative.
    /// @see net.neoforged.neoforge.transfer.TransferPreconditions#checkNonNegative(int)
    /// @since 10.8.0
    public static void checkNonNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected value to be non-negative: " + value);
        }
    }
}