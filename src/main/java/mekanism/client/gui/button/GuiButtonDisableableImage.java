package mekanism.client.gui.button;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.widget.button.ImageButton;
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

    public GuiButtonDisableableImage(int x, int y, int width, int height, int offsetX, int offsetY, int hoverOffset, ResourceLocation resource, IPressable pressable) {
        this(x, y, width, height, offsetX, offsetY, hoverOffset, 0, resource, pressable);
    }

    //public Button(int x, int y, int width, int height, String p_i51141_5_, Button.IPressable p_i51141_6_) {
    public GuiButtonDisableableImage(int x, int y, int width, int height, int offsetX, int offsetY, int hoverOffset, int disabledOffset, ResourceLocation resource, IPressable pressable) {
        super(x, y, width, height, "", pressable);
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

            drawTexturedModalRect(this.x, this.y, this.xTexStart, j, this.width, this.height);
            GlStateManager.enableDepthTest();
        }
    }
}