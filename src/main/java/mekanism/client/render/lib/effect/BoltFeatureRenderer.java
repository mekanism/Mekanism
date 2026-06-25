package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.render.MekanismRenderType;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.util.CommonColors;
import org.joml.Vector3fc;

public class BoltFeatureRenderer extends RenderTypeFeatureRenderer<BoltFeatureRenderer.Submit> {

    public static final FeatureRendererType<BoltFeatureRenderer.Submit> TYPE = FeatureRendererType.create("Mekanism Bolt");

    @Override
    protected void buildGroup(FeatureFrameContext context, List<BoltFeatureRenderer.Submit> submits) {
        VertexConsumer builder = getVertexBuilder(MekanismRenderType.MEK_LIGHTNING);
        for (BoltFeatureRenderer.Submit submit : submits) {
            for (Vector3fc vertex : submit.state.vertices) {
                builder.addVertex(submit.pose, vertex).setColor(submit.state.color);
            }
        }
    }

    public static class BoltRenderState {

        public List<Vector3fc> vertices = new ArrayList<>();
        public int color = CommonColors.WHITE;
    }

    public record Submit(PoseStack.Pose pose, BoltRenderState state) implements SubmitNode {

        @Override
        public FeatureRendererType<BoltFeatureRenderer.Submit> featureType() {
            return TYPE;
        }
    }
}