package mekanism.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.block.interfaces.IPersonalStorage;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class BlockPersonalChest extends BlockTileModel<TileEntityPersonalChest, BlockTypeTile<TileEntityPersonalChest>> implements IPersonalStorage {

    public BlockPersonalChest() {
        super(MekanismBlockTypes.PERSONAL_CHEST);
    }

    @Override
    @Deprecated
    public void tick(@Nonnull BlockState state, @Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nonnull Random random) {
        super.tick(state, level, pos, random);
        TileEntityPersonalChest chest = WorldUtils.getTileEntity(TileEntityPersonalChest.class, level, pos);
        if (chest != null) {
            chest.recheckOpen();
        }
    }
}