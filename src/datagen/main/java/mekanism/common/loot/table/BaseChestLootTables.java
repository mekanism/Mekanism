package mekanism.common.loot.table;

import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import net.minecraft.data.loot.ChestLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable.Builder;

public abstract class BaseChestLootTables extends ChestLoot {

    @Override
    public abstract void accept(@Nonnull BiConsumer<ResourceLocation, Builder> consumer);
}