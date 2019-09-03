package mekanism.client.gui.button;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * From GuiButtonImage with a couple fixes and support for rendering differently when disabled
 */
@OnlyIn(Dist.CLIENT)
public class MekanismImageButton extends MekanismButton {

    //TODO: Extend this to support disabling, also have better numbers instead of having things be hardcoded.
    // Or maybe have this button be for ONLY single buttons
    private final ResourceLocation resourceLocation;
    private final boolean hasDisabledImage;

    public MekanismImageButton(int x, int y, int size, boolean hasDisabledImage, ResourceLocation resource, IPressable onPress) {
        this(x, y, size, hasDisabledImage, resource, onPress, null);
    }

    public MekanismImageButton(int x, int y, int size, boolean hasDisabledImage, ResourceLocation resource, IPressable onPress, IHoverable onHover) {
        super(x, y, size, size, "", onPress, onHover);
        this.resourceLocation = resource;
        this.hasDisabledImage = hasDisabledImage;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        //Ensure the color gets reset. The default GuiButtonImage doesn't so other GuiButton's can have the color leak out of them
        //TODO: Figure out if the color resets is even still needed
        MekanismRenderer.resetColor();
        MekanismRenderer.bindTexture(this.resourceLocation);
        GlStateManager.disableDepthTest();
        int texStartY = height;
        if (!this.active) {
            if (this.hasDisabledImage) {
                //Add support for having a different texture for when it is disabled
                texStartY = height * 2;
            }
        } else if (isHovered()) {
            texStartY = 0;
        }
        blit(x, y, 0, texStartY, width, height, width, height * (hasDisabledImage ? 3 : 2));
        GlStateManager.enableDepthTest();
    }
}