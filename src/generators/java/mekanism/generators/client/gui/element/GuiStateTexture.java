package mekanism.generators.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiStateTexture extends GuiTexturedElement {

    private static final ResourceLocation stateHolder = MekanismGenerators.rl(ResourceType.GUI.getPrefix() + "state_holder.png");

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
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, getResource());
        blit(matrix, x, y, 0, 0, width, height, width, height);
        RenderSystem.setShaderTexture(0, onSupplier.getAsBoolean() ? onTexture : offTexture);
        blit(matrix, x + 2, y + 2, 0, 0, width - 4, height - 4, width - 4, height - 4);
    }
}