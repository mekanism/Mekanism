package mekanism.common.tile.qio;

import mekanism.api.text.EnumColor;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.state.BlockState;

public class QIOBlockTintSource implements BlockTintSource {

    public static final BlockTintSource INSTANCE = new QIOBlockTintSource();

    @Override
    public int color(BlockState state) {
        return CommonColors.WHITE;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        TileEntityQIOComponent tile = WorldUtils.getTileEntity(TileEntityQIOComponent.class, level, pos);
        if (tile != null) {
            EnumColor color = tile.getColor();
            if (color != null) {
                return color.getPackedColor();
            }
        }
        return CommonColors.WHITE;
    }
}
