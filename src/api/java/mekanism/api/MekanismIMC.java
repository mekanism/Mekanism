package mekanism.api;

import mekanism.api.providers.IModuleDataProvider;
import net.minecraftforge.fml.InterModComms;

/**
 * Class containing various helpers for sending IMC messages to Mekanism.
 */
public class MekanismIMC {

    private MekanismIMC() {
    }

    /**
     * This method registers a module or modules as supporting the Meka-Tool. The body of the message should either be an {@link IModuleDataProvider} or an array of
     * {@link IModuleDataProvider}s. {@link #addMekaToolModules(IModuleDataProvider[])} can be used as a helper to send properly structured messages of this type.
     */
    public static final String ADD_MEKA_TOOL_MODULES = "add_meka_tool_modules";
    /**
     * This method registers a module or modules as supporting the MekaSuit Helmet. The body of the message should either be an {@link IModuleDataProvider} or an array of
     * {@link IModuleDataProvider}s. {@link #addMekaSuitHelmetModules(IModuleDataProvider[])} can be used as a helper to send properly structured messages of this type.
     */
    public static final String ADD_MEKA_SUIT_HELMET_MODULES = "add_meka_suit_helmet_modules";
    /**
     * This method registers a module or modules as supporting the MekaSuit Bodyarmor. The body of the message should either be an {@link IModuleDataProvider} or an array
     * of {@link IModuleDataProvider}s. {@link #addMekaSuitBodyarmorModules(IModuleDataProvider[])} can be used as a helper to send properly structured messages of this
     * type.
     */
    public static final String ADD_MEKA_SUIT_BODYARMOR_MODULES = "add_meka_suit_bodyarmor_modules";
    /**
     * This method registers a module or modules as supporting the MekaSuit Pants. The body of the message should either be an {@link IModuleDataProvider} or an array of
     * {@link IModuleDataProvider}s. {@link #addMekaSuitPantsModules(IModuleDataProvider[])} can be used as a helper to send properly structured messages of this type.
     */
    public static final String ADD_MEKA_SUIT_PANTS_MODULES = "add_meka_suit_pants_modules";
    /**
     * This method registers a module or modules as supporting the MekaSuit Boots. The body of the message should either be an {@link IModuleDataProvider} or an array of
     * {@link IModuleDataProvider}s. {@link #addMekaSuitBootsModules(IModuleDataProvider[])} can be used as a helper to send properly structured messages of this type.
     */
    public static final String ADD_MEKA_SUIT_BOOTS_MODULES = "add_meka_suit_boots_modules";

    /**
     * Helper method to register modules as supported to all module supporting items (MekaSuit and Meka-Tool).
     *
     * @param moduleDataProviders Modules to register as supported.
     *
     * @apiNote Call this method during the {@link net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent}.
     */
    public static void addModulesToAll(IModuleDataProvider<?>... moduleDataProviders) {
        addMekaToolModules(moduleDataProviders);
        addMekaSuitModules(moduleDataProviders);
    }

    /**
     * Helper method to register modules as supported to all MekaSuit pieces.
     *
     * @param moduleDataProviders Modules to register as supported.
     *
     * @apiNote Call this method during the {@link net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent}.
     */
    public static void addMekaSuitModules(IModuleDataProvider<?>... moduleDataProviders) {
        addMekaSuitHelmetModules(moduleDataProviders);
        addMekaSuitBodyarmorModules(moduleDataProviders);
        addMekaSuitPantsModules(moduleDataProviders);
        addMekaSuitBootsModules(moduleDataProviders);
    }

    /**
     * Helper method to register modules as supported by the Meka-Tool.
     *
     * @param moduleDataProviders Modules to register as supported.
     *
     * @apiNote Call this method during the {@link net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent}.
     */
    public static void addMekaToolModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(ADD_MEKA_TOOL_MODULES, moduleDataProviders);
    }

    /**
     * Helper method to register modules as supported by the MekaSuit Helmet.
     *
     * @param moduleDataProviders Modules to register as supported.
     *
     * @apiNote Call this method during the {@link net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent}.
     */
    public static void addMekaSuitHelmetModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(ADD_MEKA_SUIT_HELMET_MODULES, moduleDataProviders);
    }

    /**
     * Helper method to register modules as supported by the MekaSuit Bodyarmor.
     *
     * @param moduleDataProviders Modules to register as supported.
     *
     * @apiNote Call this method during the {@link net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent}.
     */
    public static void addMekaSuitBodyarmorModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(ADD_MEKA_SUIT_BODYARMOR_MODULES, moduleDataProviders);
    }

    /**
     * Helper method to register modules as supported by the MekaSuit Pants.
     *
     * @param moduleDataProviders Modules to register as supported.
     *
     * @apiNote Call this method during the {@link net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent}.
     */
    public static void addMekaSuitPantsModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(ADD_MEKA_SUIT_PANTS_MODULES, moduleDataProviders);
    }

    /**
     * Helper method to register modules as supported by the MekaSuit Boots.
     *
     * @param moduleDataProviders Modules to register as supported.
     *
     * @apiNote Call this method during the {@link net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent}.
     */
    public static void addMekaSuitBootsModules(IModuleDataProvider<?>... moduleDataProviders) {
        sendModuleIMC(ADD_MEKA_SUIT_BOOTS_MODULES, moduleDataProviders);
    }

    private static void sendModuleIMC(String method, IModuleDataProvider<?>... moduleDataProviders) {
        if (moduleDataProviders == null || moduleDataProviders.length == 0) {
            throw new IllegalArgumentException("No module data providers given.");
        }
        InterModComms.sendTo(MekanismAPI.MEKANISM_MODID, method, () -> moduleDataProviders);
    }
}