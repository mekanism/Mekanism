package mekanism.generators.common;

import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.BasicChemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.common.MekanismDataGenerator;
import mekanism.common.registration.impl.MekanismDamageType;
import mekanism.common.registries.BaseRegistryProvider;
import mekanism.generators.common.loot.GeneratorsBlockLootTables;
import mekanism.generators.common.registries.GeneratorsDamageTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider.SubProviderEntry;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = MekanismGenerators.MODID)
public class GeneratorsRegistryProvider extends BaseRegistryProvider {

    private GeneratorsRegistryProvider() {
    }

    @SubscribeEvent
    public static void onGatherRegistries(GatherDataRegistryEntriesEvent event) {
        MekanismDataGenerator.bootstrapConfigs(MekanismGenerators.MODID);
        event.add(Registries.DAMAGE_TYPE, context -> {
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
              .lootTable(new SubProviderEntry(GeneratorsBlockLootTables::new, LootContextParamSets.BLOCK))
              .advancement(GeneratorsAdvancementProvider::new)
              .recipe(GeneratorsRecipeProvider::new)
        ;
    }
}