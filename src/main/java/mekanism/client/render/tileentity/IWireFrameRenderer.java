package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IWireFrameRenderer {

    //TODO - 26.1: convert this to a separate extract?
    void renderWireFrame(BlockEntity tile, BlockState blockState, float partialTick, SubmitNodeCollector submitNodeCollector, PoseStack matrix, LevelRenderState levelRenderState, boolean isHighContrast);

    default boolean hasSelectionBox(BlockState state) {
        return true;
    }

    default boolean isCombined() {
        return false;
    }
}