package mekanism.common.content.tank;

import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.multiblock.IValveHandler.ValveData;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.tile.TileEntityDynamicValve;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.math.BlockPos;

public class TankUpdateProtocol extends UpdateProtocol<TankMultiblockData> {

    public static final int FLUID_PER_TANK = 64_000;

    public TankUpdateProtocol(TileEntityDynamicTank tile) {
        super(tile);
    }

    @Override
    protected boolean isValidFrame(int x, int y, int z) {
        return BlockTypeTile.is(pointer.getWorld().getBlockState(new BlockPos(x, y, z)).getBlock(), MekanismBlockTypes.DYNAMIC_TANK);
    }

    @Override
    protected MultiblockManager<TankMultiblockData> getManager() {
        return Mekanism.tankManager;
    }

    @Override
    protected void onStructureCreated(TankMultiblockData structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
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