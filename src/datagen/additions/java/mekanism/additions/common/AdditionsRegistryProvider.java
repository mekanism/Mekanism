package mekanism.additions.common;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.loot.AdditionsBlockLootTables;
import mekanism.additions.common.loot.AdditionsEntityLootTables;
import mekanism.additions.common.recipe.AdditionsRecipeProvider;
import mekanism.additions.common.world.modifier.BabyEntitySpawnBiomeModifier;
import mekanism.additions.common.world.modifier.BabyEntitySpawnStructureModifier;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.registries.BaseRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AdditionsRegistryProvider extends BaseRegistryProvider {

    public static DatapackBuiltinEntriesProvider forWorldLayer(PackOutput output, CompletableFuture<HolderLookup.Provider> worldRegistries) {
        return forWorldLayer(output, worldRegistries, MekanismAdditions.MODID, new RegistrySetBuilder()
              .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                  for (BabyType babyType : BabyType.VALUES) {
                      context.register(biomeModifier(babyType.id()), new BabyEntitySpawnBiomeModifier(babyType));
                  }
              })
              .add(NeoForgeRegistries.Keys.STRUCTURE_MODIFIERS, context -> {
                  for (BabyType babyType : BabyType.VALUES) {
                      context.register(structureModifier(babyType.id()), new BabyEntitySpawnStructureModifier(babyType));
                  }
              })
        );
    }

    public static DatapackBuiltinEntriesProvider forReloadableLayer(PackOutput output, CompletableFuture<HolderLookup.Provider> worldRegistries,
          CompletableFuture<HolderLookup.Provider> reloadableRegistries) {
        return forReloadableLayer(output, worldRegistries, reloadableRegistries, MekanismAdditions.MODID, new RegistrySetBuilder()
              .add(Registries.LOOT_TABLE, new LootTableProvider(Collections.emptySet(), List.of(
                    new SubProviderEntry(AdditionsBlockLootTables::new, LootContextParamSets.BLOCK),
                    new SubProviderEntry(AdditionsEntityLootTables::new, LootContextParamSets.ENTITY)
              )))
              .add(Registries.ADVANCEMENT, new AdvancementProvider(List.of(AdditionsAdvancementProvider::new)))
              .add(BaseRecipeProvider.registerRecipes(AdditionsRecipeProvider::new))
        );
    }
}