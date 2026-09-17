package mekanism.client.render.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.List;
import mekanism.client.render.outline.Outlines.Line;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import net.neoforged.neoforge.client.submit.RenderPhaseKey;
import net.neoforged.neoforge.client.submit.RenderPhaseKeys;

public class MekanismOutlineRenderer implements CustomBlockOutlineRenderer {

    private static final int DEFAULT_LINE_COLOR = ARGB.black(0x66);
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
        submitLineDraw(nodeCollector, poseStack, state, levelRenderState);
        poseStack.popPose();
        return true;
    }

    protected void submitLineDraw(SubmitNodeCollector nodeCollector, PoseStack poseStack, BlockOutlineRenderState state, LevelRenderState levelRenderState) {
        submitLineDraw(nodeCollector, poseStack, state, outlinesFromModel);
    }

    protected final void submitLineDraw(SubmitNodeCollector nodeCollector, PoseStack poseStack, BlockOutlineRenderState state, Collection<Line> toDraw) {
        if (state.highContrast()) {
            submitLineDraw(nodeCollector, poseStack, toDraw, RenderTypes.secondaryBlockOutline(), CommonColors.BLACK, 7F, state.isTranslucent());
        }
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        RenderType blockOutlineRenderType;
        if (state.highContrast()) {
            blockOutlineRenderType = RenderTypes.linesDepthBias();
        } else if (gameRenderer.useImprovedTransparency()) {
            blockOutlineRenderType = RenderTypes.linesTranslucentNoDepthWrite();
        } else {
            blockOutlineRenderType = RenderTypes.linesTranslucent();
        }
        int color = state.highContrast() ? CommonColors.HIGH_CONTRAST_DIAMOND : DEFAULT_LINE_COLOR;
        float lineWidth = gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        submitLineDraw(nodeCollector, poseStack, toDraw, blockOutlineRenderType, color, lineWidth, state.isTranslucent());
    }

    private void submitLineDraw(SubmitNodeCollector nodeCollector, PoseStack poseStack, Collection<Line> toDraw, RenderType renderType, int color, float lineWidth,
          boolean isTranslucent) {
        RenderPhaseKey<SubmitNode> phase;
        if (ARGB.alpha(color) == 0xFF) {
            phase = RenderPhaseKeys.SOLID;
        } else if (isTranslucent) {
            phase = RenderPhaseKeys.AFTER_TERRAIN;
        } else {
            phase = RenderPhaseKeys.SHAPE_OUTLINES;
        }
        nodeCollector.submitSpecial(phase, new MekanismOutlineFeatureRender.Submit(poseStack.last().copy(), toDraw, renderType, color, lineWidth));
    }
}