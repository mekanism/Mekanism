package mekanism.generators.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.PersistingDisabledProvidersProvider;
import mekanism.common.recipe.MekRecipeRunner;
import mekanism.generators.client.GeneratorsLangProvider;
import mekanism.generators.client.GeneratorsModelProvider;
import mekanism.generators.client.GeneratorsSoundProvider;
import mekanism.generators.client.GeneratorsSpriteSourceProvider;
import mekanism.generators.client.integration.emi.GeneratorsEmiDefaults;
import mekanism.generators.client.recipe_viewer.alias.GeneratorsAliasMapping;
import mekanism.generators.common.loot.GeneratorsLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MekanismGenerators.MODID)
public class GeneratorsDataGenerator {

    private GeneratorsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismGenerators.MODID);
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        GeneratorsDatapackRegistryProvider drProvider = new GeneratorsDatapackRegistryProvider(output, event.getLookupProvider());
        CompletableFuture<HolderLookup.Provider> lookupProvider = drProvider.getRegistryProvider();
        ResourceManager clientResources = event.getResourceManager(PackType.CLIENT_RESOURCES);
        //Client side data generators
        gen.addProvider(true, new GeneratorsLangProvider(output));
        gen.addProvider(true, new GeneratorsSoundProvider(output));
        gen.addProvider(true, new GeneratorsSpriteSourceProvider(output, lookupProvider));
        gen.addProvider(true, new GeneratorsModelProvider(output, clientResources));
        //Server side data generators
        gen.addProvider(true, new GeneratorsTagProvider(output, lookupProvider));
        gen.addProvider(true, new GeneratorsLootProvider(output, lookupProvider));
        gen.addProvider(true, drProvider);
        gen.addProvider(true, new MekRecipeRunner(output, lookupProvider, GeneratorsRecipeProvider::new, MekanismGenerators.MODID));
        gen.addProvider(true, new AdvancementProvider(output, lookupProvider, List.of(new GeneratorsAdvancementProvider())));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(event, lookupProvider, MekanismGenerators.MODID, GeneratorsAliasMapping::new,
              () -> GeneratorsEmiDefaults::new);
    }
}