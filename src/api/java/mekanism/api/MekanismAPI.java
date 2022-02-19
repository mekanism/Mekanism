package mekanism.api;

import javax.annotation.Nonnull;
import mekanism.api.chemical.gas.EmptyGas;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.EmptyInfuseType;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.EmptyPigment;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.EmptySlurry;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.robit.RobitSkin;
import mekanism.api.text.ITooltipHelper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MekanismAPI {

    private MekanismAPI() {
    }

    /**
     * The version of the api classes - may not always match the mod's version
     */
    public static final String API_VERSION = "10.1.1";
    public static final String MEKANISM_MODID = "mekanism";
    /**
     * Mekanism debug mode
     */
    public static boolean debug = false;

    public static final Logger logger = LogManager.getLogger(MEKANISM_MODID + "_api");

    private static IForgeRegistry<Gas> GAS_REGISTRY;
    private static IForgeRegistry<InfuseType> INFUSE_TYPE_REGISTRY;
    private static IForgeRegistry<Pigment> PIGMENT_REGISTRY;
    private static IForgeRegistry<Slurry> SLURRY_REGISTRY;
    private static IForgeRegistry<ModuleData<?>> MODULE_REGISTRY;
    private static IForgeRegistry<RobitSkin> ROBIT_SKIN_REGISTRY;
    private static IModuleHelper MODULE_HELPER;
    private static IRadiationManager RADIATION_MANAGER;
    private static ITooltipHelper TOOLTIP_HELPER;

    //Note: None of the empty variants support registry replacement
    //TODO - 1.18: Rename registry names for the empty types to just being mekanism:empty instead of mekanism:empty_type,
    // and also potentially define these with ObjectHolder for purposes of fully defining them outside of the API
    /**
     * Empty Gas instance.
     */
    @Nonnull
    public static final Gas EMPTY_GAS = new EmptyGas();
    /**
     * Empty Infuse Type instance.
     */
    @Nonnull
    public static final InfuseType EMPTY_INFUSE_TYPE = new EmptyInfuseType();
    /**
     * Empty Pigment instance.
     */
    @Nonnull
    public static final Pigment EMPTY_PIGMENT = new EmptyPigment();
    /**
     * Empty Slurry instance.
     */
    @Nonnull
    public static final Slurry EMPTY_SLURRY = new EmptySlurry();

    /**
     * Gets the Forge Registry for {@link Gas}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<Gas>} instead of {@link net.minecraftforge.event.RegistryEvent.Register<Gas>}
     * make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} rather than passing the result of this method to the other create
     * method, as this method <strong>CAN</strong> return {@code null} if called before the {@link net.minecraftforge.event.RegistryEvent.NewRegistry} events have been
     * fired. This method is marked as {@link Nonnull} just because except for when this is being called super early it is never {@code null}.
     */
    @Nonnull
    public static IForgeRegistry<Gas> gasRegistry() {
        if (GAS_REGISTRY == null) {
            GAS_REGISTRY = RegistryManager.ACTIVE.getRegistry(Gas.class);
        }
        return GAS_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link InfuseType}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<InfuseType>} instead of {@link
     * net.minecraftforge.event.RegistryEvent.Register<InfuseType>} make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} rather
     * than passing the result of this method to the other create method, as this method <strong>CAN</strong> return {@code null} if called before the {@link
     * net.minecraftforge.event.RegistryEvent.NewRegistry} events have been fired. This method is marked as {@link Nonnull} just because except for when this is being
     * called super early it is never {@code null}.
     */
    @Nonnull
    public static IForgeRegistry<InfuseType> infuseTypeRegistry() {
        if (INFUSE_TYPE_REGISTRY == null) {
            INFUSE_TYPE_REGISTRY = RegistryManager.ACTIVE.getRegistry(InfuseType.class);
        }
        return INFUSE_TYPE_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link Pigment}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<Pigment>} instead of {@link
     * net.minecraftforge.event.RegistryEvent.Register<Pigment>} make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} rather than
     * passing the result of this method to the other create method, as this method <strong>CAN</strong> return {@code null} if called before the {@link
     * net.minecraftforge.event.RegistryEvent.NewRegistry} events have been fired. This method is marked as {@link Nonnull} just because except for when this is being
     * called super early it is never {@code null}.
     */
    @Nonnull
    public static IForgeRegistry<Pigment> pigmentRegistry() {
        if (PIGMENT_REGISTRY == null) {
            PIGMENT_REGISTRY = RegistryManager.ACTIVE.getRegistry(Pigment.class);
        }
        return PIGMENT_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link Slurry}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<Slurry>} instead of {@link net.minecraftforge.event.RegistryEvent.Register<Slurry>}
     * make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} rather than passing the result of this method to the other create
     * method, as this method <strong>CAN</strong> return {@code null} if called before the {@link net.minecraftforge.event.RegistryEvent.NewRegistry} events have been
     * fired. This method is marked as {@link Nonnull} just because except for when this is being called super early it is never {@code null}.
     */
    @Nonnull
    public static IForgeRegistry<Slurry> slurryRegistry() {
        if (SLURRY_REGISTRY == null) {
            SLURRY_REGISTRY = RegistryManager.ACTIVE.getRegistry(Slurry.class);
        }
        return SLURRY_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link ModuleData}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<ModuleData>} instead of {@link
     * net.minecraftforge.event.RegistryEvent.Register<ModuleData>} make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} rather
     * than passing the result of this method to the other create method, as this method <strong>CAN</strong> return {@code null} if called before the {@link
     * net.minecraftforge.event.RegistryEvent.NewRegistry} events have been fired. For convenience the class can be gotten via {@link ModuleData#getClassWithGeneric()} as
     * to reduce the unchecked cast warnings. This method is marked as {@link Nonnull} just because except for when this is being called super early it is never {@code
     * null}.
     */
    @Nonnull
    public static IForgeRegistry<ModuleData<?>> moduleRegistry() {
        if (MODULE_REGISTRY == null) {
            MODULE_REGISTRY = RegistryManager.ACTIVE.getRegistry(ModuleData.class);
        }
        return MODULE_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link RobitSkin}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<RobitSkin>} instead of {@link
     * net.minecraftforge.event.RegistryEvent.Register<RobitSkin>} make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(Class, String)} rather
     * than passing the result of this method to the other create method, as this method <strong>CAN</strong> return {@code null} if called before the {@link
     * net.minecraftforge.event.RegistryEvent.NewRegistry} events have been fired. For convenience the class can be gotten via {@link ModuleData#getClassWithGeneric()} as
     * to reduce the unchecked cast warnings. This method is marked as {@link Nonnull} just because except for when this is being called super early it is never {@code
     * null}.
     */
    @Nonnull
    public static IForgeRegistry<RobitSkin> robitSkinRegistry() {
        if (ROBIT_SKIN_REGISTRY == null) {
            ROBIT_SKIN_REGISTRY = RegistryManager.ACTIVE.getRegistry(RobitSkin.class);
        }
        return ROBIT_SKIN_REGISTRY;
    }

    /**
     * Gets Mekanism's {@link IModuleHelper} that provides various utility methods for implementing custom modules.
     */
    public static IModuleHelper getModuleHelper() {
        // Harmless race
        if (MODULE_HELPER == null) {
            try {
                Class<?> clazz = Class.forName("mekanism.common.content.gear.ModuleHelper");
                MODULE_HELPER = (IModuleHelper) clazz.getField("INSTANCE").get(null);
            } catch (ReflectiveOperationException ex) {
                logger.fatal("Error retrieving RadiationManager, Mekanism may be absent, damaged, or outdated.");
            }
        }
        return MODULE_HELPER;
    }

    /**
     * Gets Mekanism's {@link IRadiationManager}.
     */
    public static IRadiationManager getRadiationManager() {
        // Harmless race
        if (RADIATION_MANAGER == null) {
            try {
                Class<?> clazz = Class.forName("mekanism.common.lib.radiation.RadiationManager");
                RADIATION_MANAGER = (IRadiationManager) clazz.getField("INSTANCE").get(null);
            } catch (ReflectiveOperationException ex) {
                logger.fatal("Error retrieving RadiationManager, Mekanism may be absent, damaged, or outdated.");
            }
        }
        return RADIATION_MANAGER;
    }

    /**
     * Mostly for internal use, allows us to access a couple internal helper methods for formatting some numbers in tooltips.
     */
    public static ITooltipHelper getTooltipHelper() {
        // Harmless race
        if (TOOLTIP_HELPER == null) {
            try {
                Class<?> clazz = Class.forName("mekanism.common.util.text.TooltipHelper");
                TOOLTIP_HELPER = (ITooltipHelper) clazz.getField("INSTANCE").get(null);
            } catch (ReflectiveOperationException ex) {
                logger.fatal("Error retrieving TooltipHelper, Mekanism may be absent, damaged, or outdated.");
            }
        }
        return TOOLTIP_HELPER;
    }
}