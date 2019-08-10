package mekanism.client.gui.button;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiButtonSeismicReader extends Button {

    private final ResourceLocation resourceLocation;
    private final int offsetX;
    private final int offsetY;

    public GuiButtonSeismicReader(int x, int y, int width, int height, int offsetX, int offsetY, ResourceLocation resource, IPressable onPress) {
        super(x, y, width, height, "", onPress);
        this.resourceLocation = resource;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            MekanismRenderer.resetColor();
            MekanismRenderer.bindTexture(this.resourceLocation);
            if (!this.active) {
                GlStateManager.color3f(0.25F, 0.25F, 0.25F);
            } else if (isMouseOver(mouseX, mouseY)) {
                GlStateManager.color3f(0.5F, 0.5F, 1);
            }
            blit(this.x, this.y, this.offsetX, this.offsetY, this.width, this.height);
            MekanismRenderer.resetColor();
        }
    }
}