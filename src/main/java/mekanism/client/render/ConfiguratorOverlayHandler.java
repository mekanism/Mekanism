package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.renderer.FaceInfo;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
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

            //todo - 26.1: rotate when up/down so the sprite is always correct per the user's perspective?
            //Vector3fc vecForDirection = face.getUnitVec3f();
            //poseStack.rotateAround(new Quaternionf().setAngleAxis(90 * Mth.DEG_TO_RAD, vecForDirection.x(), vecForDirection.y(), vecForDirection.z()), 0.5F, 0.5F, 0.5F);
            //poseStack.rotateAround(face.getRotation(), 0.5F, 0.5F, 0.5F);

            PoseStack.Pose pose = poseStack.last();
            Vector3f normal = pose.transformNormal(face.getUnitVec3f(), new Vector3f());
            Matrix4f matrix = pose.pose();

            //face draw code donated by XFactHD
            FaceInfo faceInfo = FaceInfo.fromFacing(face);
            for (int vertex = 0; vertex < 4; vertex++) {
                FaceInfo.VertexInfo vertInfo = faceInfo.getVertexInfo(vertex);
                float x = vertInfo.xFace().select(0, 0, 0, 1, 1, 1) + face.getStepX() * 0.01F;
                float y = vertInfo.yFace().select(0, 0, 0, 1, 1, 1) + face.getStepY() * 0.01F;
                float z = vertInfo.zFace().select(0, 0, 0, 1, 1, 1) + face.getStepZ() * 0.01F;
                float u = vertex < 2 ? 0 : 1;
                float v = vertex == 0 || vertex == 3 ? 0 : 1;
                buffer.addVertex(matrix, x, y, z)
                      .setColor(transmissionColor)
                      .setUv(sprite.getU(u), sprite.getV(v))
                      .setOverlay(OverlayTexture.NO_OVERLAY)
                      .setLight(LightCoordsUtil.FULL_BRIGHT)
                      .setNormal(normal.x, normal.y, normal.z);
            }

            poseStack.popPose();
        }
        return false;
    }

}
