package mekanism.client.render.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.Collections;
import mekanism.client.render.outline.Outlines.Line;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface IWireFrameRenderer {

    //TODO - 26.2: convert this to a separate extract?
    default Collection<Line> applyTransformAndGetFrame(BlockEntity tile, float partialTick, PoseStack poseStack, LevelRenderState levelRenderState) {
        return Collections.emptyList();
    }

    default boolean hasSelectionBox(BlockState state) {
        return true;
    }

    default boolean isCombined() {
        return false;
    }
}