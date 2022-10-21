package mekanism.api;

import com.mojang.logging.LogUtils;
import java.util.function.Consumer;
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
import mekanism.api.integration.jei.IMekanismJEIHelper;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.ITooltipHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class MekanismAPI {

    private MekanismAPI() {
    }

    /**
     * The version of the api classes - may not always match the mod's version
     */
    public static final String API_VERSION = "10.3.4";
    public static final String MEKANISM_MODID = "mekanism";
    /**
     * Mekanism debug mode
     */
    public static boolean debug = false;

    public static final Logger logger = LogUtils.getLogger();

    @NotNull
    private static <T> Lazy<ResourceKey<? extends Registry<T>>> registryKey(@SuppressWarnings("unused") @NotNull Class<T> compileTimeTypeValidator, @NotNull String path) {
        return Lazy.of(() -> ResourceKey.createRegistryKey(new ResourceLocation(MEKANISM_MODID, path)));
    }

    //Note: These fields are not directly exposed and are instead exposed via getters as they need to be lazy so that they
    // don't end up causing a crash while running tests due to class loading
    @NotNull
    private static final Lazy<ResourceKey<? extends Registry<Gas>>> GAS_REGISTRY_NAME = registryKey(Gas.class, "gas");
    @NotNull
    private static final Lazy<ResourceKey<? extends Registry<InfuseType>>> INFUSE_TYPE_REGISTRY_NAME = registryKey(InfuseType.class, "infuse_type");
    @NotNull
    private static final Lazy<ResourceKey<? extends Registry<Pigment>>> PIGMENT_REGISTRY_NAME = registryKey(Pigment.class, "pigment");
    @NotNull
    private static final Lazy<ResourceKey<? extends Registry<Slurry>>> SLURRY_REGISTRY_NAME = registryKey(Slurry.class, "slurry");
    @NotNull
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Lazy<ResourceKey<? extends Registry<ModuleData<?>>>> MODULE_REGISTRY_NAME = registryKey((Class) ModuleData.class, "module");
    @NotNull
    private static final Lazy<ResourceKey<? extends Registry<RobitSkin>>> ROBIT_SKIN_REGISTRY_NAME = registryKey(RobitSkin.class, "robit_skin");

    private static IForgeRegistry<Gas> GAS_REGISTRY;
    private static IForgeRegistry<InfuseType> INFUSE_TYPE_REGISTRY;
    private static IForgeRegistry<Pigment> PIGMENT_REGISTRY;
    private static IForgeRegistry<Slurry> SLURRY_REGISTRY;
    private static IForgeRegistry<ModuleData<?>> MODULE_REGISTRY;
    private static IForgeRegistry<RobitSkin> ROBIT_SKIN_REGISTRY;
    private static IMekanismJEIHelper JEI_HELPER;
    private static IModuleHelper MODULE_HELPER;
    private static IRadialDataHelper RADIAL_DATA_HELPER;
    private static IRadiationManager RADIATION_MANAGER;
    private static ISecurityUtils SECURITY_UTILS;
    private static ITooltipHelper TOOLTIP_HELPER;

    //Note: None of the empty variants support registry replacement
    //TODO: Potentially define these with ObjectHolder for purposes of fully defining them outside of the API
    // would have some minor issues with how the empty stacks are declared
    /**
     * Empty Gas instance.
     */
    @NotNull
    public static final Gas EMPTY_GAS = new EmptyGas();
    /**
     * Empty Infuse Type instance.
     */
    @NotNull
    public static final InfuseType EMPTY_INFUSE_TYPE = new EmptyInfuseType();
    /**
     * Empty Pigment instance.
     */
    @NotNull
    public static final Pigment EMPTY_PIGMENT = new EmptyPigment();
    /**
     * Empty Slurry instance.
     */
    @NotNull
    public static final Slurry EMPTY_SLURRY = new EmptySlurry();

    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link Gas gases}.
     *
     * @return Name of the {@link Gas} registry.
     *
     * @apiNote When registering {@link Gas gases} using {@link net.minecraftforge.registries.DeferredRegister<Gas>}, use this method to get access to the
     * {@link ResourceKey}.
     */
    @NotNull
    public static ResourceKey<? extends Registry<Gas>> gasRegistryName() {
        return GAS_REGISTRY_NAME.get();
    }

    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link InfuseType infuse types}.
     *
     * @return Name of the {@link Gas} registry.
     *
     * @apiNote When registering {@link InfuseType infuse types} using {@link net.minecraftforge.registries.DeferredRegister<InfuseType>}, use this method to get access
     * to the {@link ResourceKey}.
     */
    @NotNull
    public static ResourceKey<? extends Registry<InfuseType>> infuseTypeRegistryName() {
        return INFUSE_TYPE_REGISTRY_NAME.get();
    }

    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link Pigment pigments}.
     *
     * @return Name of the {@link Pigment} registry.
     *
     * @apiNote When registering {@link Pigment pigments} using {@link net.minecraftforge.registries.DeferredRegister<Pigment>}, use this method to get access to the
     * {@link ResourceKey}.
     */
    @NotNull
    public static ResourceKey<? extends Registry<Pigment>> pigmentRegistryName() {
        return PIGMENT_REGISTRY_NAME.get();
    }

    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link Slurry sluries}.
     *
     * @return Name of the {@link Slurry} registry.
     *
     * @apiNote When registering {@link Slurry sluries} using {@link net.minecraftforge.registries.DeferredRegister<Slurry>}, use this method to get access to the
     * {@link ResourceKey}.
     */
    @NotNull
    public static ResourceKey<? extends Registry<Slurry>> slurryRegistryName() {
        return SLURRY_REGISTRY_NAME.get();
    }

    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link ModuleData modules}.
     *
     * @return Name of the {@link ModuleData} registry.
     *
     * @apiNote When registering {@link ModuleData modules} using {@link net.minecraftforge.registries.DeferredRegister<ModuleData>}, use this method to get access to the
     * {@link ResourceKey}.
     */
    @NotNull
    public static ResourceKey<? extends Registry<ModuleData<?>>> moduleRegistryName() {
        return MODULE_REGISTRY_NAME.get();
    }

    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link RobitSkin robit skins}.
     *
     * @return Name of the {@link RobitSkin} registry.
     *
     * @apiNote When registering {@link RobitSkin robit skins} using {@link net.minecraftforge.registries.DeferredRegister<RobitSkin>}, use this method to get access to
     * the {@link ResourceKey}.
     */
    @NotNull
    public static ResourceKey<? extends Registry<RobitSkin>> robitSkinRegistryName() {
        return ROBIT_SKIN_REGISTRY_NAME.get();
    }

    /**
     * Gets the Forge Registry for {@link Gas}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<Gas>} instead of {@link net.minecraftforge.registries.RegisterEvent} with the
     * registry name, make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(ResourceKey, String)} rather than passing the result of this method to
     * the other create method, as this method <strong>CAN</strong> return {@code null} if called before the {@link net.minecraftforge.registries.NewRegistryEvent} events
     * have been fired. This method is marked as {@link NotNull} just because except for when this is being called super early it is never {@code null}.
     * @see #gasRegistryName()
     */
    @NotNull
    public static IForgeRegistry<Gas> gasRegistry() {
        if (GAS_REGISTRY == null) {
            GAS_REGISTRY = RegistryManager.ACTIVE.getRegistry(gasRegistryName());
        }
        return GAS_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link InfuseType}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<InfuseType>} instead of {@link net.minecraftforge.registries.RegisterEvent} with
     * the registry name, make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(ResourceKey, String)} rather than passing the result of this
     * method to the other create method, as this method <strong>CAN</strong> return {@code null} if called before the
     * {@link net.minecraftforge.registries.NewRegistryEvent} events have been fired. This method is marked as {@link NotNull} just because except for when this is being
     * called super early it is never {@code null}.
     * @see #infuseTypeRegistryName()
     */
    @NotNull
    public static IForgeRegistry<InfuseType> infuseTypeRegistry() {
        if (INFUSE_TYPE_REGISTRY == null) {
            INFUSE_TYPE_REGISTRY = RegistryManager.ACTIVE.getRegistry(infuseTypeRegistryName());
        }
        return INFUSE_TYPE_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link Pigment}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<Pigment>} instead of {@link net.minecraftforge.registries.RegisterEvent} with the
     * registry name, make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(ResourceKey, String)} rather than passing the result of this method to
     * the other create method, as this method <strong>CAN</strong> return {@code null} if called before the {@link net.minecraftforge.registries.NewRegistryEvent} events
     * have been fired. This method is marked as {@link NotNull} just because except for when this is being called super early it is never {@code null}.
     * @see #pigmentRegistryName()
     */
    @NotNull
    public static IForgeRegistry<Pigment> pigmentRegistry() {
        if (PIGMENT_REGISTRY == null) {
            PIGMENT_REGISTRY = RegistryManager.ACTIVE.getRegistry(pigmentRegistryName());
        }
        return PIGMENT_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link Slurry}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<Slurry>} instead of {@link net.minecraftforge.registries.RegisterEvent} with the
     * registry name, make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(ResourceKey, String)} rather than passing the result of this method to
     * the other create method, as this method <strong>CAN</strong> return {@code null} if called before the {@link net.minecraftforge.registries.NewRegistryEvent} events
     * have been fired. This method is marked as {@link NotNull} just because except for when this is being called super early it is never {@code null}.
     * @see #slurryRegistryName()
     */
    @NotNull
    public static IForgeRegistry<Slurry> slurryRegistry() {
        if (SLURRY_REGISTRY == null) {
            SLURRY_REGISTRY = RegistryManager.ACTIVE.getRegistry(slurryRegistryName());
        }
        return SLURRY_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link ModuleData}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<ModuleData>} instead of {@link net.minecraftforge.registries.RegisterEvent} with
     * the registry name, make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(ResourceKey, String)} rather than passing the result of this
     * method to the other create method, as this method <strong>CAN</strong> return {@code null} if called before the
     * {@link net.minecraftforge.registries.NewRegistryEvent} events have been fired. This method is marked as {@link NotNull} just because except for when this is being
     * called super early it is never {@code null}.
     * @see #moduleRegistryName()
     */
    @NotNull
    public static IForgeRegistry<ModuleData<?>> moduleRegistry() {
        if (MODULE_REGISTRY == null) {
            MODULE_REGISTRY = RegistryManager.ACTIVE.getRegistry(moduleRegistryName());
        }
        return MODULE_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link RobitSkin}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<RobitSkin>} instead of {@link net.minecraftforge.registries.RegisterEvent} with
     * the registry name, make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(ResourceKey, String)} rather than passing the result of this
     * method to the other create method, as this method <strong>CAN</strong> return {@code null} if called before the
     * {@link net.minecraftforge.registries.NewRegistryEvent} events have been fired. This method is marked as {@link NotNull} just because except for when this is being
     * called super early it is never {@code null}.
     * @see #robitSkinRegistryName()
     */
    @NotNull
    public static IForgeRegistry<RobitSkin> robitSkinRegistry() {
        if (ROBIT_SKIN_REGISTRY == null) {
            ROBIT_SKIN_REGISTRY = RegistryManager.ACTIVE.getRegistry(robitSkinRegistryName());
        }
        return ROBIT_SKIN_REGISTRY;
    }

    /**
     * Gets Mekanism's {@link IModuleHelper} that provides various utility methods for implementing custom modules.
     */
    public static IModuleHelper getModuleHelper() {
        if (MODULE_HELPER == null) {//Harmless race
            lookupInstance(IModuleHelper.class, "mekanism.common.content.gear.ModuleHelper", helper -> MODULE_HELPER = helper);
        }
        return MODULE_HELPER;
    }

    /**
     * Gets Mekanism's {@link IRadialDataHelper} that provides various utility methods for creating prebuild {@link mekanism.api.radial.RadialData}.
     *
     * @since 10.3.2
     */
    public static IRadialDataHelper getRadialDataHelper() {
        if (RADIAL_DATA_HELPER == null) {//Harmless race
            lookupInstance(IRadialDataHelper.class, "mekanism.common.lib.radial.data.RadialDataHelper", helper -> RADIAL_DATA_HELPER = helper);
        }
        return RADIAL_DATA_HELPER;
    }

    /**
     * Gets Mekanism's {@link IRadiationManager}.
     */
    public static IRadiationManager getRadiationManager() {
        if (RADIATION_MANAGER == null) {//Harmless race
            lookupInstance(IRadiationManager.class, "mekanism.common.lib.radiation.RadiationManager", manager -> RADIATION_MANAGER = manager);
        }
        return RADIATION_MANAGER;
    }

    /**
     * Mostly for internal use, allows us to access a couple internal helper methods for formatting some numbers in tooltips.
     */
    public static ITooltipHelper getTooltipHelper() {
        if (TOOLTIP_HELPER == null) {//Harmless race
            lookupInstance(ITooltipHelper.class, "mekanism.common.util.text.TooltipHelper", helper -> TOOLTIP_HELPER = helper);
        }
        return TOOLTIP_HELPER;
    }

    /**
     * Provides access to various utility methods for interacting with Mekanism's security system.
     *
     * @since 10.2.1
     */
    public static ISecurityUtils getSecurityUtils() {
        if (SECURITY_UTILS == null) {//Harmless race
            lookupInstance(ISecurityUtils.class, "mekanism.common.util.SecurityUtils", utils -> SECURITY_UTILS = utils);
        }
        return SECURITY_UTILS;
    }

    /**
     * Gets a helper to interact with some of Mekanism's JEI integration internals. This should only be called if JEI is loaded.
     *
     * @throws IllegalStateException if JEI is not loaded.
     */
    public static IMekanismJEIHelper getJeiHelper() {
        if (!ModList.get().isLoaded("jei")) {
            throw new IllegalStateException("JEI is not loaded.");
        }
        if (JEI_HELPER == null) {//Harmless race
            lookupInstance(IMekanismJEIHelper.class, "mekanism.client.jei.MekanismJEIHelper", helper -> JEI_HELPER = helper);
        }
        return JEI_HELPER;
    }

    private static <TYPE> void lookupInstance(Class<TYPE> type, String className, Consumer<TYPE> setter) {
        try {
            Class<?> clazz = Class.forName(className);
            setter.accept(type.cast(clazz.getField("INSTANCE").get(null)));
        } catch (ReflectiveOperationException ex) {
            logger.error(LogUtils.FATAL_MARKER, "Error retrieving {}, Mekanism may be absent, damaged, or outdated.", className);
        }
    }
}