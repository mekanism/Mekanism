package mekanism.additions.common;

import mekanism.additions.common.loot.AdditionsBlockLootTables;
import mekanism.additions.common.loot.AdditionsEntityLootTables;
import mekanism.additions.common.recipe.AdditionsRecipeProvider;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.registries.BaseRegistryProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = MekanismAdditions.MODID)
public class AdditionsRegistryProvider extends BaseRegistryProvider {

    private AdditionsRegistryProvider() {
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