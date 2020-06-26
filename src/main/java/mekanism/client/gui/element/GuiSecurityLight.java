package mekanism.client.gui.element;

import java.util.function.IntSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiSecurityLight extends GuiTexturedElement {

    private static final ResourceLocation LIGHTS = MekanismUtils.getResource(ResourceType.GUI, "security_lights.png");
    private final GuiInnerScreen screen;
    private final IntSupplier lightSupplier;

    public GuiSecurityLight(IGuiWrapper gui, int x, int y, IntSupplier lightSupplier) {
        super(LIGHTS, gui, x, y, 8, 8);
        this.screen = new GuiInnerScreen(gui, x, y, field_230688_j_, field_230689_k_);
        this.lightSupplier = lightSupplier;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        screen.renderButton(mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        blit(field_230690_l_ + 1, field_230691_m_ + 1, 6 * lightSupplier.getAsInt(), 0, field_230688_j_ - 2, field_230689_k_ - 2, 18, 6);
    }
}