package mekanism.generators.common;

/**
 * Common proxy for the Mekanism Generators module.
 *
 * @author AidanBrady
 */
public class GeneratorsCommonProxy {

    /**
     * Register tile entities that have special models. Overwritten in client to register TESRs.
     */
    public void registerTESRs() {
    }

    /**
     * Register and load client-only item render information.
     */
    public void registerItemRenders() {
    }

    /**
     * Register and load client-only block render information.
     */
    public void registerBlockRenders() {
    }

    public void registerScreenHandlers() {
    }

    public void preInit() {
    }

    /**
     * Set and load the mod's common configuration properties.
     */
    public void loadConfiguration() {
        /*MekanismConfig.local().generators.load(Mekanism.configuration);
        if (Mekanism.configuration.hasChanged()) {
            Mekanism.configuration.save();
        }*/
    }
}