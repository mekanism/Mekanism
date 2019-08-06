package mekanism.client.gui.button;

import javax.annotation.Nonnull;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiButtonSeismicReader extends GuiButton {

    private final ResourceLocation resourceLocation;
    private final int offsetX;
    private final int offsetY;

    public GuiButtonSeismicReader(int id, int x, int y, int width, int height, int offsetX, int offsetY, ResourceLocation resource) {
        super(id, x, y, width, height, "");
        this.resourceLocation = resource;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            MekanismRenderer.resetColor();
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            mc.getTextureManager().bindTexture(this.resourceLocation);
            if (!this.enabled) {
                GlStateManager.color(0.25F, 0.25F, 0.25F);
            } else if (this.hovered) {
                GlStateManager.color(0.5F, 0.5F, 1);
            }
            this.drawTexturedModalRect(this.x, this.y, this.offsetX, this.offsetY, this.width, this.height);
            MekanismRenderer.resetColor();
        }
    }
}