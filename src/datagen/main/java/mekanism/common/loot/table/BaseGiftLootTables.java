package mekanism.common.loot.table;

import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import net.minecraft.data.loot.GiftLootTables;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.util.ResourceLocation;

public abstract class BaseGiftLootTables extends GiftLootTables {

    @Override
    public abstract void accept(@Nonnull BiConsumer<ResourceLocation, Builder> consumer);
}