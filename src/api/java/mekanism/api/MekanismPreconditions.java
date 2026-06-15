package mekanism.api;

/// Precondition checks useful for implementing Mekanism handlers.
///
/// @since 10.8.0
public final class MekanismPreconditions {

    private MekanismPreconditions() {
    }

    /// Ensures the value is non-negative, throws otherwise.
    ///
    /// @throws IllegalArgumentException when value is negative.
    /// @see net.neoforged.neoforge.transfer.TransferPreconditions#checkNonNegative(int)
    public static void checkNonNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected value to be non-negative: " + value);
        }
    }

    /// Ensures the value is non-negative, throws otherwise.
    ///
    /// @throws IllegalArgumentException when value is negative.
    /// @see net.neoforged.neoforge.transfer.TransferPreconditions#checkNonNegative(int)
    public static void checkNonNegative(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected value to be non-negative: " + value);
        }
    }

    /// Ensures the value is a valid heat capacity, throws otherwise.
    ///
    /// @throws IllegalArgumentException when value is less than one.
    public static void checkHeatCapacity(double value) {
        if (value < 1) {
            throw new IllegalArgumentException("Expected heat capacity to be at least one: " + value);
        }
    }
}