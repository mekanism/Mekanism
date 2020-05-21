package mekanism.common.content.sps;

import java.util.Set;
import mekanism.common.Mekanism;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlockTypes;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class SPSUpdateProtocol extends FormationProtocol<SPSMultiblockData> {

    public SPSUpdateProtocol(IMultiblock<SPSMultiblockData> tile) {
        super(tile);
    }

    @Override
    protected CasingType getCasingType(BlockPos pos) {
        Block block = pointer.getWorld().getBlockState(pos).getBlock();
        if (BlockTypeTile.is(block, MekanismBlockTypes.SPS_CASING)) {
            return CasingType.FRAME;
        } else if (BlockTypeTile.is(block, MekanismBlockTypes.SPS_PORT)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }

    @Override
    protected FormationResult validate(SPSMultiblockData structure, Set<BlockPos> innerNodes) {
        return FormationResult.SUCCESS;
    }

    @Override
    protected boolean isValidInnerNode(BlockPos pos) {
        if (super.isValidInnerNode(pos)) {
            return true;
        }
        return BlockType.is(pointer.getWorld().getBlockState(pos).getBlock(), MekanismBlockTypes.SUPERCHARGED_COIL);
    }

    @Override
    protected MultiblockManager<SPSMultiblockData> getManager() {
        return Mekanism.spsManager;
    }
}
