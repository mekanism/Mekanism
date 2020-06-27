package mekanism.client.gui.element.progress;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import net.minecraft.util.ResourceLocation;

public class GuiProgress extends GuiTexturedElement implements IJEIRecipeArea<GuiProgress> {

    protected final IProgressInfoHandler handler;
    protected final ProgressType type;
    private ResourceLocation[] recipeCategories;

    public GuiProgress(IProgressInfoHandler handler, ProgressType type, IGuiWrapper gui, int x, int y) {
        super(type.getTexture(), gui, x, y, type.getWidth(), type.getHeight());
        this.type = type;
        this.handler = handler;
    }

    @Override
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (handler.isActive()) {
            minecraft.textureManager.bindTexture(getResource());
            func_238463_a_(matrix, field_230690_l_, field_230691_m_, 0, 0, field_230688_j_, field_230689_k_, type.getTextureWidth(), type.getTextureHeight());
            if (type.isVertical()) {
                int displayInt = (int) (handler.getProgress() * field_230689_k_);
                func_238463_a_(matrix, field_230690_l_, field_230691_m_, type.getOverlayX(), type.getOverlayY(), field_230688_j_, displayInt, type.getTextureWidth(), type.getTextureHeight());
            } else {
                int innerOffsetX = type == ProgressType.BAR ? 1 : 0;
                int displayInt = (int) (handler.getProgress() * (field_230688_j_ - 2 * innerOffsetX));
                func_238463_a_(matrix, field_230690_l_ + innerOffsetX, field_230691_m_, type.getOverlayX() + innerOffsetX, type.getOverlayY(), displayInt, field_230689_k_, type.getTextureWidth(), type.getTextureHeight());
            }
        }
    }

    @Override
    public boolean isActive() {
        return handler.isActive();
    }

    @Nonnull
    @Override
    public GuiProgress jeiCategories(@Nullable ResourceLocation... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation[] getRecipeCategories() {
        return recipeCategories;
    }
}