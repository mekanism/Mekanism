package mekanism.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import mekanism.client.lang.MekanismLangProvider;
import mekanism.client.model.MekanismItemModelProvider;
import mekanism.client.sound.MekanismSoundProvider;
import mekanism.client.state.MekanismBlockStateProvider;
import mekanism.client.texture.MekanismSpriteSourceProvider;
import mekanism.client.texture.PrideRobitTextureProvider;
import mekanism.common.advancements.MekanismAdvancementProvider;
import mekanism.common.integration.computer.ComputerHelpProvider;
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
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Mekanism.MODID, bus = EventBusSubscriber.Bus.MOD)
public class MekanismDataGenerator {

    private MekanismDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        bootstrapConfigs(Mekanism.MODID);
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
    public static void bootstrapConfigs(String modid) {
        for (Set<ModConfig> configs : ConfigTracker.INSTANCE.configSets().values()) {
            for (ModConfig config : configs) {
                if (config.getModId().equals(modid)) {
                    //Similar to how ConfigTracker#loadDefaultServerConfigs works for loading default server configs on the client
                    // except we don't bother firing an event as it is private, and we are already at defaults if we had called earlier,
                    // and we also don't fully initialize the mod config as the spec is what we care about, and we can do so without having
                    // to reflect into package private methods
                    CommentedConfig commentedConfig = CommentedConfig.inMemory();
                    config.getSpec().correct(commentedConfig);
                    config.getSpec().acceptConfig(commentedConfig);
                }
            }
        }
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