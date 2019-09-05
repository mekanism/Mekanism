package mekanism.client.gui.button;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SeismicReaderButton extends MekanismButton {

    private final ResourceLocation resourceLocation;
    private final int offsetX;
    private final int offsetY;

    public SeismicReaderButton(IGuiWrapper gui, int x, int y, int width, int height, int offsetX, int offsetY, ResourceLocation resource, IPressable onPress) {
        this(gui, x, y, width, height, offsetX, offsetY, resource, onPress, null);
    }

    public SeismicReaderButton(IGuiWrapper gui, int x, int y, int width, int height, int offsetX, int offsetY, ResourceLocation resource, IPressable onPress, IHoverable onHover) {
        super(gui, x, y, width, height, "", onPress, onHover);
        this.resourceLocation = resource;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        MekanismRenderer.bindTexture(this.resourceLocation);
        if (!this.active) {
            GlStateManager.color3f(0.25F, 0.25F, 0.25F);
        } else if (isHovered()) {
            GlStateManager.color3f(0.5F, 0.5F, 1);
        } else {
            MekanismRenderer.resetColor();
        }
        blit(this.x, this.y, this.offsetX, this.offsetY, this.width, this.height);
        MekanismRenderer.resetColor();
    }
}