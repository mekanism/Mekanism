package mekanism.client.gui.element.progress;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.progress.IProgressInfoHandler.IBooleanProgressInfoHandler;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import net.minecraft.util.ResourceLocation;

public class GuiProgress extends GuiTexturedElement implements IJEIRecipeArea<GuiProgress> {

    protected final IProgressInfoHandler handler;
    protected final ProgressType type;
    private ResourceLocation[] recipeCategories;

    public GuiProgress(IBooleanProgressInfoHandler handler, ProgressType type, IGuiWrapper gui, int x, int y) {
        this((IProgressInfoHandler) handler, type, gui, x, y);
    }

    public GuiProgress(IProgressInfoHandler handler, ProgressType type, IGuiWrapper gui, int x, int y) {
        super(type.getTexture(), gui, x, y, type.getWidth(), type.getHeight());
        this.type = type;
        this.handler = handler;
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        if (handler.isActive()) {
            minecraft.textureManager.bindTexture(getResource());
            blit(matrix, x, y, 0, 0, width, height, type.getTextureWidth(), type.getTextureHeight());
            if (type.isVertical()) {
                int displayInt = (int) (getProgress() * height);
                blit(matrix, x, y, type.getOverlayX(), type.getOverlayY(), width, displayInt, type.getTextureWidth(), type.getTextureHeight());
            } else {
                int innerOffsetX = type == ProgressType.BAR ? 1 : 0;
                int displayInt = (int) (getProgress() * (width - 2 * innerOffsetX));
                blit(matrix, x + innerOffsetX, y, type.getOverlayX() + innerOffsetX, type.getOverlayY(), displayInt, height, type.getTextureWidth(), type.getTextureHeight());
            }
        }
    }

    protected double getProgress() {
        //Ensure we clamp the progress to a single unit so that if we installed a bunch of speed upgrades
        // and are unable to continue progressing due to lack of energy that we don't show a bunch of arrows
        // that are stretched past their background area
        //TODO: Eventually we may want to instead make this "finish" the recipe instead of basically locking it at
        // max progress
        return Math.min(handler.getProgress(), 1);
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