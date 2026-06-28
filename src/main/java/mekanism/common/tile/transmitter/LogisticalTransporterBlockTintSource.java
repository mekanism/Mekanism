package mekanism.common.tile.transmitter;

import mekanism.api.text.EnumColor;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.state.BlockState;

public class LogisticalTransporterBlockTintSource implements BlockTintSource {

    public static final BlockTintSource INSTANCE = new LogisticalTransporterBlockTintSource();

    @Override
    public int color(BlockState state) {
        return CommonColors.WHITE;
    }

    @Override
    public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        TileEntityLogisticalTransporter transporter = WorldUtils.getTileEntity(TileEntityLogisticalTransporter.class, level, pos);
        if (transporter != null) {
            EnumColor renderColor = transporter.getTransmitter().getColor();
            if (renderColor != null) {
                return renderColor.getPackedColor();
            }
        }
        return CommonColors.WHITE;
    }
}
