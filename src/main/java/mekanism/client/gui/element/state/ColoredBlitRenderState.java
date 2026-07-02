package mekanism.client.gui.element.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public record ColoredBlitRenderState(int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int colorFrom, int colorTo, boolean vertical,
                                     Matrix3x2fc pose, TextureSetup textureSetup, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {

    public ColoredBlitRenderState(int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int colorFrom, int colorTo, boolean vertical,
          Matrix3x2fc pose, AbstractTexture texture, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        this(x0, y0, x1, y1, u0, u1, v0, v1, colorFrom, colorTo, vertical, pose, TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()),
              scissorArea, scissorArea == null ? bounds : scissorArea.intersection(bounds));
    }

    public ColoredBlitRenderState(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int uOffset, int vOffset, int width, int height,
          int spriteWidth, int spriteHeight, int colorFrom, int colorTo, boolean vertical) {
        //Note: For some reason graphics.getSprite(SpriteId) crashes in JEI, so we just get the sprite the same way the extractor would
        TextureAtlasSprite sprite = graphics.guiSprites.getSprite(texture);
        float u0 = sprite.getU((float) uOffset / spriteWidth);
        float u1 = sprite.getU((float) (uOffset + width) / spriteWidth);
        float v0 = sprite.getV((float) vOffset / spriteHeight);
        float v1 = sprite.getV((float) (vOffset + height) / spriteHeight);
        this(x, y, x + width, y + height, u0, u1, v0, v1, colorFrom, colorTo, vertical, new Matrix3x2f(graphics.pose()),
              Minecraft.getInstance().getTextureManager().getTexture(sprite.atlasLocation()), graphics.peekScissorStack());
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(vertical ? colorTo : colorFrom);
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(colorTo);
        vertexConsumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(vertical ? colorFrom : colorTo);
        vertexConsumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(colorFrom);
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }
}