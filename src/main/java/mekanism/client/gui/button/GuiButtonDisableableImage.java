package mekanism.client.gui.button;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * From GuiButtonImage with a couple fixes and support for rendering differently when disabled
 */
@OnlyIn(Dist.CLIENT)
public class GuiButtonDisableableImage extends Button {

    private final ResourceLocation resourceLocation;
    private final int xTexStart;
    private final int yTexStart;
    private final int hoverOffset;
    private final int disabledOffset;

    public GuiButtonDisableableImage(int x, int y, int width, int height, int offsetX, int offsetY, int hoverOffset, ResourceLocation resource, IPressable onPress) {
        this(x, y, width, height, offsetX, offsetY, hoverOffset, 0, resource, onPress);
    }

    public GuiButtonDisableableImage(int x, int y, int width, int height, int offsetX, int offsetY, int hoverOffset, int disabledOffset, ResourceLocation resource, IPressable onPress) {
        super(x, y, width, height, "", onPress);
        this.xTexStart = offsetX;
        this.yTexStart = offsetY;
        this.hoverOffset = hoverOffset;
        this.disabledOffset = disabledOffset;
        this.resourceLocation = resource;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            //Ensure the color gets reset. The default GuiButtonImage doesn't so other GuiButton's can have the color leak out of them
            MekanismRenderer.resetColor();
            MekanismRenderer.bindTexture(this.resourceLocation);
            GlStateManager.disableDepthTest();
            int j = this.yTexStart;

            if (!this.active) {
                //Add support for having a different texture for when it is disabled
                j += this.disabledOffset;
            } else if (isMouseOver(mouseX, mouseY)) {
                j += this.hoverOffset;
            }

            blit(this.x, this.y, this.xTexStart, j, this.width, this.height);
            GlStateManager.enableDepthTest();
        }
    }
}