package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class GuiBigLight extends GuiTexturedElement {

    private static final ResourceLocation LIGHTS = MekanismUtils.getResource(ResourceType.GUI, "big_lights.png");
    private final GuiInnerScreen screen;
    private final BooleanSupplier lightSupplier;

    public GuiBigLight(IGuiWrapper gui, int x, int y, BooleanSupplier lightSupplier) {
        super(LIGHTS, gui, x, y, 14, 14);
        this.screen = new GuiInnerScreen(gui, x, y, width, height);
        this.lightSupplier = lightSupplier;
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        screen.drawBackground(matrix, mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        blit(matrix, x + 1, y + 1, lightSupplier.getAsBoolean() ? 0 : 12, 0, width - 2, height - 2, 24, 12);
    }
}