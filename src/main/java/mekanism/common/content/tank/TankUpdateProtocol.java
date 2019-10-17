package mekanism.common.content.tank;

import java.util.List;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.block.basic.BlockDynamicTank;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.util.StackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class TankUpdateProtocol extends UpdateProtocol<SynchronizedTankData> {

    public static final int FLUID_PER_TANK = 64000;

    public TankUpdateProtocol(TileEntityDynamicTank tileEntity) {
        super(tileEntity);
    }

    @Override
    protected boolean isValidFrame(int x, int y, int z) {
        BlockState state = pointer.getWorld().getBlockState(new BlockPos(x, y, z));
        return state.getBlock() instanceof BlockDynamicTank;
    }

    @Override
    protected TankCache getNewCache() {
        return new TankCache();
    }

    @Override
    protected SynchronizedTankData getNewStructure() {
        return new SynchronizedTankData();
    }

    @Override
    protected MultiblockManager<SynchronizedTankData> getManager() {
        return Mekanism.tankManager;
    }

    @Override
    protected void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<SynchronizedTankData> cache, MultiblockCache<SynchronizedTankData> merge) {
        TankCache tankCache = (TankCache) cache;
        TankCache mergeCache = (TankCache) merge;
        if (tankCache.fluid.isEmpty()) {
            tankCache.fluid = mergeCache.fluid;
        } else if (!mergeCache.fluid.isEmpty() && tankCache.fluid.isFluidEqual(mergeCache.fluid)) {
            tankCache.fluid.setAmount(tankCache.fluid.getAmount() + mergeCache.fluid.getAmount());
        }
        tankCache.editMode = mergeCache.editMode;
        List<ItemStack> rejects = StackUtils.getMergeRejects(tankCache.getInventorySlots(), mergeCache.getInventorySlots());
        if (!rejects.isEmpty()) {
            rejectedItems.addAll(rejects);
        }
        StackUtils.merge(tankCache.getInventorySlots(), mergeCache.getInventorySlots());
    }

    @Override
    protected void onFormed() {
        super.onFormed();
        if (!structureFound.fluidStored.isEmpty()) {
            structureFound.fluidStored.setAmount(Math.min(structureFound.fluidStored.getAmount(), structureFound.volume * FLUID_PER_TANK));
        }
    }

    @Override
    protected void onStructureCreated(SynchronizedTankData structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
        for (Coord4D obj : structure.locations) {
            if (obj.getTileEntity(pointer.getWorld()) instanceof TileEntityDynamicValve) {
                ValveData data = new ValveData();
                data.location = obj;
                data.side = getSide(obj, origX + xmin, origX + xmax, origY + ymin, origY + ymax, origZ + zmin, origZ + zmax);
                structure.valves.add(data);
            }
        }
    }
}