package mekanism.common.loot.table;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsRedstone;
import mekanism.api.block.ISupportsUpgrades;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.sustained.ISustainedTank;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTable.Builder;
import net.minecraft.world.storage.loot.functions.CopyNbt;
import net.minecraft.world.storage.loot.functions.CopyNbt.Source;
import net.minecraftforge.items.IItemHandler;

public abstract class BaseBlockLootTables extends BlockLootTables {

    private Set<Block> knownBlocks = new ObjectOpenHashSet<>();

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
    protected void registerDropSelfLootTable(IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            registerDropSelfLootTable(blockProvider.getBlock());
        }
    }

    protected void registerLootTable(Function<Block, Builder> factory, IBlockProvider... blockProviders) {
        for (IBlockProvider blockProvider : blockProviders) {
            registerLootTable(blockProvider.getBlock(), factory);
        }
    }

    protected void registerDropSelfWithContentsLootTable(IBlockProvider... blockProviders) {
        //TODO: Replace a lot of the NBT strings in here with constants to make sure that they are the same across the board
        //TODO: When doing that, also see if there is other stuff we want to be transferring which we currently do not
        // For example, when writing this we added dump mode for gas tanks to getting transferred to the item
        for (IBlockProvider blockProvider : blockProviders) {
            Block block = blockProvider.getBlock();
            CopyNbt.Builder nbtBuilder = CopyNbt.builder(Source.BLOCK_ENTITY);
            boolean hasData = false;
            @Nullable
            TileEntity tile = null;
            if (block instanceof IHasTileEntity<?>) {
                //TODO: TEST ME, hopefully this does not blow up in our faces, it really shouldn't but if it does
                // it will be revealing a bug about assumptions we are making in the constructor about a world existing
                //TODO: Ideally at some point we will end up moving some of the stuff up to TileEntityMekanism anyways
                // so then we can just remove having to create a tile to check if it implements specific things
                tile = ((IHasTileEntity<?>) block).getTileType().create();
            }
            if (block instanceof IHasSecurity) {
                nbtBuilder.replaceOperation("ownerUUID", ItemDataUtils.DATA_ID + ".ownerUUID");
                nbtBuilder.replaceOperation("securityMode", ItemDataUtils.DATA_ID + ".security");
                hasData = true;
            }
            if (block instanceof ISupportsUpgrades) {
                nbtBuilder.replaceOperation("componentUpgrade", ItemDataUtils.DATA_ID + ".componentUpgrade");
                hasData = true;
            }
            if (tile instanceof ISideConfiguration) {
                nbtBuilder.replaceOperation("componentConfig", ItemDataUtils.DATA_ID + ".componentConfig");
                nbtBuilder.replaceOperation("componentEjector", ItemDataUtils.DATA_ID + ".componentEjector");
                hasData = true;
            }
            if (tile instanceof ISustainedData) {
                Set<Entry<String, String>> remapEntries = ((ISustainedData) tile).getTileDataRemap().entrySet();
                for (Entry<String, String> remapEntry : remapEntries) {
                    nbtBuilder.replaceOperation(remapEntry.getKey(), ItemDataUtils.DATA_ID + "." + remapEntry.getValue());
                }
                if (!remapEntries.isEmpty()) {
                    hasData = true;
                }
            }
            if (block instanceof ISupportsRedstone) {
                nbtBuilder.replaceOperation("controlType", ItemDataUtils.DATA_ID + ".controlType");
                hasData = true;
            }
            //TODO: If anything for inventories doesn't work we may have to check if the tile is an ISustainedInventory
            // I don't believe it is directly needed anymore for this due to IHasInventory
            if (block instanceof IHasInventory) {
                //If the block has an inventory, copy the inventory slots,
                // but if it is an IItemHandler, which for most cases of ours it will be,
                // then only copy the slots if we actually have any slots because otherwise maybe something just went wrong
                if (!(tile instanceof IItemHandler) || ((IItemHandler) tile).getSlots() > 0) {
                    //If we don't actually handle saving an inventory (such as the quantum entangloporter, don't actually add it as something to copy)
                    if (!(tile instanceof TileEntityMekanism) || ((TileEntityMekanism) tile).handleInventory()) {
                        nbtBuilder.replaceOperation("Items", ItemDataUtils.DATA_ID + ".Items");
                        hasData = true;
                    }
                }
            }
            if (tile instanceof ISustainedTank) {
                //TODO: Should this use a similar system to ISustainedData
                nbtBuilder.replaceOperation("fluidTank", ItemDataUtils.DATA_ID + ".fluidTank");
                hasData = true;
            }
            if (block instanceof IBlockElectric) {
                //If the block is electric but is not part of a multiblock
                // we want to copy the energy information
                if (!(tile instanceof TileEntityMultiblock<?>)) {
                    //TODO: Eventually make these use the same key of energyStored?
                    nbtBuilder.replaceOperation("electricityStored", ItemDataUtils.DATA_ID + ".energyStored");
                    hasData = true;
                }
            }
            if (block instanceof BlockCardboardBox) {
                //TODO: Do this better so that it doesn't have to be as hard coded to being a cardboard box
                nbtBuilder.replaceOperation("storedData", ItemDataUtils.DATA_ID + ".blockData");
                hasData = true;
            }
            if (!hasData) {
                //To keep the json as clean as possible don't bother even registering a blank accept function if we have no
                // persistent data that we want to copy. Also log a warning so that we don't have to attempt to check against
                // that block
                Mekanism.logger.warn("Block: '{}' does not have any persistent data to copy.", block.getRegistryName());
                registerDropSelfLootTable(block);
            } else {
                registerLootTable(block, LootTable.builder().addLootPool(withSurvivesExplosion(block, LootPool.builder().rolls(ConstantRange.of(1))
                      .addEntry(ItemLootEntry.builder(block).acceptFunction(nbtBuilder)))));
            }
        }
    }
}