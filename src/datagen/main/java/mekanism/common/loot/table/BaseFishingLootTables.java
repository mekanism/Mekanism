package mekanism.common.loot.table;

import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import net.minecraft.data.loot.FishingLootTables;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable.Builder;

public abstract class BaseFishingLootTables extends FishingLootTables {

    @Override
    public abstract void accept(@Nonnull BiConsumer<ResourceLocation, Builder> consumer);
}