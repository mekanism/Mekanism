package mekanism.client.render.lib.effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.client.particle.LaserParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.QuadParticleFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.LightCoordsUtil;
import org.jspecify.annotations.Nullable;

/// Trimmed down version of [QuadParticleFeatureRenderer]
public class LaserFeatureRenderer implements FeatureRenderer<LaserFeatureRenderer.Submit> {

    public static final FeatureRendererType<LaserFeatureRenderer.Submit> TYPE = FeatureRendererType.create("Mekanism Laser");
    private final List<LaserFeatureRenderer.PreparedGroup> groups = new ArrayList<>();
    @Nullable
    private GpuBufferSlice dynamicTransforms;

    @Override
    public void prepareGroup(FeatureFrameContext context, List<LaserFeatureRenderer.Submit> submits, boolean strictlyOrdered) {
        if (!submits.isEmpty()) {
            StagedVertexBuffer stagedVertexBuffer = context.stagedVertexBuffer();
            StagedVertexBuffer.Draw layerDraw = stagedVertexBuffer.appendDraw(DefaultVertexFormat.PARTICLE, PrimitiveTopology.QUADS, null);
            for (LaserFeatureRenderer.Submit submit : submits) {
                VertexConsumer vertexBuilder = stagedVertexBuffer.getVertexBuilder(layerDraw);

                float uMin = submit.sprite.getU0();
                float uMax = submit.sprite.getU1();
                float vMin = submit.sprite.getV0();
                float vMax = submit.sprite.getV1();
                float energyScale = submit.energyScale;
                float halfLength = submit.halfLength;

                addVertex(vertexBuilder, submit.pose, energyScale, -halfLength, uMax, vMax);
                addVertex(vertexBuilder, submit.pose, energyScale, halfLength, uMax, vMin);
                addVertex(vertexBuilder, submit.pose, -energyScale, halfLength, uMin, vMin);
                addVertex(vertexBuilder, submit.pose, -energyScale, -halfLength, uMin, vMax);

                //TODO - 26.3: Why do we need to draw back faces? (Can we just change the rotation based on the camera? Ideally yes)
                //Draw back faces
                addVertex(vertexBuilder, submit.pose, energyScale, halfLength, uMax, vMin);
                addVertex(vertexBuilder, submit.pose, energyScale, -halfLength, uMax, vMax);
                addVertex(vertexBuilder, submit.pose, -energyScale, -halfLength, uMin, vMax);
                addVertex(vertexBuilder, submit.pose, -energyScale, halfLength, uMin, vMin);

                stagedVertexBuffer.requestIndexCount(layerDraw);
            }
            this.groups.add(new LaserFeatureRenderer.PreparedGroup(layerDraw, context.textureManager().getTexture(SingleQuadParticle.Layer.TRANSLUCENT.textureAtlasLocation())));
        }
    }

    @Override
    public void finishPrepare(FeatureFrameContext context) {
        this.dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrixCopy());
    }

    @Override
    public void executeGroup(FeatureFrameContext context, @Nullable OitStage stage, RenderPass renderPass, int groupIndex, List<LaserFeatureRenderer.Submit> submits,
          boolean strictlyOrdered) {
        LaserFeatureRenderer.PreparedGroup group = this.groups.get(groupIndex);
        renderPass.pushDebugGroup(() -> "Particles - Translucent (Mekanism Laser)");
        RenderSystem.bindDefaultUniforms(renderPass);
        renderPass.setUniform("DynamicTransforms", Objects.requireNonNull(this.dynamicTransforms));
        renderPass.setUniform("Sampler2", context.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        drawLayers(context.stagedVertexBuffer(), group, renderPass, stage);
        renderPass.popDebugGroup();
    }

    private static void drawLayers(StagedVertexBuffer stagedBuffer, LaserFeatureRenderer.PreparedGroup group, RenderPass renderPass, @Nullable OitStage stage) {
        SingleQuadParticle.Layer layer = SingleQuadParticle.Layer.TRANSLUCENT;
        StagedVertexBuffer.ExecuteInfo executeInfo = stagedBuffer.getExecuteInfo(group.draw);
        if (executeInfo != null) {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(stage == null ? layer.pipeline() : getOitPipeline(stage, layer)));
            renderPass.setVertexBuffer(0, executeInfo.vertexBuffer().slice());
            renderPass.setIndexBuffer(executeInfo.indexBuffer(), executeInfo.indexType());
            renderPass.setUniform("Sampler0", group.texture.getTextureView(), group.texture.getSampler());
            renderPass.drawIndexed(executeInfo.indexCount(), 1, executeInfo.firstIndex(), executeInfo.baseVertex(), 0);
        }
    }

    private static RenderPipeline getOitPipeline(OitStage stage, SingleQuadParticle.Layer layer) {
        if (layer.oitPipelineSet() == null) {
            throw new IllegalStateException("OIT pipeline set for particle layer not specified.");
        }
        return layer.oitPipelineSet().getPipeline(stage);
    }

    @Override
    public void finishExecute(FeatureFrameContext context) {
        this.groups.clear();
        this.dynamicTransforms = null;
    }

    private record PreparedGroup(StagedVertexBuffer.Draw draw, AbstractTexture texture) {
    }

    private void addVertex(VertexConsumer vertexBuilder, PoseStack.Pose pose, float x, float y, float u, float v) {
        vertexBuilder.addVertex(pose, x, y, 0)
              .setUv(u, v)
              //Note: Vanilla discards pieces from particles that are under the alpha of 0.1. We use the lowest alpha value that doesn't get discarded
              .setColor(0x1AFF0000)
              .setLight(LightCoordsUtil.FULL_BRIGHT)
        ;
    }

    public record Submit(PoseStack.Pose pose, float energyScale, float halfLength, TextureAtlasSprite sprite) implements SubmitNode {

        public Submit(PoseStack poseStack, LaserParticle particle) {
            this(poseStack.last(), particle.energyScale(), particle.halfLength(), particle.sprite());
        }

        @Override
        public FeatureRendererType<LaserFeatureRenderer.Submit> featureType() {
            return TYPE;
        }
    }
}