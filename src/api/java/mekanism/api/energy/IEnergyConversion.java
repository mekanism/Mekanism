package mekanism.api.energy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.ULong;
import mekanism.api.math.Unsigned;
import mekanism.api.text.IHasTranslationKey;

/**
 * Represents a conversion between Joules and another energy type (it is valid for this other type to also be Joules).
 *
 * @since 10.3.4
 */
@NothingNullByDefault
public interface IEnergyConversion extends IHasTranslationKey {

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
     * @return Joules (unsigned).
     */
    @Unsigned
    long convertFrom(long energy);

    /**
     * Helper that converts Joules to the energy of the type represented by this conversion and returns it as an int.
     *
     * @param joules Joules. (This object will not be modified)
     *
     * @return Amount of energy clamped to an int. (Units matching this conversion)
     */
    default int convertToAsInt(@Unsigned long joules) {
        return ULong.clampToInt(convertTo(joules));
    }

    /**
     * Helper that converts Joules to the energy of the type represented by this conversion and returns it as a long.
     *
     * @param joules Joules. (This object will not be modified)
     *
     * @return Amount of energy clamped to a long. (Units matching this conversion)
     */
    default long convertToAsLong(@Unsigned long joules) {
        return ULong.clampToSigned(convertTo(joules));
    }

    /**
     * Converts Joules to the energy of the type represented by this conversion.
     *
     * @param joules Joules. (This object will not be modified)
     *
     * @return Amount of energy. (Units matching this conversion)
     */
    @Unsigned
    long convertTo(@Unsigned long joules);
}