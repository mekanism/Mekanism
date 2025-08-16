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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import mekanism.client.lang.MekanismLangProvider;
import mekanism.client.model.MekanismItemModelProvider;
import mekanism.client.sound.MekanismSoundProvider;
import mekanism.client.state.MekanismBlockStateProvider;
import mekanism.client.texture.MekanismSpriteSourceProvider;
import mekanism.client.texture.PrideRobitTextureProvider;
import mekanism.common.advancements.MekanismAdvancementProvider;
import mekanism.common.integration.computer.ComputerHelpProvider;
import mekanism.common.lib.FieldReflectionHelper;
import mekanism.common.loot.MekanismLootProvider;
import mekanism.common.recipe.impl.MekanismRecipeProvider;
import mekanism.common.registries.MekanismDatapackRegistryProvider;
import mekanism.common.tag.MekanismTagProvider;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.DeferredWorkQueue;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

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


    private MekanismDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        bootstrapConfigs(Mekanism.MODID);
        bootstrapIMC();
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        MekanismDatapackRegistryProvider drProvider = new MekanismDatapackRegistryProvider(output, event.getLookupProvider());
        CompletableFuture<HolderLookup.Provider> lookupProvider = drProvider.getRegistryProvider();
        gen.addProvider(true, new BasePackMetadataGenerator(output, MekanismLang.PACK_DESCRIPTION));
        //Client side data generators
        gen.addProvider(event.includeClient(), new MekanismLangProvider(output));
        gen.addProvider(event.includeClient(), new PrideRobitTextureProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new MekanismSoundProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new MekanismSpriteSourceProvider(output, existingFileHelper, lookupProvider));
        gen.addProvider(event.includeClient(), new MekanismItemModelProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new MekanismBlockStateProvider(output, existingFileHelper));
        //Server side data generators
        gen.addProvider(event.includeServer(), new MekanismTagProvider(output, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeServer(), new MekanismLootProvider(output, lookupProvider));
        gen.addProvider(event.includeServer(), drProvider);
        gen.addProvider(event.includeServer(), new MekanismDataMapsProvider(output, lookupProvider));
        MekanismRecipeProvider recipeProvider = new MekanismRecipeProvider(output, lookupProvider, existingFileHelper);
        gen.addProvider(event.includeServer(), recipeProvider);
        gen.addProvider(event.includeServer(), new MekanismAdvancementProvider(output, lookupProvider, existingFileHelper));
        gen.addProvider(event.includeServer(), new ComputerHelpProvider(output, lookupProvider, Mekanism.MODID));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisableableProviders(event, lookupProvider, recipeProvider.getDisabledCompats());
    }

    /**
     * Used to bootstrap configs to their default values so that if we are querying if things exist we don't have issues with it happening to early or in cases we have
     * fake tiles.
     */
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

    private static void bootstrapIMC() {
        List<ModContainer> mods = new ArrayList<>();
        DeferredWorkQueue enqueueIMC = new DeferredWorkQueue("IMC Bootstrap: Enqueue IMC");
        for (ModContainer mod : ModList.get().getSortedMods()) {
            //Handle all our modules
            if (mod.getModId().startsWith(Mekanism.MODID)) {
                mods.add(mod);
                mod.getEventBus().post(new InterModEnqueueEvent(mod, enqueueIMC));
            }
        }
        enqueueIMC.runTasks();
        DeferredWorkQueue processIMC = new DeferredWorkQueue("IMC Bootstrap: Process IMC");
        for (ModContainer mod : mods) {
            mod.getEventBus().post(new InterModProcessEvent(mod, processIMC));
        }
        processIMC.runTasks();
    }

    /**
     * Basically a copy of {@link DataProvider#saveStable(CachedOutput, JsonElement, Path)} but it takes a consumer of the output stream instead of serializes json using
     * GSON. Use it to write arbitrary files.
     */
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