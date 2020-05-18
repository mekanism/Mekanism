package mekanism.common.content.tank;

import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class TankUpdateProtocol extends FormationProtocol<TankMultiblockData> {

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
    protected MultiblockManager<TankMultiblockData> getManager() {
        return Mekanism.tankManager;
    }
}