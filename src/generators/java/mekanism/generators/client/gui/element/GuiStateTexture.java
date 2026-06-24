package mekanism.generators.client.gui.element;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class GuiStateTexture extends GuiTexturedElement {

    private static final Identifier stateHolder = MekanismGenerators.rl("state/holder");

    private final BooleanSupplier onSupplier;
    private final Identifier onTexture;
    private final Identifier offTexture;

    public GuiStateTexture(IGuiWrapper gui, int x, int y, BooleanSupplier onSupplier, Identifier onTexture, Identifier offTexture) {
        super(stateHolder, gui, x, y, 16, 16);
        this.onSupplier = onSupplier;
        this.onTexture = onTexture;
        this.offTexture = offTexture;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        Identifier resource = onSupplier.getAsBoolean() ? onTexture : offTexture;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resource, relativeX + 2, relativeY + 2, width - 4, height - 4);
    }
}