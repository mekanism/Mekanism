package mekanism.common.tile;

import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityUpdateable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCardboardBox extends TileEntityUpdateable {

    public TileEntityCardboardBox(BlockPos pos, BlockState state) {
        super(MekanismTileEntityTypes.CARDBOARD_BOX, pos, state);
    }
}