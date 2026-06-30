package mekanism.client.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.MekanismRenderType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.HUDElement.HUDColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class CompassPiP extends PictureInPictureRenderer<CompassPiP.State> {

    private static final Identifier COMPASS = Mekanism.rl("hud/compass");
    private static final float ONE_THIRD_PI = (float) (Math.PI / 3);

    @Override
    protected void renderToTexture(CompassPiP.State state, PoseStack poseStack, SubmitNodeCollector nodeCollector) {
        Font font = Minecraft.getInstance().font;
        int color = HUDColor.REGULAR.getColorARGB();
        poseStack.translate(50, 50, 0);

        poseStack.pushPose();
        poseStack.scale(0.7F, 0.7F, 0.7F);
        nodeCollector.submitText(poseStack, -font.width(state.coords()) / 2F, -4, state.coords(), false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT,
              color, 0, EntityRenderState.NO_OUTLINE);
        poseStack.popPose();

        poseStack.mulPose(Axis.XN.rotation(ONE_THIRD_PI));
        poseStack.mulPose(Axis.ZP.rotation(state.angle()));

        rotateStr(poseStack, nodeCollector, MekanismLang.NORTH_SHORT, state.angle(), 0, color);
        rotateStr(poseStack, nodeCollector, MekanismLang.EAST_SHORT, state.angle(), Mth.HALF_PI, color);
        rotateStr(poseStack, nodeCollector, MekanismLang.SOUTH_SHORT, state.angle(), Mth.PI, color);
        rotateStr(poseStack, nodeCollector, MekanismLang.WEST_SHORT, state.angle(), -Mth.HALF_PI, color);

        poseStack.translate(-50, -50, 0);
        nodeCollector.submitCustomGeometry(poseStack, MekanismRenderType.GUI_SPRITES, (pose, buffer) -> {
            TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(COMPASS);
            int argb = HUDColor.REGULAR.getColorARGB();
            Matrix4f matrix = pose.pose();
            buffer.addVertex(matrix, 0, 0, 0).setUv(sprite.getU0(), sprite.getV0()).setColor(argb);
            buffer.addVertex(matrix, 0, 100, 0).setUv(sprite.getU0(), sprite.getV1()).setColor(argb);
            buffer.addVertex(matrix, 100, 100, 0).setUv(sprite.getU1(), sprite.getV1()).setColor(argb);
            buffer.addVertex(matrix, 100, 0, 0).setUv(sprite.getU1(), sprite.getV0()).setColor(argb);
        });
    }

    private void rotateStr(PoseStack pose, SubmitNodeCollector nodeCollector, ILangEntry langEntry, float rotation, float shift, int color) {
        pose.pushPose();
        pose.mulPose(Axis.ZP.rotation(shift));
        pose.translate(0, -50, 0);
        pose.mulPose(Axis.ZN.rotation(rotation + shift));
        nodeCollector.submitText(pose, -2.5F, -4, langEntry.translate().getVisualOrderText(), false, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT,
              color, 0, EntityRenderState.NO_OUTLINE);
        pose.popPose();
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2F;
    }

    @Override
    public Class<State> getRenderStateClass() {
        return CompassPiP.State.class;
    }

    @Override
    protected String getTextureLabel() {
        return "Mekanism HUD Compass";
    }

    public record State(Matrix3x2fc pose, FormattedCharSequence coords, float angle, int x0, int y0, int x1, int y1, @Nullable ScreenRectangle bounds,
                        @Nullable ScreenRectangle scissorArea) implements PictureInPictureRenderState {

        private static final int SIZE = 250;

        public State(Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea, Component coords, float angle) {
            this(pose, coords.getVisualOrderText(), angle, 0, 0, SIZE, SIZE, PictureInPictureRenderState.getBounds(0, 0, SIZE, SIZE, scissorArea), scissorArea);
        }

        @Override
        public float scale() {
            return 1;
        }
    }
}