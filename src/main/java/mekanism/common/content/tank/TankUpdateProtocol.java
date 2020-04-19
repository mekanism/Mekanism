package mekanism.common.content.tank;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.multiblock.IValveHandler.ValveData;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class TankUpdateProtocol extends UpdateProtocol<SynchronizedTankData> {

    public static final int FLUID_PER_TANK = 64_000;

    public TankUpdateProtocol(TileEntityDynamicTank tile) {
        super(tile);
    }

    @Override
    protected boolean isValidFrame(int x, int y, int z) {
        return BlockTypeTile.is(pointer.getWorld().getBlockState(new BlockPos(x, y, z)).getBlock(), MekanismBlockTypes.DYNAMIC_TANK);
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

    @Override
    protected void onStructureCreated(SynchronizedTankData structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
        for (Coord4D obj : structure.locations) {
            if (MekanismUtils.getTileEntity(TileEntityDynamicValve.class, pointer.getWorld(), obj.getPos()) != null) {
                ValveData data = new ValveData();
                data.location = obj;
                data.side = getSide(obj, origX + xmin, origX + xmax, origY + ymin, origY + ymax, origZ + zmin, origZ + zmax);
                structure.valves.add(data);
            }
        }
    }
}