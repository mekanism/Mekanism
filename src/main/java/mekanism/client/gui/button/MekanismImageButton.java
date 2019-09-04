package mekanism.client.gui.button;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//TODO: Update javadocs
@OnlyIn(Dist.CLIENT)
public class MekanismImageButton extends MekanismButton {

    private final ResourceLocation resourceLocation;
    private final int textureSize;

    public MekanismImageButton(int x, int y, int size, ResourceLocation resource, IPressable onPress) {
        this(x, y, size, size, resource, onPress);
    }

    public MekanismImageButton(int x, int y, int size, ResourceLocation resource, IPressable onPress, IHoverable onHover) {
        this(x, y, size, size, resource, onPress, onHover);
    }

    public MekanismImageButton(int x, int y, int size, int textureSize, ResourceLocation resource, IPressable onPress) {
        this(x, y, size, textureSize, resource, onPress, null);
    }

    public MekanismImageButton(int x, int y, int size, int textureSize, ResourceLocation resource, IPressable onPress, IHoverable onHover) {
        super(x, y, size, size, "", onPress, onHover);
        this.resourceLocation = resource;
        this.textureSize = textureSize;
    }

    //TODO: Convert this stuff into a javadoc
    //Based off how it is drawn in Widget, except that instead of drawing left half and right half, we draw all four corners individually
    // The benefit of drawing all four corners instead of just left and right halves, is that we ensure we include the bottom black bar of the texture
    // Math has also been added to fix rendering odd size buttons.
    // we also don't even bother calling the rendering of an empty string
    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        MekanismRenderer.resetColor();
        MekanismRenderer.bindTexture(WIDGETS_LOCATION);
        int i = this.getYImage(this.isHovered());
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        int halfWidthLeft = width / 2;
        int halfWidthRight = width % 2 == 0 ? halfWidthLeft : halfWidthLeft + 1;
        int halfHeightTop = height / 2;
        int halfHeightBottom = height % 2 == 0 ? halfHeightTop : halfHeightTop + 1;
        int position = 46 + i * 20;
        //Left Top Corner
        blit(this.x, this.y, 0, position, halfWidthLeft, halfHeightTop);
        //Left Bottom Corner
        blit(this.x, this.y + halfHeightTop, 0, position + 20 - halfHeightBottom, halfWidthLeft, halfHeightBottom);
        //Right Top Corner
        blit(this.x + halfWidthLeft, this.y, 200 - halfWidthRight, position, halfWidthRight, halfHeightTop);
        //Right Bottom Corner
        blit(this.x + halfWidthLeft, this.y + halfHeightTop, 200 - halfWidthRight, position + 20 - halfHeightBottom, halfWidthRight, halfHeightBottom);

        //TODO: Add support for buttons that are larger than 200x20 in either direction (most likely would be in the height direction

        MekanismRenderer.bindTexture(this.resourceLocation);
        blit(x, y, width, height, 0, 0, textureSize, textureSize, textureSize, textureSize);
        GlStateManager.disableBlend();
    }
}