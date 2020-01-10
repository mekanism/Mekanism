package mekanism.common.loot;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.loot.table.BaseBlockLootTable;
import mekanism.common.loot.table.BaseChestLootTable;
import mekanism.common.loot.table.BaseEntityLootTable;
import mekanism.common.loot.table.BaseFishingLootTable;
import mekanism.common.loot.table.BaseGiftLootTable;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootParameterSet;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTable.Builder;
import net.minecraft.world.storage.loot.ValidationTracker;

//TODO: Override getName?
public abstract class BaseLootGenerator extends LootTableProvider {

    public BaseLootGenerator(DataGenerator gen) {
        super(gen);
    }

    @Nullable
    protected BaseBlockLootTable getBlockLootTable() {
        return null;
    }

    @Nullable
    protected BaseChestLootTable getChestLootTable() {
        return null;
    }

    @Nullable
    protected BaseEntityLootTable getEntityLootTable() {
        return null;
    }

    @Nullable
    protected BaseFishingLootTable getFishingLootTable() {
        return null;
    }

    @Nullable
    protected BaseGiftLootTable getGiftLootTable() {
        return null;
    }

    @Nonnull
    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> getTables() {
        ImmutableList.Builder<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> builder = new ImmutableList.Builder<>();
        BaseBlockLootTable blockLootTable = getBlockLootTable();
        if (blockLootTable != null) {
            builder.add(Pair.of(() -> blockLootTable, LootParameterSets.BLOCK));
        }
        BaseChestLootTable chestLootTable = getChestLootTable();
        if (chestLootTable != null) {
            builder.add(Pair.of(() -> chestLootTable, LootParameterSets.CHEST));
        }
        BaseEntityLootTable entityLootTable = getEntityLootTable();
        if (entityLootTable != null) {
            builder.add(Pair.of(() -> entityLootTable, LootParameterSets.ENTITY));
        }
        BaseFishingLootTable fishingLootTable = getFishingLootTable();
        if (fishingLootTable != null) {
            builder.add(Pair.of(() -> fishingLootTable, LootParameterSets.FISHING));
        }
        BaseGiftLootTable giftLootTable = getGiftLootTable();
        if (giftLootTable != null) {
            builder.add(Pair.of(() -> giftLootTable, LootParameterSets.GIFT));
        }
        return builder.build();
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
        //NO-OP, as we don't
    }
}