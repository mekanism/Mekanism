package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IWireFrameRenderer {

    void renderWireFrame(BlockEntity tile, float partialTick, PoseStack matrix, VertexConsumer buffer);

    default boolean hasSelectionBox(BlockState state) {
        return true;
    }

    default boolean isCombined() {
        return false;
    }
}