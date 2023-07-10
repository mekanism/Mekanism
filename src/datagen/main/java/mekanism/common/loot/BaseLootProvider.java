package mekanism.common.loot;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseLootProvider extends LootTableProvider {

    protected BaseLootProvider(PackOutput output, List<LootTableProvider.SubProviderEntry> subProviders) {
        this(output, Collections.emptySet(), subProviders);
    }

    protected BaseLootProvider(PackOutput output, Set<ResourceLocation> requiredTables, List<LootTableProvider.SubProviderEntry> subProviders) {
        super(output, requiredTables, subProviders);
    }
}