package mekanism.common.content.tank;

import java.util.List;
import mekanism.api.Action;
import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.StackUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class TankUpdateProtocol extends UpdateProtocol<SynchronizedTankData> {

    public static final int FLUID_PER_TANK = 64_000;

    public TankUpdateProtocol(TileEntityDynamicTank tile) {
        super(tile);
    }

    @Override
    protected CasingType getCasingType(BlockPos pos) {
        Block block = pointer.getWorld().getBlockState(pos).getBlock();
        if (BlockTypeTile.is(block, MekanismBlockTypes.DYNAMIC_TANK)) {
            return CasingType.FRAME;
        } else if (BlockTypeTile.is(block, MekanismBlockTypes.DYNAMIC_VALVE)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    protected TankCache getNewCache() {
        return new TankCache();
    }

    @Override
    protected SynchronizedTankData getNewStructure() {
        return new SynchronizedTankData((TileEntityDynamicTank) pointer);
    }

    @Override
    protected MultiblockManager<SynchronizedTankData> getManager() {
        return Mekanism.tankManager;
    }

    @Override
    protected void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<SynchronizedTankData> cache, MultiblockCache<SynchronizedTankData> merge) {
        TankCache tankCache = (TankCache) cache;
        TankCache mergeCache = (TankCache) merge;
        StorageUtils.mergeTanks(tankCache.getFluidTanks(null).get(0), mergeCache.getFluidTanks(null).get(0));
        tankCache.editMode = mergeCache.editMode;
        List<ItemStack> rejects = StackUtils.getMergeRejects(tankCache.getInventorySlots(null), mergeCache.getInventorySlots(null));
        if (!rejects.isEmpty()) {
            rejectedItems.addAll(rejects);
        }
        StackUtils.merge(tankCache.getInventorySlots(null), mergeCache.getInventorySlots(null));
    }

    @Override
    protected void onFormed() {
        super.onFormed();
        if (!structureFound.fluidTank.isEmpty()) {
            structureFound.fluidTank.setStackSize(Math.min(structureFound.fluidTank.getFluidAmount(), structureFound.getTankCapacity()), Action.EXECUTE);
        }
    }
}