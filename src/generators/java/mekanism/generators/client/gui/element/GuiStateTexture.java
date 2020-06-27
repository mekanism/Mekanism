package mekanism.generators.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.util.ResourceLocation;

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
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        func_238463_a_(matrix, field_230690_l_, field_230691_m_, 0, 0, field_230688_j_, field_230689_k_, field_230688_j_, field_230689_k_);
        minecraft.textureManager.bindTexture(onSupplier.getAsBoolean() ? onTexture : offTexture);
        func_238463_a_(matrix, field_230690_l_ + 2, field_230691_m_ + 2, 0, 0, field_230688_j_ - 4, field_230689_k_ - 4, field_230688_j_ - 4, field_230689_k_ - 4);
    }
}