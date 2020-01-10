package mekanism.common.loot.table;

import java.util.function.BiConsumer;
import net.minecraft.data.loot.GiftLootTables;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable.Builder;

//TODO: Implement/improve this when needed
public abstract class BaseGiftLootTable extends GiftLootTables {

    @Override
    public abstract void accept(BiConsumer<ResourceLocation, Builder> consumer);
}