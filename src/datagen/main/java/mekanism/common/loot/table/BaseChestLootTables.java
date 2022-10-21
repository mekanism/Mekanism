package mekanism.common.loot.table;

import java.util.function.BiConsumer;
import net.minecraft.data.loot.ChestLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import org.jetbrains.annotations.NotNull;

public abstract class BaseChestLootTables extends ChestLoot {

    @Override
    public abstract void accept(@NotNull BiConsumer<ResourceLocation, Builder> consumer);
}