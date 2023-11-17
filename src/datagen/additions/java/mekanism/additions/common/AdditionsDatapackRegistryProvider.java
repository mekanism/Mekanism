package mekanism.additions.common;

import java.util.concurrent.CompletableFuture;
import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.world.modifier.BabyEntitySpawnBiomeModifier;
import mekanism.additions.common.world.modifier.BabyEntitySpawnStructureModifier;
import mekanism.common.registries.BaseDatapackRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AdditionsDatapackRegistryProvider extends BaseDatapackRegistryProvider {

    public AdditionsDatapackRegistryProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, BUILDER, MekanismAdditions.MODID);
    }

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
          .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
              for (BabyType babyType : BabyType.values()) {
                  context.register(biomeModifier(MekanismAdditions.rl(babyType.getSerializedName())), new BabyEntitySpawnBiomeModifier(babyType));
              }
          })
          .add(NeoForgeRegistries.Keys.STRUCTURE_MODIFIERS, context -> {
              for (BabyType babyType : BabyType.values()) {
                  context.register(structureModifier(MekanismAdditions.rl(babyType.getSerializedName())), new BabyEntitySpawnStructureModifier(babyType));
              }
          });
}