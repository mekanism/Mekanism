package mekanism.client.gui.button;

import com.mojang.blaze3d.platform.GlStateManager;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * From GuiButtonImage with a couple fixes and support for rendering differently when disabled
 */
//TODO: Update javadocs
@OnlyIn(Dist.CLIENT)
public class MekanismImageButton extends MekanismButton {

    private final ResourceLocation resourceLocation;

    public MekanismImageButton(int x, int y, int size, ResourceLocation resource, IPressable onPress) {
        this(x, y, size, resource, onPress, null);
    }

    public MekanismImageButton(int x, int y, int size, ResourceLocation resource, IPressable onPress, IHoverable onHover) {
        //TODO: It seems to render the border incorrectly if we don't lower height by one
        super(x, y, size, size - 1, "", onPress, onHover);
        this.resourceLocation = resource;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        //Ensure the color gets reset. The default ImageButton doesn't so other Button's can have the color leak out of them
        //TODO: Figure out if the color resets is even still needed
        MekanismRenderer.resetColor();
        super.renderButton(mouseX, mouseY, partialTicks);
        MekanismRenderer.bindTexture(this.resourceLocation);
        blit(x, y, 0, 0, width, height + 1, width, height + 1);
        GlStateManager.enableDepthTest();
    }
}