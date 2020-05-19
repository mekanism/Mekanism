package mekanism.generators.common.content.fusion;

import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.fusion.TileEntityFusionReactorBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class FusionReactorUpdateProtocol extends FormationProtocol<FusionReactorMultiblockData> {

    public FusionReactorUpdateProtocol(TileEntityFusionReactorBlock tile) {
        super(tile);
    }

    @Override
    protected CasingType getCasingType(BlockPos pos) {
        Block block = pointer.getWorld().getBlockState(pos).getBlock();
        if (BlockTypeTile.is(block, GeneratorsBlockTypes.FUSION_REACTOR_FRAME)) {
            return CasingType.FRAME;
        } else if (BlockTypeTile.is(block, GeneratorsBlockTypes.FUSION_REACTOR_PORT)) {
            return CasingType.VALVE;
        } else if (BlockTypeTile.is(block, GeneratorsBlockTypes.FUSION_REACTOR_CONTROLLER,
              GeneratorsBlockTypes.FUSION_REACTOR_LOGIC_ADAPTER, GeneratorsBlockTypes.LASER_FOCUS_MATRIX)) {
            return CasingType.OTHER;
        }
        return CasingType.INVALID;
    }

    @Override
    protected MultiblockManager<FusionReactorMultiblockData> getManager() {
        return MekanismGenerators.fusionReactorManager;
    }
}