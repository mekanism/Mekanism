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
import mekanism.common.loot.table.BaseBlockLootTables;
import mekanism.common.loot.table.BaseChestLootTables;
import mekanism.common.loot.table.BaseEntityLootTables;
import mekanism.common.loot.table.BaseFishingLootTables;
import mekanism.common.loot.table.BaseGiftLootTables;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootParameterSet;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTable.Builder;
import net.minecraft.world.storage.loot.ValidationTracker;

public abstract class BaseLootGenerator extends LootTableProvider {

    private final String modid;

    protected BaseLootGenerator(DataGenerator gen, String modid) {
        super(gen);
        this.modid = modid;
    }

    @Nonnull
    @Override
    public String getName() {
        return super.getName() + ": " + modid;
    }

    @Nullable
    protected BaseBlockLootTables getBlockLootTable() {
        return null;
    }

    @Nullable
    protected BaseChestLootTables getChestLootTable() {
        return null;
    }

    @Nullable
    protected BaseEntityLootTables getEntityLootTable() {
        return null;
    }

    @Nullable
    protected BaseFishingLootTables getFishingLootTable() {
        return null;
    }

    @Nullable
    protected BaseGiftLootTables getGiftLootTable() {
        return null;
    }

    @Nonnull
    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> getTables() {
        ImmutableList.Builder<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> builder = new ImmutableList.Builder<>();
        BaseBlockLootTables blockLootTable = getBlockLootTable();
        if (blockLootTable != null) {
            builder.add(Pair.of(() -> blockLootTable, LootParameterSets.BLOCK));
        }
        BaseChestLootTables chestLootTable = getChestLootTable();
        if (chestLootTable != null) {
            builder.add(Pair.of(() -> chestLootTable, LootParameterSets.CHEST));
        }
        BaseEntityLootTables entityLootTable = getEntityLootTable();
        if (entityLootTable != null) {
            builder.add(Pair.of(() -> entityLootTable, LootParameterSets.ENTITY));
        }
        BaseFishingLootTables fishingLootTable = getFishingLootTable();
        if (fishingLootTable != null) {
            builder.add(Pair.of(() -> fishingLootTable, LootParameterSets.FISHING));
        }
        BaseGiftLootTables giftLootTable = getGiftLootTable();
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