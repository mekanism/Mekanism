package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import org.joml.Quaternionf;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
    public boolean render(BlockOutlineRenderState renderState, MultiBufferSource.BufferSource renderer, PoseStack matrix, boolean translucentPass, LevelRenderState levelRenderState) {
        if (renderState.isTranslucent() == translucentPass) {
            Vec3 viewPosition = levelRenderState.cameraRenderState.pos;
            matrix.pushPose();
            matrix.translate(pos.getX() - viewPosition.x, pos.getY() - viewPosition.y, pos.getZ() - viewPosition.z);
            MekanismRenderer.SingleTexturePicker tex = MekanismRenderer.overlays.get(type);
            MekanismRenderer.Model3D object = RenderTickHandler.getOverlayModel(face, type);
            if (object != null) {
                OrderedSubmitNodeCollector nodeCollector = new HackyNodeCollector(renderer);
                //todo - 26.1: this is overkill, it's only really a single face... also requires wrapping in a NodeCollector
                RenderResizableCuboid.renderCube(object, object.minX, object.minY, object.minZ, object.maxX, object.maxY, object.maxZ, matrix, Sheets.translucentBlockSheet(), nodeCollector, transmissionColor, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, RenderResizableCuboid.FaceDisplay.FRONT, viewPosition, null, tex);
            }
            matrix.popPose();
        }
        return false;
    }

    @NullMarked
    private record HackyNodeCollector(MultiBufferSource.BufferSource renderer) implements OrderedSubmitNodeCollector {

        @Override
        public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer) {
            customGeometryRenderer.render(poseStack.last(), renderer.getBuffer(renderType));
        }

        //nothing else supported

        @Override
        public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void submitNameTag(PoseStack poseStack, @Nullable Vec3 nameTagAttachment, int offset, Component name, boolean seeThrough, int lightCoords, double distanceToCameraSq, CameraRenderState camera) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow, Font.DisplayMode displayMode, int lightCoords, int color, int backgroundColor, int outlineColor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <S> void submitModel(Model<? super S> model, S state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, int tintedColor, @Nullable TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, @Nullable TextureAtlasSprite sprite, boolean sheeted, boolean hasFoil, int tintedColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, int outlineColor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> parts, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void submitBreakingBlockModel(PoseStack poseStack, BlockStateModel model, long seed, int progress) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, ItemStackRenderState.FoilType foilType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void submitParticleGroup(SubmitNodeCollector.ParticleGroupRenderer particleGroupRenderer) {
            throw new UnsupportedOperationException();
        }
    }
}
