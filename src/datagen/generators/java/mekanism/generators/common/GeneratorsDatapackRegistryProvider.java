package mekanism.generators.common;

import java.util.concurrent.CompletableFuture;
import mekanism.common.registration.impl.MekanismDamageType;
import mekanism.common.registries.BaseDatapackRegistryProvider;
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
          });
}