package mekanism.client.gui.element;

import java.util.function.IntSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiSecurityLight extends GuiTexturedElement {

    private static final ResourceLocation LIGHTS = MekanismUtils.getResource(ResourceType.GUI, "security_lights.png");
    private final IntSupplier lightSupplier;

    public GuiSecurityLight(IGuiWrapper gui, int x, int y, IntSupplier lightSupplier) {
        super(LIGHTS, gui, x, y, 8, 8);
        this.lightSupplier = lightSupplier;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        renderBackgroundTexture(guiGraphics, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
        guiGraphics.blit(getResource(), relativeX + 1, relativeY + 1, 6 * lightSupplier.getAsInt(), 0, width - 2, height - 2, 18, 6);
    }
}