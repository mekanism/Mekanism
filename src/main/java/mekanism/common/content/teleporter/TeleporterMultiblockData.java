package mekanism.common.content.teleporter;

import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TeleporterMultiblockData extends MultiblockData {

    public TeleporterMultiblockData(TileEntity tile) {
        super(tile);
    }

    public BlockPos getTeleportTargetCoords() {
        VoxelCuboid bounds = getBounds();
        BlockPos center = bounds.getCenter();
        return new BlockPos(center.getX(), bounds.getMinPos().getY(), center.getZ());
    }
}
