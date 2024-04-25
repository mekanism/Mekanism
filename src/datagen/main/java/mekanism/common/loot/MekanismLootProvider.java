package mekanism.common.loot;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.common.loot.table.MekanismBlockLootTables;
import mekanism.common.loot.table.MekanismEntityLootTables;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class MekanismLootProvider extends BaseLootProvider {

    public MekanismLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, List.of(
              new SubProviderEntry(MekanismBlockLootTables::new, LootContextParamSets.BLOCK),
              new SubProviderEntry(MekanismEntityLootTables::new, LootContextParamSets.ENTITY)
        ), provider);
    }
}