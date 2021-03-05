package mekanism.common.integration.computer;

import java.util.Map;
import mekanism.common.Mekanism;

public interface IComputerTile {

    default boolean hasComputerSupport() {
        return Mekanism.hooks.computerCompatEnabled();
    }

    /**
     * Checks if the computer capability should persist the values between capability invalidations or not. In most cases it can persist and keep track of the bound
     * methods, but for cases like with multiblocks that the multiblock may have changed a secondary backing object, we will need to make sure to recalculate the bound
     * methods.
     */
    default boolean isComputerCapabilityPersistent() {
        return hasComputerSupport();
    }

    /**
     * Gathers all computer methods this computer tile supports and adds them to the given map.
     *
     * @apiNote Only call this if {@link #hasComputerSupport()} is true.
     */
    default void getComputerMethods(Map<String, BoundComputerMethod> methods) {
        ComputerMethodMapper.INSTANCE.getAndBindToHandler(this, methods);
    }

    String getComputerName();
}