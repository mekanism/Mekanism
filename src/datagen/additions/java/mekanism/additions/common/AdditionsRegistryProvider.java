package mekanism.additions.common;

import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.loot.AdditionsBlockLootTables;
import mekanism.additions.common.loot.AdditionsEntityLootTables;
import mekanism.additions.common.recipe.AdditionsRecipeProvider;
import mekanism.additions.common.world.modifier.BabyEntitySpawnBiomeModifier;
import mekanism.additions.common.world.modifier.BabyEntitySpawnStructureModifier;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.registries.BaseRegistryProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid = MekanismAdditions.MODID)
public class AdditionsRegistryProvider extends BaseRegistryProvider {

    private AdditionsRegistryProvider() {
    }

    @SubscribeEvent
    public static void onGatherRegistries(GatherDataRegistryEntriesEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismAdditions.MODID);
        event.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                  for (BabyType babyType : BabyType.VALUES) {
                      context.register(biomeModifier(babyType.id()), new BabyEntitySpawnBiomeModifier(babyType));
                  }
              })
              .add(NeoForgeRegistries.Keys.STRUCTURE_MODIFIERS, context -> {
                  for (BabyType babyType : BabyType.VALUES) {
                      context.register(structureModifier(babyType.id()), new BabyEntitySpawnStructureModifier(babyType));
                  }
              })
              .lootTable(
                    new SubProviderEntry(AdditionsBlockLootTables::new, LootContextParamSets.BLOCK),
                    new SubProviderEntry(AdditionsEntityLootTables::new, LootContextParamSets.ENTITY)
              )
              .advancement(AdditionsAdvancementProvider::new)
              .recipe(AdditionsRecipeProvider::new)
        ;
    }
}