package mekanism.common.loot;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public abstract class BaseLootProvider extends LootTableProvider {

    protected BaseLootProvider(PackOutput output, List<LootTableProvider.SubProviderEntry> subProviders, CompletableFuture<HolderLookup.Provider> provider) {
        this(output, Collections.emptySet(), subProviders, provider);
    }

    protected BaseLootProvider(PackOutput output, Set<ResourceKey<LootTable>> requiredTables, List<LootTableProvider.SubProviderEntry> subProviders,
          CompletableFuture<HolderLookup.Provider> provider) {
        super(output, requiredTables, subProviders, provider);
    }
}