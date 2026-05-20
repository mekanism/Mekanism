package mekanism.generators.client.gui.element;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class GuiStateTexture extends GuiTexturedElement {

    private static final Identifier stateHolder = MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "state_holder.png");

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
    public void drawBackground(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getResource(), relativeX, relativeY, 0, 0, width, height, width, height);
        Identifier resource = onSupplier.getAsBoolean() ? onTexture : offTexture;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resource, relativeX + 2, relativeY + 2, 0, 0, width - 4, height - 4, width - 4, height - 4);
    }
}