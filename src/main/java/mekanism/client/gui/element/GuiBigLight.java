package mekanism.client.gui.element;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiBigLight extends GuiTexturedElement {

    private static final Identifier ENABLED = Mekanism.rl("light/big/enabled");
    private static final Identifier DISABLED = Mekanism.rl("light/big/disabled");
    private final BooleanSupplier lightSupplier;

    public GuiBigLight(IGuiWrapper gui, int x, int y, BooleanSupplier lightSupplier) {
        super(GuiInnerScreen.SCREEN, gui, x, y, 14, 14);
        this.lightSupplier = lightSupplier;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, lightSupplier.getAsBoolean() ? ENABLED : DISABLED, relativeX + 1, relativeY + 1, width - 2, height - 2);
    }
}