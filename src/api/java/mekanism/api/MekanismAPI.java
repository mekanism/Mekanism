package mekanism.api;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.Iterator;
import java.util.ServiceLoader;
import mekanism.api.chemical.Chemical;
import mekanism.api.gear.ModuleData;
import mekanism.api.recipes.ingredients.chemical.ChemicalIngredient;
import mekanism.api.robit.RobitSkin;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;

public class MekanismAPI {

    private MekanismAPI() {
    }

    /// The version of the api classes - may not always match the mod's version
    public static final String API_VERSION = "10.8.0";
    /// Mekanism's Mod ID
    public static final String MEKANISM_MODID = "mekanism";
    /// Mekanism debug mode
    public static boolean debug = false;
    /// Logger for use in Mekanism's API classes
    public static final Logger logger = LogUtils.getLogger();

    private static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MEKANISM_MODID, path);
    }

    private static <T> ResourceKey<Registry<T>> registryKey(@SuppressWarnings("unused") Class<T> compileTimeTypeValidator, String path) {
        return ResourceKey.createRegistryKey(rl(path));
    }

    private static <T> ResourceKey<Registry<MapCodec<? extends T>>> codecRegistryKey(@SuppressWarnings("unused") Class<T> compileTimeTypeValidator, String path) {
        return ResourceKey.createRegistryKey(rl(path));
    }

    /// Gets the [ResourceKey] representing the name of the Registry for [`chemicals`][Chemical].
    ///
    /// @apiNote When registering [`chemicals`][Chemical] using [DeferredRegister], use this field to get access to the [ResourceKey].
    /// @since 10.7.0
    public static final ResourceKey<Registry<Chemical>> CHEMICAL_REGISTRY_NAME = registryKey(Chemical.class, "chemical");

    /// Constant location representing the name all empty chemicals will be registered under.
    ///
    /// @since 10.6.0
    public static final ResourceKey<Chemical> EMPTY_CHEMICAL_KEY = ResourceKey.create(CHEMICAL_REGISTRY_NAME, rl("empty"));

    /// Gets the [ResourceKey] representing the name of the Registry for [ChemicalIngredient] ingredient type serializers.
    ///
    /// @apiNote When registering chemical ingredient types using [DeferredRegister], use this field to get access to the [ResourceKey].
    /// @since 10.7.0
    public static final ResourceKey<Registry<MapCodec<? extends ChemicalIngredient>>> CHEMICAL_INGREDIENT_TYPE_REGISTRY_NAME = codecRegistryKey(ChemicalIngredient.class, "chemical_ingredient_type");

    /// Gets the [ResourceKey] representing the name of the Registry for [`modules`][ModuleData].
    ///
    /// @apiNote When registering [`modules`][ModuleData] using [DeferredRegister], use this field to get access to the [ResourceKey].
    /// @since 10.4.0
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final ResourceKey<Registry<ModuleData<?>>> MODULE_REGISTRY_NAME = registryKey((Class) ModuleData.class, "module");
    /// Gets the [ResourceKey] representing the name of the Datapack Registry for [`robit skins`][RobitSkin].
    ///
    /// @since 10.4.0
    public static final ResourceKey<Registry<RobitSkin>> ROBIT_SKIN_REGISTRY_NAME = registryKey(RobitSkin.class, "robit_skin");
    /// Gets the [ResourceKey] representing the name of the Registry for [`robit skin`][RobitSkin] serializers.
    ///
    /// @apiNote When registering [`robit skin`][RobitSkin] serializers using [DeferredRegister], use this field to get access to the [ResourceKey].
    /// @since 10.4.0
    public static final ResourceKey<Registry<MapCodec<? extends RobitSkin>>> ROBIT_SKIN_SERIALIZER_REGISTRY_NAME = codecRegistryKey(RobitSkin.class, "robit_skin_serializer");

    /// Gets the Registry for [Chemical].
    ///
    /// @see #CHEMICAL_REGISTRY_NAME
    /// @since 10.7.0
    public static final DefaultedRegistry<Chemical> CHEMICAL_REGISTRY = (DefaultedRegistry<Chemical>) new RegistryBuilder<>(CHEMICAL_REGISTRY_NAME)
          .defaultKey(EMPTY_CHEMICAL_KEY)
          .sync(true)
          .create();

    /// Gets the Registry for [ChemicalIngredient] type serializers.
    ///
    /// @see #CHEMICAL_INGREDIENT_TYPE_REGISTRY_NAME
    /// @since 10.7.0
    public static final Registry<MapCodec<? extends ChemicalIngredient>> CHEMICAL_INGREDIENT_TYPES = new RegistryBuilder<>(CHEMICAL_INGREDIENT_TYPE_REGISTRY_NAME)
          .sync(true)
          .create();

    /// Gets the Registry for [ModuleData].
    ///
    /// @see #MODULE_REGISTRY_NAME
    /// @since 10.5.0
    public static final Registry<ModuleData<?>> MODULE_REGISTRY = new RegistryBuilder<>(MODULE_REGISTRY_NAME)
          .sync(true)
          .create();
    /// Gets the Registry for [RobitSkin] serializers.
    ///
    /// @see #ROBIT_SKIN_SERIALIZER_REGISTRY_NAME
    /// @since 10.5.0
    public static final Registry<MapCodec<? extends RobitSkin>> ROBIT_SKIN_SERIALIZER_REGISTRY = new RegistryBuilder<>(ROBIT_SKIN_SERIALIZER_REGISTRY_NAME)
          .create();

    /// Holder for the empty Chemical instance.
    ///
    /// @since 10.7.11
    public static final Holder<Chemical> EMPTY_CHEMICAL_HOLDER = DeferredHolder.create(EMPTY_CHEMICAL_KEY);

    @Internal
    private static final ClassLoader SERVICE_CL = MekanismAPI.class.getClassLoader();

    /// Loads a Mekanism service from ServiceLoader, ensuring that the correct classloader is used instead of relying on the context classloader, which may not be
    /// correct
    ///
    /// @param serviceClass the interface class to search for
    ///
    /// @return the concrete implementation
    ///
    /// @throws IllegalStateException when an implementation is not found
    @Internal
    public static <SERVICE> SERVICE getService(Class<SERVICE> serviceClass) {
        Iterator<SERVICE> service = ServiceLoader.load(serviceClass, SERVICE_CL).iterator();
        if (service.hasNext()) {
            return service.next();
        }

        IllegalStateException illegalStateException = new IllegalStateException("No valid ServiceImpl for " + serviceClass.getSimpleName() + " found");
        logger.error("Failed to load service", illegalStateException);
        logger.error("CL: {} CCL: {}", SERVICE_CL, Thread.currentThread().getContextClassLoader());
        throw illegalStateException;

    }

}