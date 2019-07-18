package mekanism.common.block.basic;

import mekanism.common.base.IActiveState;
import mekanism.common.block.BlockBasic;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockTeleporterFrame extends BlockBasic {

    public BlockTeleporterFrame() {
        super("teleporter_frame");
    }

    @Override
    public boolean hasDescription() {
        return true;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        if (tileEntity instanceof IActiveState) {
            if (((IActiveState) tileEntity).getActive() && ((IActiveState) tileEntity).lightUpdate()) {
                return 15;
            }
        }
        return 12;
    }
}