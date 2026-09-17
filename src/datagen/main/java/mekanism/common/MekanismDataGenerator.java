package mekanism.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import mekanism.client.integration.emi.MekanismEmiDefaults;
import mekanism.client.lang.MekanismLangProvider;
import mekanism.client.model.MekanismEquipmentAssetProvider;
import mekanism.client.model.MekanismModelProvider;
import mekanism.client.sound.MekanismSoundProvider;
import mekanism.client.texture.MekanismSpriteSourceProvider;
import mekanism.client.texture.PrideRobitTextureProvider;
import mekanism.common.integration.computer.ComputerHelpProvider;
import mekanism.common.lib.FieldReflectionHelper;
import mekanism.common.registries.MekanismRegistryProvider;
import mekanism.common.tag.MekanismTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = Mekanism.MODID)
public class MekanismDataGenerator {

    @SuppressWarnings("UnstableApiUsage")
    private static final FieldReflectionHelper<ConfigTracker, EnumMap<ModConfig.Type, Set<ModConfig>>> CONFIG_SETS =
          new FieldReflectionHelper<>(ConfigTracker.class, "configSets", () -> new EnumMap<>(ModConfig.Type.class));
    private static final Constructor<?> LOADED_CONFIG;
    private static final Method SET_CONFIG;

    static {
        Class<?> loadedConfig;
        try {
            loadedConfig = Class.forName("net.neoforged.fml.config.LoadedConfig");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        LOADED_CONFIG = ObfuscationReflectionHelper.findConstructor(loadedConfig, CommentedConfig.class, Path.class, ModConfig.class);
        SET_CONFIG = ObfuscationReflectionHelper.findMethod(ModConfig.class, "setConfig", loadedConfig, Function.class);
    }

    @Nullable
    private static CompletableFuture<HolderLookup.Provider> reloadableLookupProvider = null;
    @Nullable
    private static CompletableFuture<HolderLookup.Provider> worldLookupProvider = null;

    private MekanismDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        bootstrapConfigs(Mekanism.MODID);
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        DatapackBuiltinEntriesProvider worldRegistryProvider = MekanismRegistryProvider.forWorldLayer(output, event.getWorldLookupProvider());
        worldLookupProvider = worldRegistryProvider.getRegistryProvider();
        HashSet<String> disabledCompats = new HashSet<>();
        DatapackBuiltinEntriesProvider reloadableRegistryProvider = MekanismRegistryProvider.forReloadableLayer(output, worldLookupProvider, event.getReloadableLookupProvider(), disabledCompats);
        reloadableLookupProvider = reloadableRegistryProvider.getRegistryProvider();

        ResourceManager clientResources = event.getResourceManager(PackType.CLIENT_RESOURCES);
        //Client side data generators
        gen.addProvider(true, new MekanismLangProvider(output));
        gen.addProvider(true, new PrideRobitTextureProvider(output, clientResources));
        gen.addProvider(true, new MekanismSoundProvider(output));
        gen.addProvider(true, new MekanismSpriteSourceProvider(output, worldLookupProvider));
        gen.addProvider(true, new MekanismModelProvider(output, clientResources));
        gen.addProvider(true, new MekanismEquipmentAssetProvider(output));
        //Server side data generators
        gen.addProvider(true, new MekanismTagProvider(output, reloadableLookupProvider));
        gen.addProvider(true, worldRegistryProvider);
        gen.addProvider(true, reloadableRegistryProvider);
        gen.addProvider(true, new MekanismDataMapsProvider(output, reloadableLookupProvider));
        gen.addProvider(true, new ComputerHelpProvider(output, reloadableLookupProvider, Mekanism.MODID));
        gen.addProvider(true, new MekanismEmiDefaults(output, event.getResourceManager(PackType.SERVER_DATA), reloadableLookupProvider));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisableableProviders(event, reloadableLookupProvider, disabledCompats);
    }

    public static CompletableFuture<HolderLookup.Provider> getReloadableLookupProvider() {
        return Objects.requireNonNull(reloadableLookupProvider);
    }

    public static CompletableFuture<HolderLookup.Provider> getWorldLookupProvider() {
        return Objects.requireNonNull(worldLookupProvider);
    }

    /// Used to bootstrap configs to their default values so that if we are querying if things exist we don't have issues with it happening to early or in cases we have
    /// fake tiles.
    @SuppressWarnings("UnstableApiUsage")
    public static void bootstrapConfigs(String modid) {
        for (Set<ModConfig> configs : CONFIG_SETS.getValue(ConfigTracker.INSTANCE).values()) {
            for (ModConfig config : configs) {
                if (config.getModId().equals(modid)) {
                    //Similar to how ConfigTracker#loadDefaultServerConfigs works for loading default server configs on the client
                    // except we don't bother firing an event as it is private, and we are already at defaults if we had called earlier,
                    // and we also don't fully initialize the mod config as the spec is what we care about, and we can do so without having
                    // to reflect into package private methods
                    CommentedConfig commentedConfig = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(), LinkedHashMap::new);
                    config.getSpec().correct(commentedConfig);
                    try {
                        SET_CONFIG.invoke(config, LOADED_CONFIG.newInstance(commentedConfig, null, config),
                              (Function<ModConfig, ModConfigEvent>) ModConfigEvent.Loading::new);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /// Basically a copy of [DataProvider#saveStable(CachedOutput, JsonElement, Path)] but it takes a consumer of the output stream instead of serializes json using GSON.
    /// Use it to write arbitrary files.
    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public static CompletableFuture<?> save(CachedOutput cache, IOConsumer<OutputStream> osConsumer, Path path) {
        return CompletableFuture.runAsync(() -> {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), outputStream)) {
                osConsumer.accept(hashingOutputStream);
                cache.writeIfNeeded(path, outputStream.toByteArray(), hashingOutputStream.hash());
            } catch (IOException ioexception) {
                DataProvider.LOGGER.error("Failed to save file to {}", path, ioexception);
            }
        }, Util.backgroundExecutor());
    }

    @FunctionalInterface
    public interface IOConsumer<T> {

        void accept(T value) throws IOException;
    }
}