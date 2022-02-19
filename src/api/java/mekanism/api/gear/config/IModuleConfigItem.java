package mekanism.api.gear.config;

import javax.annotation.Nonnull;

/**
 * Interface representing module config items.
 *
 * @apiNote This interface should not be directly implemented and should instead be created using the provided {@link ModuleConfigItemCreator}.
 */
public interface IModuleConfigItem<TYPE> {

    /**
     * Gets the name of this {@link IModuleConfigItem}.
     *
     * @return Name.
     */
    @Nonnull
    String getName();

    /**
     * Gets the current value of this {@link IModuleConfigItem}.
     *
     * @return Current value.
     */
    @Nonnull
    TYPE get();

    /**
     * Sets the value of this {@link IModuleConfigItem}.
     *
     * @param val Desired value.
     */
    void set(@Nonnull TYPE val);
}