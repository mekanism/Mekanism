package mekanism.common.block;

import mekanism.common.block.interfaces.IPersonalStorage;
import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BlockPersonalChest extends BlockTileModel<TileEntityPersonalChest, BlockTypeTile<TileEntityPersonalChest>> implements IPersonalStorage {

    public BlockPersonalChest() {
        super(MekanismBlockTypes.PERSONAL_CHEST);
    }

    @Override
    @Deprecated
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.tick(state, level, pos, random);
        TileEntityPersonalChest chest = WorldUtils.getTileEntity(TileEntityPersonalChest.class, level, pos);
        if (chest != null) {
            chest.recheckOpen();
        }
    }
}