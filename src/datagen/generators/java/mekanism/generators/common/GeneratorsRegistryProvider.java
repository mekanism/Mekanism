package mekanism.generators.common;

import mekanism.common.MekanismDataGenerator;
import mekanism.common.registries.BaseRegistryProvider;
import mekanism.generators.common.loot.GeneratorsBlockLootTables;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = MekanismGenerators.MODID)
public class GeneratorsRegistryProvider extends BaseRegistryProvider {

    private GeneratorsRegistryProvider() {
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