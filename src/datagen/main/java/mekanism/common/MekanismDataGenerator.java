package mekanism.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import java.util.concurrent.CompletableFuture;
import mekanism.client.lang.MekanismLangProvider;
import mekanism.client.model.MekanismItemModelProvider;
import mekanism.client.sound.MekanismSoundProvider;
import mekanism.client.state.MekanismBlockStateProvider;
import mekanism.client.texture.MekanismSpriteSourceProvider;
import mekanism.client.texture.PrideRobitTextureProvider;
import mekanism.common.advancements.MekanismAdvancementProvider;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.integration.computer.ComputerHelpProvider;
import mekanism.common.integration.crafttweaker.MekanismCrTExampleProvider;
import mekanism.common.loot.MekanismLootProvider;
import mekanism.common.recipe.impl.MekanismRecipeProvider;
import mekanism.common.registries.MekanismDatapackRegistryProvider;
import mekanism.common.tag.MekanismTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(modid = Mekanism.MODID, bus = Bus.MOD)
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
        //Bootstrap our advancement triggers as common setup doesn't run
        MekanismCriteriaTriggers.init();
        gen.addProvider(true, new BasePackMetadataGenerator(output, MekanismLang.PACK_DESCRIPTION));
        //Client side data generators
        addProvider(gen, event.includeClient(), MekanismLangProvider::new);
        gen.addProvider(event.includeClient(), new PrideRobitTextureProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new MekanismSoundProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new MekanismSpriteSourceProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new MekanismItemModelProvider(output, existingFileHelper));
        gen.addProvider(event.includeClient(), new MekanismBlockStateProvider(output, existingFileHelper));
        //Server side data generators
        gen.addProvider(event.includeServer(), new MekanismTagProvider(output, lookupProvider, existingFileHelper));
        addProvider(gen, event.includeServer(), MekanismLootProvider::new);
        gen.addProvider(event.includeServer(), drProvider);
        MekanismRecipeProvider recipeProvider = new MekanismRecipeProvider(output, existingFileHelper);
        gen.addProvider(event.includeServer(), recipeProvider);
        gen.addProvider(event.includeServer(), new MekanismAdvancementProvider(output, existingFileHelper));
        //TODO - 1.20: Re-enable after updating ProjectE
        //addProvider(gen, event.includeServer(), MekanismCustomConversions::new);
        gen.addProvider(event.includeServer(), new MekanismCrTExampleProvider(output, existingFileHelper));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        gen.addProvider(true, new PersistingDisabledProvidersProvider(output, recipeProvider.getDisabledCompats()));
        gen.addProvider(event.includeServer(), new ComputerHelpProvider(output, Mekanism.MODID));
    }

    public static <PROVIDER extends DataProvider> void addProvider(DataGenerator gen, boolean run, DataProvider.Factory<PROVIDER> factory) {
        gen.addProvider(run, factory);
    }

    /**
     * Used to bootstrap configs to their default values so that if we are querying if things exist we don't have issues with it happening to early or in cases we have
     * fake tiles.
     */
    public static void bootstrapConfigs(String modid) {
        ConfigTracker.INSTANCE.configSets().forEach((type, configs) -> {
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
        });
    }
}
