package mekanism.additions.common;

import mekanism.additions.client.AdditionsLangProvider;
import mekanism.additions.client.AdditionsModelProvider;
import mekanism.additions.client.AdditionsSoundProvider;
import mekanism.additions.client.AdditionsSplashProvider;
import mekanism.additions.client.AdditionsSpriteSourceProvider;
import mekanism.additions.client.integration.emi.AdditionsEmiDefaults;
import mekanism.additions.client.recipe_viewer.aliases.AdditionsAliasMapping;
import mekanism.additions.common.loot.AdditionsBlockLootTables;
import mekanism.additions.common.loot.AdditionsEntityLootTables;
import mekanism.additions.common.recipe.AdditionsRecipeProvider;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.PersistingDisabledProvidersProvider;
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

@EventBusSubscriber(modid = MekanismAdditions.MODID)
public class AdditionsDataGenerator {

    private AdditionsDataGenerator() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        PackGenerator packGenerator = event.getDefaultPackGenerator();
        //Client side data generators
        packGenerator.createProvider(output -> PackMetadataGenerator.forFeaturePack(output, AdditionsLang.PACK_DESCRIPTION.translate()));
        packGenerator.createProvider(AdditionsLangProvider::new);
        packGenerator.createProvider(AdditionsSoundProvider::new);
        packGenerator.createProvider(AdditionsSpriteSourceProvider::new);
        packGenerator.createProvider(output -> new AdditionsModelProvider(output, event.getResourceManager(PackType.CLIENT_RESOURCES)));
        packGenerator.createProvider(AdditionsSplashProvider::new);
        //Server side data generators
        packGenerator.createProvider(AdditionsTagProvider::new);
        packGenerator.createProvider(AdditionsDataMapsProvider::new);
        packGenerator.createProvider(AdditionsEmiDefaults::new);
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        PersistingDisabledProvidersProvider.addDisabledEmiProvider(packGenerator, event.getModContainer(), AdditionsAliasMapping::new);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGatherRegistries(GatherDataRegistryEntriesEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismAdditions.MODID);
        event.lootTable(
                    new SubProviderEntry(AdditionsBlockLootTables::new, LootContextParamSets.BLOCK),
                    new SubProviderEntry(AdditionsEntityLootTables::new, LootContextParamSets.ENTITY)
              )
              .advancement(AdditionsAdvancementProvider::new)
              .recipe(AdditionsRecipeProvider::new)
        ;
    }
}