package mekanism.common.loot.table;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTable.Builder;

public abstract class BaseBlockLootTable extends BlockLootTables {

    private Set<Block> knownBlocks = new HashSet<>();

    @Override
    protected abstract void addTables();

    @Override
    protected void registerLootTable(@Nonnull Block block, @Nonnull LootTable.Builder table) {
        //Overwrite the core register method to add to our list of known blocks
        super.registerLootTable(block, table);
        knownBlocks.add(block);
    }

    @Nonnull
    @Override
    protected Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }

    //IBlockProvider versions of BlockLootTable methods, modified to support varargs
    public void registerDropSelfLootTable(IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            registerDropSelfLootTable(blockProvider.getBlock());
        }
    }

    protected void registerLootTable(Function<Block, Builder> factory, IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            registerLootTable(blockProvider.getBlock(), factory);
        }
    }

    protected void registerLootTable(LootTable.Builder table, IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            registerLootTable(blockProvider.getBlock(), table);
        }
    }
}