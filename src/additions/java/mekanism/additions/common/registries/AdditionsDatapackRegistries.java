package mekanism.additions.common.registries;

import mekanism.additions.common.entity.baby.BabyType;
import mekanism.additions.common.world.modifier.BabyEntitySpawnBiomeModifier;
import mekanism.additions.common.world.modifier.BabyEntitySpawnStructureModifier;
import mekanism.common.util.DatapackRegistryUtils;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AdditionsDatapackRegistries {

    private AdditionsDatapackRegistries() {
    }

    public static void register(GatherDataRegistryEntriesEvent event) {
        event.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                  for (BabyType babyType : BabyType.VALUES) {
                      context.register(DatapackRegistryUtils.biomeModifier(babyType.id()), new BabyEntitySpawnBiomeModifier(babyType));
                  }
              })
              .add(NeoForgeRegistries.Keys.STRUCTURE_MODIFIERS, context -> {
                  for (BabyType babyType : BabyType.VALUES) {
                      context.register(DatapackRegistryUtils.structureModifier(babyType.id()), new BabyEntitySpawnStructureModifier(babyType));
                  }
              })
        ;
    }
}