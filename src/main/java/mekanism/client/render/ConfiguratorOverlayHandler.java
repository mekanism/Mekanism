package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NullMarked;

class ConfiguratorOverlayHandler implements CustomBlockOutlineRenderer {

    private final BlockPos pos;
    private final TransmissionType type;
    private final Direction face;
    private final int transmissionColor;

    public ConfiguratorOverlayHandler(BlockPos pos, TransmissionType type, Direction face, int transmissionColor) {
        this.pos = pos;
        this.type = type;
        this.face = face;
        this.transmissionColor = transmissionColor;
    }

    @Override
    @NullMarked
    public boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource renderer, PoseStack poseStack, boolean translucentPass, LevelRenderState levelRenderState) {
        if (renderState.isTranslucent() == translucentPass) {
            Vec3 viewPosition = levelRenderState.cameraRenderState.pos;
            TextureAtlasSprite sprite = MekanismRenderer.overlays.get(type);
            VertexConsumer buffer = renderer.getBuffer(RenderTypes.eyes(TextureAtlas.LOCATION_BLOCKS));
            poseStack.pushPose();
            poseStack.translate(pos.getX() - viewPosition.x, pos.getY() - viewPosition.y, pos.getZ() - viewPosition.z);

            //rotate so the drawn top face is oriented correctly
            //todo - 26.1: rotate when up/down so the sprite is always correct per the user's perspective?
            Vector3fc vecForDirection = face.getUnitVec3f();
            poseStack.rotateAround(new Quaternionf().setAngleAxis(90 * Mth.DEG_TO_RAD, vecForDirection.x(), vecForDirection.y(), vecForDirection.z()), 0.5F, 0.5F, 0.5F);
            poseStack.rotateAround(face.getRotation(), 0.5F, 0.5F, 0.5F);

            PoseStack.Pose pose = poseStack.last();
            Vector3f normal = pose.transformNormal(face.getUnitVec3f(), new Vector3f());
            Matrix4f matrix = pose.pose();
            drawFace(buffer, matrix, sprite.getU(1), sprite.getU(0), sprite.getV(0), sprite.getV(1), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, normal, transmissionColor,
                  0, 1.01F, 1,
                  1, 1.01F, 1,
                  1, 1.01F, 0,
                  0, 1.01F, 0);

            poseStack.popPose();
        }
        return false;
    }

    private static void drawFace(VertexConsumer buffer, Matrix4f matrix, float minU, float maxU, float minV, float maxV, int light, int overlay,
          Vector3f normal, int color,
          float x1, float y1, float z1,
          float x2, float y2, float z2,
          float x3, float y3, float z3,
          float x4, float y4, float z4) {

        buffer.addVertex(matrix, x1, y1, z1)
              .setColor(color)
              .setUv(minU, maxV)
              .setOverlay(overlay)
              .setLight(light)
              .setNormal(normal.x(), normal.y(), normal.z());
        buffer.addVertex(matrix, x2, y2, z2)
              .setColor(color)
              .setUv(minU, minV)
              .setOverlay(overlay)
              .setLight(light)
              .setNormal(normal.x(), normal.y(), normal.z());
        buffer.addVertex(matrix, x3, y3, z3)
              .setColor(color)
              .setUv(maxU, minV)
              .setOverlay(overlay)
              .setLight(light)
              .setNormal(normal.x(), normal.y(), normal.z());
        buffer.addVertex(matrix, x4, y4, z4)
              .setColor(color)
              .setUv(maxU, maxV)
              .setOverlay(overlay)
              .setLight(light)
              .setNormal(normal.x(), normal.y(), normal.z());

    }

}
