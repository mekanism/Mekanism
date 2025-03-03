package mekanism.api.energy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import net.minecraft.util.Mth;

/**
 * Represents a conversion between Joules and another energy type (it is valid for this other type to also be Joules).
 *
 * @since 10.3.4
 */
@NothingNullByDefault
public interface IEnergyConversion {

    /**
     * Checks whether this energy conversion is currently usable, or if it is disabled in the config or missing required mods.
     *
     * @return {@code true} if this energy conversion is enabled and can be used.
     */
    boolean isEnabled();

    /**
     * Converts energy of the type represented by this conversion to Joules.
     *
     * @param energy Amount of energy in 'other' type. (Units matching this conversion)
     *
     * @return Joules.
     */
    default long convertFrom(long energy) {
        return MathUtils.clampToLong(energy * getConversion());
    }

    /**
     * Helper that converts Joules to the energy of the type represented by this conversion and returns it as an int.
     *
     * @param joules Joules.
     *
     * @return Amount of energy clamped to an int. (Units matching this conversion)
     */
    default int convertToAsInt(long joules) {
        return MathUtils.clampToInt(convertTo(joules));
    }

    /**
     * Helper that converts Joules to the energy of the type represented by this conversion and returns it as a long.
     *
     * @param joules Joules.
     *
     * @return Amount of energy clamped to a long. (Units matching this conversion)
     */
    @Deprecated(forRemoval = true, since = "10.6.6")
    default long convertToAsLong(long joules) {
        return convertTo(joules);
    }

    /**
     * Converts Joules to the energy of the type represented by this conversion.
     *
     * @param joules Joules.
     *
     * @return Amount of energy. (Units matching this conversion)
     */
    default long convertTo(long joules) {
        return MathUtils.clampToLong(convertToDouble(joules));
    }

    /**
     * Converts Joules to the energy of the type represented by this conversion.
     *
     * @param joules Joules.
     *
     * @return Amount of energy. (Units matching this conversion)
     *
     * @since 10.6.6
     */
    default double convertToDouble(long joules) {
        if (joules == 0) {
            //Short circuit if energy is zero to avoid floating point math
            return 0;
        }
        return joules / getConversion();
    }

    /**
     * {@return if this conversion is one to one with joules}
     *
     * @since 10.6.6
     */
    default boolean isOneToOne() {
        //Use Mth.equal to compare against epsilon
        return Mth.equal(1, getConversion());
    }

    /**
     *
     * {@return the conversion rate to how many of this unit is equal to one Joule}
     *
     * @since 10.6.6
     */
    double getConversion();
}