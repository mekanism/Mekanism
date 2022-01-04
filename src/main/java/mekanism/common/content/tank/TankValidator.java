package mekanism.common.content.tank;

import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol.CasingType;
import mekanism.common.registries.MekanismBlockTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TankValidator extends CuboidStructureValidator<TankMultiblockData> {

    @Override
    protected CasingType getCasingType(BlockState state) {
        Block block = state.getBlock();
        if (BlockType.is(block, MekanismBlockTypes.DYNAMIC_TANK)) {
            return CasingType.FRAME;
        } else if (BlockType.is(block, MekanismBlockTypes.DYNAMIC_VALVE)) {
            return CasingType.VALVE;
        }
        return CasingType.INVALID;
    }
}