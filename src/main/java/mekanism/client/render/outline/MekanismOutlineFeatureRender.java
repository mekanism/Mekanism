package mekanism.client.render.outline;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Collection;
import java.util.List;
import mekanism.client.render.outline.Outlines.Line;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Vector3f;

public class MekanismOutlineFeatureRender extends RenderTypeFeatureRenderer<MekanismOutlineFeatureRender.Submit> {

    public static final FeatureRendererType<MekanismOutlineFeatureRender.Submit> TYPE = FeatureRendererType.create("Mekanism Outline");

    @Override
    protected void buildGroup(FeatureFrameContext context, List<MekanismOutlineFeatureRender.Submit> submits) {
        Vector3f normal = new Vector3f();
        for (MekanismOutlineFeatureRender.Submit submit : submits) {
            VertexConsumer outline = getVertexBuilder(submit.renderType());
            Pose pose = submit.pose();
            int lineColor = submit.color();
            float lineWidth = submit.lineWidth();
            for (Line line : submit.toDraw()) {
                normal.set(line.nX(), line.nY(), line.nZ());
                outline.addVertex(pose, line.x1(), line.y1(), line.z1()).setColor(lineColor).setNormal(pose, normal).setLineWidth(lineWidth);
                outline.addVertex(pose, line.x2(), line.y2(), line.z2()).setColor(lineColor).setNormal(pose, normal).setLineWidth(lineWidth);
            }
        }
    }

    public record Submit(PoseStack.Pose pose, Collection<Line> toDraw, RenderType renderType, int color, float lineWidth) implements SubmitNode {

        @Override
        public FeatureRendererType<MekanismOutlineFeatureRender.Submit> featureType() {
            return TYPE;
        }
    }
}
