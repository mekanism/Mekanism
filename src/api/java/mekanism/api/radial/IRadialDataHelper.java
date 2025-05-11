package mekanism.api.radial;

import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radial.mode.IRadialMode;
import net.minecraft.resources.ResourceLocation;

/**
 * Helper class for creating builtin implementations of {@link RadialData}.
 *
 * @see IRadialDataHelper#INSTANCE
 * @since 10.3.2
 */
@NothingNullByDefault
public interface IRadialDataHelper {

    /**
     * Provides access to Mekanism's implementation of {@link IRadialDataHelper}.
     *
     * @since 10.4.0
     */
    IRadialDataHelper INSTANCE = MekanismAPI.getService(IRadialDataHelper.class);

    /**
     * Creates an Enum based Radial Data implementation with the given default mode.
     *
     * @param identifier  Identifier representing the radial data. Must be unique within the radial level if it will be used as a nested radial element.
     * @param defaultMode Default mode.
     * @param <MODE>      Radial Mode.
     *
     * @return Enum based Radial Data implementation.
     */
    <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForEnum(ResourceLocation identifier, MODE defaultMode);

    /**
     * Creates an Enum based Radial Data implementation with a default mode of the first element in the given enum.
     *
     * @param identifier Identifier representing the radial data. Must be unique within the radial level if it will be used as a nested radial element.
     * @param enumClass  Class representing the enum type.
     * @param <MODE>     Radial Mode.
     *
     * @return Enum based Radial Data implementation.
     */
    <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForEnum(ResourceLocation identifier, Class<MODE> enumClass);

    /**
     * Creates a Truncated Enum based Radial Data implementation out of the first accessibleValues in the enum of the default mode's type.
     *
     * @param identifier       Identifier representing the radial data. Must be unique within the radial level if it will be used as a nested radial element.
     * @param accessibleValues The number of accessible elements.
     * @param defaultMode      Default mode.
     * @param <MODE>           Radial Mode.
     *
     * @return Truncated Enum based Radial Data implementation.
     *
     * @apiNote Does not currently support {@link mekanism.api.IDisableableEnum}.
     */
    <MODE extends Enum<MODE> & IRadialMode> RadialData<MODE> dataForTruncated(ResourceLocation identifier, int accessibleValues, MODE defaultMode);

    /**
     * Creates a Boolean based Radial Data implementation with a default mode corresponding to {@link BooleanRadialModes#falseMode}.
     *
     * @param identifier Identifier representing the radial data. Must be unique within the radial level if it will be used as a nested radial element.
     * @param modes      Boolean representation of the pairing of two radial modes.
     *
     * @return Boolean based Radial Data implementation.
     *
     * @see #booleanBasedData(ResourceLocation, BooleanRadialModes, boolean)
     */
    default RadialData<IRadialMode> booleanBasedData(ResourceLocation identifier, BooleanRadialModes modes) {
        return booleanBasedData(identifier, modes, false);
    }

    /**
     * Creates a Boolean based Radial Data implementation with a default mode corresponding to the provided boolean.
     *
     * @param identifier   Identifier representing the radial data. Must be unique within the radial level if it will be used as a nested radial element.
     * @param modes        Boolean representation of the pairing of two radial modes.
     * @param defaultValue Default value of the created Radial Data.
     *
     * @return Boolean based Radial Data implementation.
     *
     * @see #booleanBasedData(ResourceLocation, BooleanRadialModes)
     */
    RadialData<IRadialMode> booleanBasedData(ResourceLocation identifier, BooleanRadialModes modes, boolean defaultValue);

    /**
     * Record representing a boolean based pairing of two radial modes.
     *
     * @param falseMode Radial mode corresponding to a {@code false} value.
     * @param trueMode  Radial mode corresponding to a {@code true} value.
     */
    record BooleanRadialModes(IRadialMode falseMode, IRadialMode trueMode) {

        public BooleanRadialModes {
            Objects.requireNonNull(falseMode, "Radial mode representing 'false' cannot be null.");
            Objects.requireNonNull(trueMode, "Radial mode representing 'true' cannot be null.");
        }

        /**
         * Gets the radial mode corresponding to the given value.
         *
         * @param value boolean representation of the mode to lookup.
         *
         * @return {@link #falseMode} if value is {@code false}, otherwise {@link #trueMode}
         */
        public IRadialMode get(boolean value) {
            return value ? trueMode : falseMode;
        }
    }
}