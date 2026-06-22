package mekanism.generators.common;

import java.util.concurrent.CompletableFuture;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.BasicChemical;
import mekanism.common.registration.impl.MekanismDamageType;
import mekanism.common.registries.BaseDatapackRegistryProvider;
import mekanism.generators.common.registries.GeneratorsChemicals;
import mekanism.generators.common.registries.GeneratorsDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;

public class GeneratorsDatapackRegistryProvider extends BaseDatapackRegistryProvider {

    public GeneratorsDatapackRegistryProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, BUILDER, MekanismGenerators.MODID);
    }

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
          .add(Registries.DAMAGE_TYPE, context -> {
              for (MekanismDamageType damageType : GeneratorsDamageTypes.DAMAGE_TYPES.damageTypes()) {
                  context.register(damageType.key(), damageType.toVanilla());
              }
          })
          .add(MekanismAPI.CHEMICAL_REGISTRY_NAME, context -> {
              for (GeneratorsChemicalConstants constant : GeneratorsChemicalConstants.values()) {
                  registerConstant(context, MekanismGenerators.MODID, constant);
              }
              context.register(GeneratorsChemicals.TRITIUM, BasicChemical.defaultIcon(0xFF64FF70));
              context.register(GeneratorsChemicals.FUSION_FUEL, BasicChemical.defaultIcon(0xFF7E007D));
          })
          ;
}