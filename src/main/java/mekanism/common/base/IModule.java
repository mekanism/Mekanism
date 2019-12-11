package mekanism.common.base;

import mekanism.common.Version;

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

    /*
     * Writes this module's configuration to a ConfigSync packet.
     *
     * @param dataStream - the PacketBuffer of the sync packet
     * @param config     - the configuration to write
     */
    //void writeConfig(PacketBuffer dataStream, MekanismConfigOld config);

    /*
     * Reads this module's configuration from the original ConfigSync packet.
     *
     * @param dataStream - the incoming PacketBuffer of the sync packet
     * @param destConfig - configuration to read to
     */
    //void readConfig(PacketBuffer dataStream, MekanismConfigOld destConfig);*/

    /**
     * Called when the player returns to the main menu.
     */
    void resetClient();
}