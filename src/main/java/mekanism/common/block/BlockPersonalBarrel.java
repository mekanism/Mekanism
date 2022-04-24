package mekanism.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.block.interfaces.IPersonalStorage;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityPersonalBarrel;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPersonalBarrel extends BlockTile<TileEntityPersonalBarrel, BlockTypeTile<TileEntityPersonalBarrel>> implements IPersonalStorage {

    public BlockPersonalBarrel() {
        super(MekanismBlockTypes.PERSONAL_BARREL);
    }

    @Override
    @Deprecated
    public void tick(@Nonnull BlockState state, @Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nonnull Random random) {
        super.tick(state, level, pos, random);
        TileEntityPersonalBarrel barrel = WorldUtils.getTileEntity(TileEntityPersonalBarrel.class, level, pos);
        if (barrel != null) {
            barrel.recheckOpen();
        }
    }
}