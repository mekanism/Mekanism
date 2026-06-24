package mekanism.client.render.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collection;
import java.util.List;
import mekanism.client.render.outline.Outlines.Line;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.joml.Vector3f;

public class MekanismOutlineFeatureRender extends RenderTypeFeatureRenderer<MekanismOutlineFeatureRender.Submit> {

    public static final FeatureRendererType<MekanismOutlineFeatureRender.Submit> TYPE = FeatureRendererType.create("Mekanism Outline");
    private static final int DEFAULT_LINE_COLOR = ARGB.black(0x66);

    @Override
    protected void buildGroup(FeatureFrameContext context, List<MekanismOutlineFeatureRender.Submit> submits) {
        Vector3f normal = new Vector3f();
        //TODO: Neo PR to expose highContrast to FeatureFrameContext#options?
        VertexConsumer secondaryBlockOutline = getVertexBuilder(RenderTypes.secondaryBlockOutline());
        for (MekanismOutlineFeatureRender.Submit submit : submits) {
            if (submit.highContrast()) {
                render(submit.pose, secondaryBlockOutline, normal, submit.toDraw, CommonColors.BLACK, 7F);
            }
        }
        VertexConsumer outline = getVertexBuilder(RenderTypes.lines());
        for (MekanismOutlineFeatureRender.Submit submit : submits) {
            render(submit.pose, outline, normal, submit.toDraw, submit.highContrast ? CommonColors.HIGH_CONTRAST_DIAMOND : DEFAULT_LINE_COLOR, submit.lineWidth);
        }
    }

    private void render(PoseStack.Pose pose, VertexConsumer buffer, Vector3f normal, Collection<Line> toDraw, int lineColor, float lineWidth) {
        for (Line line : toDraw) {
            normal.set(line.nX(), line.nY(), line.nZ());
            buffer.addVertex(pose, line.x1(), line.y1(), line.z1()).setColor(lineColor).setNormal(pose, normal).setLineWidth(lineWidth);
            buffer.addVertex(pose, line.x2(), line.y2(), line.z2()).setColor(lineColor).setNormal(pose, normal).setLineWidth(lineWidth);
        }
    }

    public record Submit(PoseStack.Pose pose, Collection<Line> toDraw, float lineWidth, boolean highContrast) implements SubmitNode {

        @Override
        public FeatureRendererType<MekanismOutlineFeatureRender.Submit> featureType() {
            return TYPE;
        }
    }
}
