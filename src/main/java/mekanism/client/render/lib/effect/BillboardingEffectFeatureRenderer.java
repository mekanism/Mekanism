package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.BatchableSubmit;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.CommonColors;
import org.joml.Vector3f;

public class BillboardingEffectFeatureRenderer extends RenderTypeFeatureRenderer<BillboardingEffectFeatureRenderer.Submit> {

    public static final FeatureRendererType<BillboardingEffectFeatureRenderer.Submit> TYPE = FeatureRendererType.create("Mekanism Billboard");

    @Override
    protected void buildGroup(FeatureFrameContext context, List<BillboardingEffectFeatureRenderer.Submit> submits) {
        for (BillboardingEffectFeatureRenderer.Submit submit : submits) {
            VertexConsumer builder = getVertexBuilder(submit.renderType);
            render(submit.pose, builder, submit.state);
        }
    }

    private void render(PoseStack.Pose pose, VertexConsumer buffer, BillboardingRenderState state) {
        int tick = state.renderTick % (state.gridSize * state.gridSize);
        int xIndex = tick % state.gridSize;
        int yIndex = tick / state.gridSize;
        float spriteSize = 1F / state.gridSize;

        float u0 = xIndex * spriteSize;
        float u1 = u0 + spriteSize;
        float v0 = yIndex * spriteSize;
        float v1 = v0 + spriteSize;

        buffer.addVertex(pose, 1.0F, -1.0F, 0.0F).setUv(u0, v1).setColor(state.color);
        buffer.addVertex(pose, 1.0F, 1.0F, 0.0F).setUv(u1, v1).setColor(state.color);
        buffer.addVertex(pose, -1.0F, 1.0F, 0.0F).setUv(u1, v0).setColor(state.color);
        buffer.addVertex(pose, -1.0F, -1.0F, 0.0F).setUv(u0, v0).setColor(state.color);
    }

    public static class BillboardingRenderState {

        public int color = CommonColors.WHITE;
        public float scale = 1;
        public int gridSize = 1;
        public Vector3f pos = new Vector3f();
        public int renderTick;
    }

    public record Submit(PoseStack.Pose pose, RenderType renderType, BillboardingRenderState state) implements BatchableSubmit {

        @Override
        public Object batchKey() {
            return renderType;
        }

        @Override
        public FeatureRendererType<BillboardingEffectFeatureRenderer.Submit> featureType() {
            return TYPE;
        }
    }
}