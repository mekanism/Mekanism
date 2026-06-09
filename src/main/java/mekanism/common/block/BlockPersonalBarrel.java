package mekanism.common.block;

import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityPersonalBarrel;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class BlockPersonalBarrel extends BlockPersonalStorage<TileEntityPersonalBarrel, BlockTypeTile<TileEntityPersonalBarrel>> {

    public BlockPersonalBarrel(BlockBehaviour.Properties properties) {
        super(MekanismBlockTypes.PERSONAL_BARREL, defaultProperties(properties).mapColor(MapColor.COLOR_GRAY));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        TileEntityPersonalBarrel barrel = WorldUtils.getTileEntity(TileEntityPersonalBarrel.class, level, pos);
        if (barrel != null) {
            barrel.recheckOpen();
        }
    }
}
