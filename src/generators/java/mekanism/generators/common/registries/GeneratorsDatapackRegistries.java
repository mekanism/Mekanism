package mekanism.generators.common.registries;

import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.BasicChemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.common.registration.impl.MekanismDamageType;
import mekanism.common.util.DatapackRegistryUtils;
import mekanism.generators.common.GeneratorsChemicalConstants;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

public class GeneratorsDatapackRegistries {

    private GeneratorsDatapackRegistries() {
    }

    public static void register(GatherDataRegistryEntriesEvent event) {
        event.add(Registries.DAMAGE_TYPE, context -> {
                  for (MekanismDamageType damageType : GeneratorsDamageTypes.DAMAGE_TYPES.damageTypes()) {
                      context.register(damageType.key(), damageType.toVanilla());
                  }
              })
              .add(MekanismRegistries.Keys.CHEMICAL, context -> {
                  for (GeneratorsChemicalConstants constant : GeneratorsChemicalConstants.values()) {
                      DatapackRegistryUtils.registerConstant(context, constant);
                  }
                  context.register(ChemicalIds.TRITIUM, BasicChemical.builder().tint(0xFF64FF70).build());
                  context.register(ChemicalIds.FUSION_FUEL, BasicChemical.builder().tint(0xFF7E007D).build());
              })
        ;
    }
}