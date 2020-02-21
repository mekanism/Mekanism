package mekanism.generators.client.gui.element;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.util.ResourceLocation;

public class GuiStateTexture extends GuiTexturedElement {

    private static final ResourceLocation stateHolder = MekanismGenerators.rl(ResourceType.GUI_ELEMENT.getPrefix() + "state_holder.png");

    private final BooleanSupplier onSupplier;
    private final ResourceLocation onTexture;
    private final ResourceLocation offTexture;

    public GuiStateTexture(IGuiWrapper gui, int x, int y, BooleanSupplier onSupplier, ResourceLocation onTexture, ResourceLocation offTexture) {
        super(stateHolder, gui, x, y, 16, 16);
        this.onSupplier = onSupplier;
        this.onTexture = onTexture;
        this.offTexture = offTexture;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        minecraft.textureManager.bindTexture(onSupplier.getAsBoolean() ? onTexture : offTexture);
        guiObj.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0, 0, width - 4, height - 4, width - 4, height - 4);
    }
}