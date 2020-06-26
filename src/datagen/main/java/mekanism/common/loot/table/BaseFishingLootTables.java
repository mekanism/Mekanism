package mekanism.common.loot.table;

import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import net.minecraft.data.loot.FishingLootTables;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.util.ResourceLocation;

public abstract class BaseFishingLootTables extends FishingLootTables {

    @Override
    public abstract void accept(@Nonnull BiConsumer<ResourceLocation, Builder> consumer);
}