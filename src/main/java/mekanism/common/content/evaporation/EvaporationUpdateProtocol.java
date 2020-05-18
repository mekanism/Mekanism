package mekanism.common.content.evaporation;

import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityThermalEvaporationBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class EvaporationUpdateProtocol extends FormationProtocol<EvaporationMultiblockData> {

    public EvaporationUpdateProtocol(TileEntityThermalEvaporationBlock tile) {
        super(tile);
    }

    @Override
    protected CasingType getCasingType(BlockPos pos) {
        Block block = pointer.getWorld().getBlockState(pos).getBlock();
        if (BlockTypeTile.is(block, MekanismBlockTypes.THERMAL_EVAPORATION_BLOCK)) {
            return CasingType.FRAME;
        } else if (BlockTypeTile.is(block, MekanismBlockTypes.THERMAL_EVAPORATION_VALVE)) {
            return CasingType.VALVE;
        } else if (BlockTypeTile.is(block, MekanismBlockTypes.THERMAL_EVAPORATION_CONTROLLER)) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    protected MultiblockManager<EvaporationMultiblockData> getManager() {
        return Mekanism.evaporationManager;
    }
}
