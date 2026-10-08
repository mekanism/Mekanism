package mekanism.common.registries;

import java.util.HashSet;
import java.util.Set;
import mekanism.common.Mekanism;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.advancements.MekanismAdvancementProvider;
import mekanism.common.loot.MekanismBlockLootTables;
import mekanism.common.loot.MekanismEntityLootTables;
import mekanism.common.recipe.impl.MekanismRecipeProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = Mekanism.MODID)
public class MekanismRegistryProvider extends BaseRegistryProvider {

    public static final Set<String> DISABLED_COMPATS = new HashSet<>();

    private MekanismRegistryProvider() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onGatherRegistries(GatherDataRegistryEntriesEvent event) {
        MekanismDataGenerator.bootstrapConfigs(Mekanism.MODID);
        event.lootTable(
                    new SubProviderEntry(MekanismBlockLootTables::new, LootContextParamSets.BLOCK),
                    new SubProviderEntry(MekanismEntityLootTables::new, LootContextParamSets.ENTITY)
              )
              .advancement(MekanismAdvancementProvider::new)
              .recipe((recipeOutput, advancementOutput) -> new MekanismRecipeProvider(recipeOutput, advancementOutput, DISABLED_COMPATS))
        ;
    }
}