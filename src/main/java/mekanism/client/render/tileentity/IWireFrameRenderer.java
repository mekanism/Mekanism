package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IWireFrameRenderer {

    //TODO - 26.1: convert this to a separate extract?
    void renderWireFrame(BlockEntity tile, BlockState blockState, float partialTick, PoseStack matrix, VertexConsumer buffer, boolean isHighContrast);

    default boolean hasSelectionBox(BlockState state) {
        return true;
    }

    default boolean isCombined() {
        return false;
    }
}