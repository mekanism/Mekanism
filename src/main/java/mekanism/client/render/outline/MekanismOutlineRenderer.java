package mekanism.client.render.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import mekanism.client.render.outline.Outlines.Line;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.submit.RenderPhaseKey;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;

public class MekanismOutlineRenderer implements CustomBlockOutlineRenderer {

    private final List<Line> outlinesFromModel;
    private final BlockPos blockPos;

    public MekanismOutlineRenderer(BlockPos blockPos, List<Line> outlinesFromModel) {
        this.blockPos = blockPos;
        this.outlinesFromModel = outlinesFromModel;
    }

    @Override
    public final boolean render(BlockOutlineRenderState state, SubmitNodeCollector nodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {
        poseStack.pushPose();
        Vec3 viewPosition = levelRenderState.cameraRenderState.pos;
        poseStack.translate(blockPos.getX() - viewPosition.x, blockPos.getY() - viewPosition.y, blockPos.getZ() - viewPosition.z);
        float lineWidth = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        RenderPhaseKey<SubmitNode> phase = state.isTranslucent() ? RenderPhaseKeys.AFTER_TERRAIN : RenderPhaseKeys.SHAPE_OUTLINES;
        submitLineDraw(nodeCollector, poseStack, state.highContrast(), lineWidth, phase, levelRenderState);
        poseStack.popPose();
        return true;
    }

    protected void submitLineDraw(SubmitNodeCollector nodeCollector, PoseStack poseStack, boolean highContrast, float lineWidth, RenderPhaseKey<SubmitNode> phase,
          LevelRenderState levelRenderState) {
        submitLineDraw(nodeCollector, poseStack, highContrast, lineWidth, phase, outlinesFromModel);
    }

    protected final void submitLineDraw(SubmitNodeCollector nodeCollector, PoseStack poseStack, boolean highContrast, float lineWidth, RenderPhaseKey<SubmitNode> phase,
          Collection<Line> toDraw) {
        nodeCollector.submitSpecial(phase, new MekanismOutlineFeatureRender.Submit(poseStack.last().copy(), toDraw, lineWidth, highContrast));
    }
}