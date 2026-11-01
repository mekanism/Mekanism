package mekanism.generators.common;

import mekanism.common.MekanismDataGenerator;
import mekanism.common.PersistingDisabledProvidersProvider;
import mekanism.generators.client.GeneratorsLangProvider;
import mekanism.generators.client.GeneratorsModelProvider;
import mekanism.generators.client.GeneratorsSoundProvider;
import mekanism.generators.client.GeneratorsSplashProvider;
import mekanism.generators.client.integration.emi.GeneratorsEmiDefaults;
import mekanism.generators.client.recipe_viewer.alias.GeneratorsAliasMapping;
import mekanism.generators.common.loot.GeneratorsBlockLootTables;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;
import net.neoforged.neoforge.data.event.PackGenerator;

@EventBusSubscriber(modid = MekanismGenerators.MODID)
public class GeneratorsDataGenerator {

    private GeneratorsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        PackGenerator packGenerator = event.getDefaultPackGenerator();
        //Client side data generators
        packGenerator.createProvider(output -> PackMetadataGenerator.forFeaturePack(output, GeneratorsLang.PACK_DESCRIPTION.translate()));
        packGenerator.createProvider(GeneratorsLangProvider::new);
        packGenerator.createProvider(GeneratorsSoundProvider::new);
        packGenerator.createProvider(output -> new GeneratorsModelProvider(output, event.getResourceManager(PackType.CLIENT_RESOURCES)));
        packGenerator.createProvider(GeneratorsSplashProvider::new);
        //Server side data generators
        packGenerator.createProvider(GeneratorsTagProvider::new);
        packGenerator.createProvider(GeneratorsDataMapsProvider::new);
        packGenerator.createProvider(GeneratorsEmiDefaults::new);
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(packGenerator, event.getModContainer(), GeneratorsAliasMapping::new);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGatherRegistries(GatherDataRegistryEntriesEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismGenerators.MODID);
        event.lootTable(new SubProviderEntry(GeneratorsBlockLootTables::new, LootContextParamSets.BLOCK))
              .advancement(GeneratorsAdvancementProvider::new)
              .recipe(GeneratorsRecipeProvider::new)
        ;
    }
}