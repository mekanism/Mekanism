package mekanism.api.gas;

import javax.annotation.Nonnull;

/**
 * Created by Thiakil on 11/11/2017.
 */
//TODO: Should something like this be made for chemicals more generically?
public interface GasTankInfo {

    /**
     * Retrieve the stored gas stack. DO NOT MODIFY.
     *
     * @return the stored gas, or null
     */
    @Nonnull
    GasStack getStack();

    /**
     * Gets the amount of gas stored by this GasTank.
     *
     * @return amount of gas stored
     */
    int getStored();

    /**
     * Gets the capacity of this tank.
     *
     * @return - max gas
     */
    int getCapacity();
}