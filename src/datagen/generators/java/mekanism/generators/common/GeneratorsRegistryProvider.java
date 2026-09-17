package mekanism.generators.common;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.BasicChemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.registration.impl.MekanismDamageType;
import mekanism.common.registries.BaseRegistryProvider;
import mekanism.generators.common.loot.GeneratorsBlockLootTables;
import mekanism.generators.common.registries.GeneratorsDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

public class GeneratorsRegistryProvider extends BaseRegistryProvider {

    public static DatapackBuiltinEntriesProvider forWorldLayer(PackOutput output, CompletableFuture<HolderLookup.Provider> worldRegistries) {
        return forWorldLayer(output, worldRegistries, MekanismGenerators.MODID, new RegistrySetBuilder()
              .add(Registries.DAMAGE_TYPE, context -> {
                  for (MekanismDamageType damageType : GeneratorsDamageTypes.DAMAGE_TYPES.damageTypes()) {
                      context.register(damageType.key(), damageType.toVanilla());
                  }
              })
              .add(MekanismRegistries.Keys.CHEMICAL, context -> {
                  for (GeneratorsChemicalConstants constant : GeneratorsChemicalConstants.values()) {
                      registerConstant(context, constant);
                  }
                  context.register(ChemicalIds.TRITIUM, BasicChemical.builder().tint(0xFF64FF70).build());
                  context.register(ChemicalIds.FUSION_FUEL, BasicChemical.builder().tint(0xFF7E007D).build());
              })
        );
    }

    public static DatapackBuiltinEntriesProvider forReloadableLayer(PackOutput output, CompletableFuture<HolderLookup.Provider> worldRegistries,
          CompletableFuture<HolderLookup.Provider> reloadableRegistries) {
        return forReloadableLayer(output, worldRegistries, reloadableRegistries, MekanismGenerators.MODID, new RegistrySetBuilder()
              .add(Registries.LOOT_TABLE, new LootTableProvider(Collections.emptySet(), List.of(
                    new SubProviderEntry(GeneratorsBlockLootTables::new, LootContextParamSets.BLOCK)
              )))
              .add(Registries.ADVANCEMENT, new AdvancementProvider(List.of(GeneratorsAdvancementProvider::new)))
              .add(BaseRecipeProvider.registerRecipes(GeneratorsRecipeProvider::new))
        );
    }
}