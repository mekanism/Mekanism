package mekanism.common.block;

import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class BlockPersonalChest extends BlockPersonalStorage<TileEntityPersonalChest, BlockTypeTile<TileEntityPersonalChest>> implements IStateFluidLoggable {

    public BlockPersonalChest() {
        super(MekanismBlockTypes.PERSONAL_CHEST, properties -> properties.mapColor(MapColor.COLOR_GRAY));
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.tick(state, level, pos, random);
        TileEntityPersonalChest chest = WorldUtils.getTileEntity(TileEntityPersonalChest.class, level, pos);
        if (chest != null) {
            chest.recheckOpen();
        }
    }
}
