package mekanism.additions.common.loot;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import mekanism.common.loot.BaseLootProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class AdditionsLootProvider extends BaseLootProvider {

    public AdditionsLootProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, List.of(
              new SubProviderEntry(AdditionsBlockLootTables::new, LootContextParamSets.BLOCK),
              new SubProviderEntry(AdditionsEntityLootTables::new, LootContextParamSets.ENTITY)
        ), provider);
    }
}