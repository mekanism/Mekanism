package mekanism.common.content.tank;

import java.util.List;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.BlockBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.util.StackUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class TankUpdateProtocol extends UpdateProtocol<SynchronizedTankData> {

    public static final int FLUID_PER_TANK = 64000;

    public TankUpdateProtocol(TileEntityDynamicTank tileEntity) {
        super(tileEntity);
    }

    @Override
    protected boolean isValidFrame(int x, int y, int z) {
        IBlockState state = pointer.getWorld().getBlockState(new BlockPos(x, y, z));
        return state.getBlock() == MekanismBlocks.BasicBlock && state.getValue(((BlockBasic) state.getBlock()).getTypeProperty()) == BasicBlockType.DYNAMIC_TANK;
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
        if (tankCache.fluid == null) {
            tankCache.fluid = mergeCache.fluid;
        } else if (mergeCache.fluid != null && tankCache.fluid.isFluidEqual(mergeCache.fluid)) {
            tankCache.fluid.amount += mergeCache.fluid.amount;
        }
        tankCache.editMode = mergeCache.editMode;
        List<ItemStack> rejects = StackUtils.getMergeRejects(tankCache.inventory, mergeCache.inventory);
        if (!rejects.isEmpty()) {
            rejectedItems.addAll(rejects);
        }
        StackUtils.merge(tankCache.inventory, mergeCache.inventory);
    }

    @Override
    protected void onFormed() {
        super.onFormed();
        if (structureFound.fluidStored != null) {
            structureFound.fluidStored.amount = Math.min(structureFound.fluidStored.amount, structureFound.volume * FLUID_PER_TANK);
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