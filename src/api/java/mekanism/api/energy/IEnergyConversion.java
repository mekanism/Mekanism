package mekanism.api.energy;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
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
     * Helper that accepts a long to convert energy of the type represented by this conversion to Joules.
     *
     * @param energy Amount of energy. (Units matching this conversion, will attempt to modify this object)
     *
     * @return Joules.
     *
     * @implNote This method will short circuit for zero or negative values as Joules are always positive.
     */
    default FloatingLong convertFrom(long energy) {
        if (energy <= 0) {
            //Short circuit if energy is zero to avoid having to create any additional objects
            return FloatingLong.ZERO;
        }
        return convertInPlaceFrom(FloatingLong.create(energy));
    }

    /**
     * Converts energy of the type represented by this conversion to Joules.
     *
     * @param energy Amount of energy. (Units matching this conversion, this object will not be modified)
     *
     * @return Joules.
     *
     * @implNote This method will return a new FloatingLong.
     */
    FloatingLong convertFrom(FloatingLong energy);

    /**
     * Converts energy of the type represented by this conversion to Joules.
     *
     * @param energy Amount of energy. (Units matching this conversion, will attempt to modify this object)
     *
     * @return Joules.
     *
     * @implNote This method will attempt to modify the passed in FloatingLong and return it.
     * @apiNote It is recommended to set this to itself to reduce the chance of accidental calls if calling this on a constant {@link FloatingLong}.
     */
    FloatingLong convertInPlaceFrom(FloatingLong energy);

    /**
     * Helper that converts Joules to the energy of the type represented by this conversion and returns it as an int.
     *
     * @param joules Joules. (This object will not be modified)
     *
     * @return Amount of energy clamped to an int. (Units matching this conversion)
     */
    default int convertToAsInt(FloatingLong joules) {
        return convertTo(joules).intValue();
    }

    /**
     * Helper that converts Joules to the energy of the type represented by this conversion and returns it as a long.
     *
     * @param joules Joules. (This object will not be modified)
     *
     * @return Amount of energy clamped to a long. (Units matching this conversion)
     */
    default long convertToAsLong(FloatingLong joules) {
        return convertTo(joules).longValue();
    }

    /**
     * Converts Joules to the energy of the type represented by this conversion.
     *
     * @param joules Joules. (This object will not be modified)
     *
     * @return Amount of energy. (Units matching this conversion)
     *
     * @implNote This method will return a new FloatingLong.
     */
    FloatingLong convertTo(FloatingLong joules);

    /**
     * Converts Joules to the energy of the type represented by this conversion.
     *
     * @param joules Joules. (Will attempt to modify this object)
     *
     * @return Amount of energy. (Units matching this conversion)
     *
     * @implNote This method will attempt to modify the passed in FloatingLong and return it.
     * @apiNote It is recommended to set this to itself to reduce the chance of accidental calls if calling this on a constant {@link FloatingLong}.
     */
    FloatingLong convertInPlaceTo(FloatingLong joules);
}