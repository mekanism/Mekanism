package mekanism.client.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.model.robit.RobitSkinManager.BakeResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class RobitSkinPreviewPiP extends PictureInPictureRenderer<RobitSkinPreviewPiP.State> {

    @Override
    protected void renderToTexture(RobitSkinPreviewPiP.State state, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        poseStack.mulPose(Axis.ZP.rotation(Mth.PI));
        poseStack.rotateAround(state.rotation, 0.5F, 0.0F, 0.5F);
        BakeResult model = state.model();
        nodeCollector.submitBlockModel(poseStack, model.renderType(), model.model(), BlockModelRenderState.EMPTY_TINTS, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, EntityRenderState.NO_OUTLINE);
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return 0.8F * height;
    }

    @Override
    public Class<State> getRenderStateClass() {
        return RobitSkinPreviewPiP.State.class;
    }

    @Override
    protected String getTextureLabel() {
        return "Mekanism Robit";
    }

    public record State(int x0, int y0, int x1, int y1, @Nullable ScreenRectangle bounds, @Nullable ScreenRectangle scissorArea, Quaternionf rotation, BakeResult model,
                        float scale) implements PictureInPictureRenderState {

        public State(int x0, int y0, int size, @Nullable ScreenRectangle scissorArea, Quaternionf rotation, BakeResult model) {
            int x1 = x0 + 2 * size;
            int y1 = y0 + size;
            this(x0, y0, x1, y1, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea), scissorArea, rotation, model, size);
        }
    }
}