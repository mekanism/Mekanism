package mekanism.common.base;

import mekanism.common.lib.Version;

/**
 * Implement in your main class if your mod happens to be completely reliant on Mekanism, or in other words, is a Mekanism module.
 *
 * @author aidancbrady
 */
public interface IModule {

    /**
     * Gets the version of the module.
     *
     * @return the module's version
     */
    Version getVersion();

    /**
     * Gets the name of the module.  Note that this doesn't include "Mekanism" like the actual module's name does, just the unique name.  For example, MekanismGenerators
     * returns "Generators" here.
     *
     * @return unique name of the module
     */
    String getName();

    /**
     * Called when the player returns to the main menu.
     */
    void resetClient();

    /**
     * Called during the first tick after joining a game.
     */
    default void launchClient() {
    }
}