package mekanism.client.gui.button;

import mekanism.client.render.MekanismRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        super.renderButton(mouseX, mouseY, partialTicks);
        MekanismRenderer.bindTexture(this.resourceLocation);
        blit(x, y, width, height, 0, 0, textureSize, textureSize, textureSize, textureSize);
    }
}