package mekanism.client.render.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collection;
import java.util.List;
import mekanism.client.render.outline.Outlines.Line;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
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
import org.joml.Vector3f;

public class MekanismOutlineRenderer implements CustomBlockOutlineRenderer {

    private static final int DEFAULT_LINE_COLOR = ARGB.black(0x66);

    private final List<Line> outlinesFromModel;
    private final BlockPos blockPos;

    public MekanismOutlineRenderer(BlockPos blockPos, List<Line> outlinesFromModel) {
        this.blockPos = blockPos;
        this.outlinesFromModel = outlinesFromModel;
    }

    @Override
    public final boolean render(BlockOutlineRenderState state, SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState) {
        poseStack.pushPose();
        Vec3 viewPosition = levelRenderState.cameraRenderState.pos;
        poseStack.translate(blockPos.getX() - viewPosition.x, blockPos.getY() - viewPosition.y, blockPos.getZ() - viewPosition.z);
        float lineWidth = Minecraft.getInstance().gameRenderer.gameRenderState().windowRenderState.appropriateLineWidth;
        RenderPhaseKey<SubmitNode> phase = state.isTranslucent() ? RenderPhaseKeys.AFTER_TERRAIN : RenderPhaseKeys.SHAPE_OUTLINES;
        submitLineDraw(submitNodeCollector, poseStack, state.highContrast(), lineWidth, phase, levelRenderState);
        poseStack.popPose();
        return true;
    }

    protected void submitLineDraw(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, boolean highContrast, float lineWidth, RenderPhaseKey<SubmitNode> phase,
          LevelRenderState levelRenderState) {
        submitLineDraw(submitNodeCollector, poseStack, highContrast, lineWidth, phase, outlinesFromModel);
    }

    protected final void submitLineDraw(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, boolean highContrast, float lineWidth, RenderPhaseKey<SubmitNode> phase,
          Collection<Line> toDraw) {
        int outlineColor;
        if (highContrast) {
            //TODO - 26.2: Figure this out, it seems to cause z-fighting
            submitLineDraw(submitNodeCollector, poseStack, RenderTypes.secondaryBlockOutline(), phase, new LineDrawer(toDraw, CommonColors.BLACK, 7F));
            outlineColor = CommonColors.HIGH_CONTRAST_DIAMOND;
        } else {
            outlineColor = DEFAULT_LINE_COLOR;
        }
        submitLineDraw(submitNodeCollector, poseStack, RenderTypes.lines(), phase, new LineDrawer(toDraw, outlineColor, lineWidth));
    }

    private void submitLineDraw(SubmitNodeCollector collector, PoseStack poseStack, RenderType renderType, RenderPhaseKey<SubmitNode> phase, LineDrawer lineDrawer) {
        collector.submitSpecial(phase, new CustomFeatureRenderer.Submit(poseStack.last().copy(), renderType, lineDrawer));
    }

    private record LineDrawer(Collection<Line> toDraw, int lineColor, float lineWidth) implements SubmitNodeCollector.CustomGeometryRenderer {

        @Override
        public void render(PoseStack.Pose pose, VertexConsumer buffer) {
            Vector3f normal = new Vector3f();
            for (Line line : toDraw) {
                normal.set(line.nX(), line.nY(), line.nZ());
                //normal.set(line.x2() - line.x1(), line.y2() - line.y1(), line.z2() - line.z1()).normalize();
                buffer.addVertex(pose, line.x1(), line.y1(), line.z1()).setColor(lineColor).setNormal(pose, normal).setLineWidth(lineWidth);
                buffer.addVertex(pose, line.x2(), line.y2(), line.z2()).setColor(lineColor).setNormal(pose, normal).setLineWidth(lineWidth);
            }
        }
    }
}