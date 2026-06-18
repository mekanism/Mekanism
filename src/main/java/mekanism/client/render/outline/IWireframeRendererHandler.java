package mekanism.client.render.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.outline.Outlines.Line;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.submit.RenderPhaseKey;

public class IWireframeRendererHandler extends MekanismOutlineRenderer {

    private final IWireFrameRenderer wireFrameRenderer;
    private final BlockEntity tile;

    public IWireframeRendererHandler(BlockPos blockPos, List<Line> outlinesFromModel, IWireFrameRenderer wireFrameRenderer, BlockEntity tile) {
        super(blockPos, outlinesFromModel);
        this.wireFrameRenderer = wireFrameRenderer;
        this.tile = tile;
    }

    @Override
    protected void submitLineDraw(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, boolean highContrast, float lineWidth, RenderPhaseKey<SubmitNode> phase, LevelRenderState levelRenderState) {
        super.submitLineDraw(submitNodeCollector, poseStack, highContrast, lineWidth, phase, levelRenderState);
        poseStack.pushPose();
        Collection<Line> movingLines = wireFrameRenderer.applyTransformAndGetFrame(tile, MekanismRenderer.getPartialTick(), poseStack, levelRenderState);
        if (!movingLines.isEmpty()) {
            submitLineDraw(submitNodeCollector, poseStack, highContrast, lineWidth, phase, movingLines);
        }
        poseStack.popPose();
    }
}