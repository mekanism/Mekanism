package mekanism.client.gui.element.progress;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.progress.IProgressInfoHandler.IBooleanProgressInfoHandler;
import mekanism.client.gui.element.state.ColoredBlitRenderState;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class GuiProgress extends GuiTexturedElement implements ISupportsWarning<GuiProgress> {

    protected final IProgressInfoHandler handler;
    protected final ProgressType type;
    @Nullable
    private ColorDetails colorDetails;
    @Nullable
    private BooleanSupplier warningSupplier;

    public GuiProgress(IBooleanProgressInfoHandler handler, ProgressType type, IGuiWrapper gui, int x, int y) {
        this((IProgressInfoHandler) handler, type, gui, x, y);
    }

    public GuiProgress(IProgressInfoHandler handler, ProgressType type, IGuiWrapper gui, int x, int y) {
        super(type.emptyTexture(), gui, x, y, type.getWidth(), type.getHeight());
        this.type = type;
        this.handler = handler;
    }

    public GuiProgress colored(ColorDetails colorDetails) {
        this.colorDetails = colorDetails;
        return this;
    }

    @Override
    public GuiProgress warning(WarningType type, BooleanSupplier warningSupplier) {
        this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, gui().trackWarning(type, warningSupplier));
        return this;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (handler.isActive()) {
            boolean warning = warningSupplier != null && warningSupplier.getAsBoolean();
            Identifier texture = type.texture(warning);
            float progress = warning ? 1 : getProgress();
            if (progress == 0) {
                //Skip drawing if there is no progress
                return;
            }
            if (type.isVertical()) {
                int progressHeight = calculateProgressSize(progress, height);
                int innerOffsetY = 0;
                if (type.isReverse()) {
                    innerOffsetY += type.getHeight() - progressHeight;
                }
                blit(guiGraphics, texture, relativeX, relativeY + innerOffsetY, 0, innerOffsetY, width, progressHeight, progress, warning);
            } else {
                int innerOffsetX = type == ProgressType.BAR ? 1 : 0;
                int progressWidth = calculateProgressSize(progress, width - 2 * innerOffsetX);
                if (type.isReverse()) {
                    innerOffsetX += type.getWidth() - progressWidth;
                }
                blit(guiGraphics, texture, relativeX + innerOffsetX, relativeY, innerOffsetX, 0, progressWidth, height, progress, warning);
            }
        }
    }

    private int calculateProgressSize(float progress, int size) {
        //Based on how AbstractFurnaceScreen calculates the flame progress height to always have at least 1 pixel showing if it is active
        return Mth.ceil(progress * (size - 1)) + 1;
    }

    protected float getProgress() {
        //Ensure we clamp the progress to a single unit so that if we installed a bunch of speed upgrades
        // and are unable to continue progressing due to lack of energy that we don't show a bunch of arrows
        // that are stretched past their background area
        //TODO: Eventually we may want to instead make this "finish" the recipe instead of basically locking it at
        // max progress
        return Math.min(handler.getProgress(), 1);
    }

    @Override
    public boolean isRecipeViewerAreaActive() {
        return handler.isActive();
    }

    @Override
    public GuiProgress recipeViewerCategories(IRecipeViewerRecipeType<?>... recipeCategories) {
        super.recipeViewerCategories(recipeCategories);
        return this;
    }

    @Override
    public GuiProgress recipeViewerCategory(IRecipeLookupHandler<?> recipeLookup) {
        super.recipeViewerCategory(recipeLookup);
        return this;
    }

    private void blit(GuiGraphicsExtractor guiGraphics, Identifier texture, int x, int y, int uOffset, int vOffset, int width, int height, float progress, boolean warning) {
        if (!warning && colorDetails != null) {
            int colorFrom = colorDetails.getColorFrom();
            int colorTo = colorDetails.getColorTo();
            if (colorFrom != CommonColors.WHITE || colorTo != CommonColors.WHITE) {
                //Merge of blit and fillGradient
                int to, from;
                if (type.isReverse()) {
                    from = colorTo;
                    to = ARGB.srgbLerp(progress, colorTo, colorFrom);
                } else {
                    from = colorFrom;
                    to = ARGB.srgbLerp(progress, colorFrom, colorTo);
                }
                guiGraphics.submitGuiElementRenderState(new ColoredBlitRenderState(guiGraphics, texture, x, y, uOffset, vOffset, width, height, type.getWidth(), type.getHeight(), from, to, type.isVertical()));
                return;
            }//No coloring needed, just use the normal blit method
        }//If we are drawing a warning or don't have any color details just draw it normally
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, type.getWidth(), type.getHeight(), uOffset, vOffset, x, y, width, height);
    }

    public interface ColorDetails {

        int getColorFrom();

        int getColorTo();
    }
}