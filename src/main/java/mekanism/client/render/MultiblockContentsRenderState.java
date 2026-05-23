package mekanism.client.render;

import java.util.Objects;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;

public class MultiblockContentsRenderState extends BlockEntityRenderState {
    public BlockPos renderLocation = BlockPos.ZERO;
    public int length, width, height;

    public void gather(MultiblockData multiblock) {
        renderLocation = Objects.requireNonNull(multiblock.renderLocation, "Render location may not be null.");
        length = multiblock.length() - 2;
        width = multiblock.width() - 2;
        height = multiblock.height() - 2;
    }
}
