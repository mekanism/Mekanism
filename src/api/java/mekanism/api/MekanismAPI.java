package mekanism.api;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.EmptyGas;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.EmptyInfuseType;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.EmptyPigment;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.EmptySlurry;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@NothingNullByDefault
public class MekanismAPI {

    private MekanismAPI() {
    }

    /**
     * The version of the api classes - may not always match the mod's version
     */
    public static final String API_VERSION = "10.4.0";
    /**
     * Mekanism's Mod ID
     */
    public static final String MEKANISM_MODID = "mekanism";
    /**
     * Mekanism debug mode
     */
    public static boolean debug = false;
    /**
     * Logger for use in Mekanism's API classes
     */
    public static final Logger logger = LogUtils.getLogger();

    private static <T> ResourceKey<Registry<T>> registryKey(@SuppressWarnings("unused") Class<T> compileTimeTypeValidator, String path) {
        return ResourceKey.createRegistryKey(new ResourceLocation(MEKANISM_MODID, path));
    }

    private static <T> ResourceKey<Registry<Codec<? extends T>>> codecRegistryKey(@SuppressWarnings("unused") Class<T> compileTimeTypeValidator, String path) {
        return ResourceKey.createRegistryKey(new ResourceLocation(MEKANISM_MODID, path));
    }

    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link Gas gases}.
     *
     * @apiNote When registering {@link Gas gases} using {@link net.minecraftforge.registries.DeferredRegister<Gas>}, use this field to get access to the
     * {@link ResourceKey}.
     * @since 10.4.0
     */
    public static final ResourceKey<Registry<Gas>> GAS_REGISTRY_NAME = registryKey(Gas.class, "gas");
    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link InfuseType infuse types}.
     *
     * @apiNote When registering {@link InfuseType infuse types} using {@link net.minecraftforge.registries.DeferredRegister<InfuseType>}, use this field to get access to
     * the {@link ResourceKey}.
     * @since 10.4.0
     */
    public static final ResourceKey<Registry<InfuseType>> INFUSE_TYPE_REGISTRY_NAME = registryKey(InfuseType.class, "infuse_type");
    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link Pigment pigments}.
     *
     * @apiNote When registering {@link Pigment pigments} using {@link net.minecraftforge.registries.DeferredRegister<Pigment>}, use this field to get access to the
     * {@link ResourceKey}.
     * @since 10.4.0
     */
    public static final ResourceKey<Registry<Pigment>> PIGMENT_REGISTRY_NAME = registryKey(Pigment.class, "pigment");
    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link Slurry sluries}.
     *
     * @apiNote When registering {@link Slurry sluries} using {@link net.minecraftforge.registries.DeferredRegister<Slurry>}, use this field to get access to the
     * {@link ResourceKey}.
     * @since 10.4.0
     */
    public static final ResourceKey<Registry<Slurry>> SLURRY_REGISTRY_NAME = registryKey(Slurry.class, "slurry");
    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link ModuleData modules}.
     *
     * @apiNote When registering {@link ModuleData modules} using {@link net.minecraftforge.registries.DeferredRegister<ModuleData>}, use this field to get access to the
     * {@link ResourceKey}.
     * @since 10.4.0
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final ResourceKey<Registry<ModuleData<?>>> MODULE_REGISTRY_NAME = registryKey((Class) ModuleData.class, "module");
    /**
     * Gets the {@link ResourceKey} representing the name of the Datapack Registry for {@link RobitSkin robit skins}.
     *
     * @since 10.4.0
     */
    public static final ResourceKey<Registry<RobitSkin>> ROBIT_SKIN_REGISTRY_NAME = registryKey(RobitSkin.class, "robit_skin");
    /**
     * Gets the {@link ResourceKey} representing the name of the Forge Registry for {@link RobitSkin robit skin} serializers.
     *
     * @apiNote When registering {@link RobitSkin robit skin} serializers using
     * {@link net.minecraftforge.registries.DeferredRegister DeferredRegister< Codec<? extends RobitSkin>>}, use this field to get access to the {@link ResourceKey}.
     * @since 10.4.0
     */
    public static final ResourceKey<Registry<Codec<? extends RobitSkin>>> ROBIT_SKIN_SERIALIZER_REGISTRY_NAME = codecRegistryKey(RobitSkin.class, "robit_skin_serializer");

    @Nullable
    private static IForgeRegistry<Gas> GAS_REGISTRY;
    @Nullable
    private static IForgeRegistry<InfuseType> INFUSE_TYPE_REGISTRY;
    @Nullable
    private static IForgeRegistry<Pigment> PIGMENT_REGISTRY;
    @Nullable
    private static IForgeRegistry<Slurry> SLURRY_REGISTRY;
    @Nullable
    private static IForgeRegistry<ModuleData<?>> MODULE_REGISTRY;
    @Nullable
    private static IForgeRegistry<Codec<? extends RobitSkin>> ROBIT_SKIN_SERIALIZER_REGISTRY;

    //Note: None of the empty variants support registry replacement
    //TODO: Potentially define these with ObjectHolder for purposes of fully defining them outside of the API
    // would have some minor issues with how the empty stacks are declared
    /**
     * Empty Gas instance.
     */
    public static final Gas EMPTY_GAS = new EmptyGas();
    /**
     * Empty Infuse Type instance.
     */
    public static final InfuseType EMPTY_INFUSE_TYPE = new EmptyInfuseType();
    /**
     * Empty Pigment instance.
     */
    public static final Pigment EMPTY_PIGMENT = new EmptyPigment();
    /**
     * Empty Slurry instance.
     */
    public static final Slurry EMPTY_SLURRY = new EmptySlurry();

    /**
     * Gets the Forge Registry for {@link Gas}.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister<Gas>} instead of {@link net.minecraftforge.registries.RegisterEvent} with the
     * registry name, make sure to use {@link net.minecraftforge.registries.DeferredRegister#create(ResourceKey, String)} rather than passing the result of this method to
     * the other create method, as this method <strong>CAN</strong> return {@code null} if called before the {@link net.minecraftforge.registries.NewRegistryEvent} events
     * have been fired. This method is marked as {@link NotNull} just because except for when this is being called super early it is never {@code null}.
     * @see #GAS_REGISTRY_NAME
     */
    public static IForgeRegistry<Gas> gasRegistry() {
        if (GAS_REGISTRY == null) {
            GAS_REGISTRY = RegistryManager.ACTIVE.getRegistry(GAS_REGISTRY_NAME);
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
     * @see #INFUSE_TYPE_REGISTRY_NAME
     */
    public static IForgeRegistry<InfuseType> infuseTypeRegistry() {
        if (INFUSE_TYPE_REGISTRY == null) {
            INFUSE_TYPE_REGISTRY = RegistryManager.ACTIVE.getRegistry(INFUSE_TYPE_REGISTRY_NAME);
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
     * @see #PIGMENT_REGISTRY_NAME
     */
    public static IForgeRegistry<Pigment> pigmentRegistry() {
        if (PIGMENT_REGISTRY == null) {
            PIGMENT_REGISTRY = RegistryManager.ACTIVE.getRegistry(PIGMENT_REGISTRY_NAME);
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
     * @see #SLURRY_REGISTRY_NAME
     */
    public static IForgeRegistry<Slurry> slurryRegistry() {
        if (SLURRY_REGISTRY == null) {
            SLURRY_REGISTRY = RegistryManager.ACTIVE.getRegistry(SLURRY_REGISTRY_NAME);
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
     * @see #MODULE_REGISTRY_NAME
     */
    public static IForgeRegistry<ModuleData<?>> moduleRegistry() {
        if (MODULE_REGISTRY == null) {
            MODULE_REGISTRY = RegistryManager.ACTIVE.getRegistry(MODULE_REGISTRY_NAME);
        }
        return MODULE_REGISTRY;
    }

    /**
     * Gets the Forge Registry for {@link RobitSkin} serializers.
     *
     * @apiNote If registering via {@link net.minecraftforge.registries.DeferredRegister DeferredRegister< Codec<? extends RobitSkin>>} instead of
     * {@link net.minecraftforge.registries.RegisterEvent} with the registry name, make sure to use
     * {@link net.minecraftforge.registries.DeferredRegister#create(ResourceKey, String)} rather than passing the result of this method to the other create method, as
     * this method <strong>CAN</strong> return {@code null} if called before the {@link net.minecraftforge.registries.NewRegistryEvent} events have been fired. This
     * method is marked as {@link NotNull} just because except for when this is being called super early it is never {@code null}.
     * @see #ROBIT_SKIN_SERIALIZER_REGISTRY_NAME
     * @since 10.4.0
     */
    public static IForgeRegistry<Codec<? extends RobitSkin>> robitSkinSerializerRegistry() {
        if (ROBIT_SKIN_SERIALIZER_REGISTRY == null) {
            ROBIT_SKIN_SERIALIZER_REGISTRY = RegistryManager.ACTIVE.getRegistry(ROBIT_SKIN_SERIALIZER_REGISTRY_NAME);
        }
        return ROBIT_SKIN_SERIALIZER_REGISTRY;
    }
}