package mekanism.client.gui.element.progress;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.progress.IProgressInfoHandler.IBooleanProgressInfoHandler;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerRecipeArea;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class GuiProgress extends GuiTexturedElement implements IRecipeViewerRecipeArea<GuiProgress>, ISupportsWarning<GuiProgress> {

    protected final IProgressInfoHandler handler;
    protected final ProgressType type;
    private IRecipeViewerRecipeType<?>[] recipeCategories;
    @Nullable
    private ColorDetails colorDetails;
    @Nullable
    private BooleanSupplier warningSupplier;

    public GuiProgress(IBooleanProgressInfoHandler handler, ProgressType type, IGuiWrapper gui, int x, int y) {
        this((IProgressInfoHandler) handler, type, gui, x, y);
    }

    public GuiProgress(IProgressInfoHandler handler, ProgressType type, IGuiWrapper gui, int x, int y) {
        super(type.getTexture(), gui, x, y, type.getWidth(), type.getHeight());
        this.type = type;
        this.handler = handler;
    }

    public GuiProgress colored(ColorDetails colorDetails) {
        this.colorDetails = colorDetails;
        return this;
    }

    @Override
    public GuiProgress warning(@NotNull WarningType type, @NotNull BooleanSupplier warningSupplier) {
        this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, gui().trackWarning(type, warningSupplier));
        return this;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (handler.isActive()) {
            ResourceLocation resource = getResource();
            guiGraphics.blit(resource, relativeX, relativeY, 0, 0, width, height, type.getTextureWidth(), type.getTextureHeight());
            boolean warning = warningSupplier != null && warningSupplier.getAsBoolean();
            double progress = warning ? 1 : getProgress();
            if (type.isVertical()) {
                int displayInt = (int) (progress * height);
                if (displayInt > 0) {
                    int innerOffsetY = 0;
                    if (type.isReverse()) {
                        innerOffsetY += type.getTextureHeight() - displayInt;
                    }
                    blit(guiGraphics, resource, relativeX, relativeY + innerOffsetY, type.getOverlayX(warning), type.getOverlayY(warning) + innerOffsetY, width, displayInt,
                          type.getTextureWidth(), type.getTextureHeight(), progress, warning);
                }
            } else {
                int innerOffsetX = type == ProgressType.BAR ? 1 : 0;
                int displayInt = (int) (progress * (width - 2 * innerOffsetX));
                if (displayInt > 0) {
                    if (type.isReverse()) {
                        innerOffsetX += type.getTextureWidth() - displayInt;
                    }
                    blit(guiGraphics, resource, relativeX + innerOffsetX, relativeY, type.getOverlayX(warning) + innerOffsetX, type.getOverlayY(warning), displayInt, height,
                          type.getTextureWidth(), type.getTextureHeight(), progress, warning);
                }
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
    public boolean isRecipeViewerAreaActive() {
        return handler.isActive();
    }

    @NotNull
    @Override
    public GuiProgress recipeViewerCategories(@NotNull IRecipeViewerRecipeType<?>... recipeCategories) {
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Nullable
    @Override
    public IRecipeViewerRecipeType<?>[] getRecipeCategories() {
        return recipeCategories;
    }

    private void blit(GuiGraphics guiGraphics, ResourceLocation resource, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight, double progress,
          boolean warning) {
        if (warning || colorDetails == null) {
            //If we are drawing a warning or don't have any color details just draw it normally
            guiGraphics.blit(resource, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
            return;
        }
        int colorFrom = colorDetails.getColorFrom();
        int colorTo = colorDetails.getColorTo();
        if (colorFrom == 0xFFFFFFFF && colorTo == 0xFFFFFFFF) {
            //No coloring needed, just use the normal blit method
            guiGraphics.blit(resource, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
            return;
        }
        //Merge of blit and fillGradient
        int x2 = x + width;
        int y2 = y + height;
        Matrix4f matrix = guiGraphics.pose().last().pose();
        float minU = uOffset / textureWidth;
        float maxU = (uOffset + width) / textureWidth;
        float minV = vOffset / textureHeight;
        float maxV = (vOffset + height) / textureHeight;

        float alphaFrom = MekanismRenderer.getAlpha(colorFrom);
        float redFrom = MekanismRenderer.getRed(colorFrom);
        float greenFrom = MekanismRenderer.getGreen(colorFrom);
        float blueFrom = MekanismRenderer.getBlue(colorFrom);
        float alphaTo = MekanismRenderer.getAlpha(colorTo);
        float redTo = MekanismRenderer.getRed(colorTo);
        float greenTo = MekanismRenderer.getGreen(colorTo);
        float blueTo = MekanismRenderer.getBlue(colorTo);
        //Adjust coloring to be based on how much of the progress bar is actually filled
        // so that it properly has the correct colors for the start and the end
        float percent = (float) progress;
        alphaTo = alphaFrom + percent * (alphaTo - alphaFrom);
        redTo = redFrom + percent * (redTo - redFrom);
        greenTo = greenFrom + percent * (greenTo - greenFrom);
        blueTo = blueFrom + percent * (blueTo - blueFrom);
        if (type.isReverse()) {
            //If we are going in the reverse direction flip the color portions
            // We have to do this here, instead of when we set colorFrom and colorTo
            // to ensure that the percentage is properly taken into account
            float alphaTemp = alphaTo;
            float redTemp = redTo;
            float greenTemp = greenTo;
            float blueTemp = blueTo;
            alphaTo = alphaFrom;
            redTo = redFrom;
            greenTo = greenFrom;
            blueTo = blueFrom;
            alphaFrom = alphaTemp;
            redFrom = redTemp;
            greenFrom = greenTemp;
            blueFrom = blueTemp;
        }
        //Prep colored blit
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, resource);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        BufferBuilder builder = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        if (type.isVertical()) {
            builder.addVertex(matrix, x, y2, 0)
                  .setUv(minU, maxV)
                  .setColor(redTo, greenTo, blueTo, alphaTo);
            builder.addVertex(matrix, x2, y2, 0)
                  .setUv(maxU, maxV)
                  .setColor(redTo, greenTo, blueTo, alphaTo);
            builder.addVertex(matrix, x2, y, 0)
                  .setUv(maxU, minV)
                  .setColor(redFrom, greenFrom, blueFrom, alphaFrom);
            builder.addVertex(matrix, x, y, 0)
                  .setUv(minU, minV)
                  .setColor(redFrom, greenFrom, blueFrom, alphaFrom);
        } else {
            builder.addVertex(matrix, x, y2, 0)
                  .setUv(minU, maxV)
                  .setColor(redFrom, greenFrom, blueFrom, alphaFrom);
            builder.addVertex(matrix, x2, y2, 0)
                  .setUv(maxU, maxV)
                  .setColor(redTo, greenTo, blueTo, alphaTo);
            builder.addVertex(matrix, x2, y, 0)
                  .setUv(maxU, minV)
                  .setColor(redTo, greenTo, blueTo, alphaTo);
            builder.addVertex(matrix, x, y, 0)
                  .setUv(minU, minV)
                  .setColor(redFrom, greenFrom, blueFrom, alphaFrom);
        }

        BufferUploader.drawWithShader(builder.buildOrThrow());
        //Reset blit and fill gradient states
        RenderSystem.disableBlend();
    }

    public interface ColorDetails {

        int getColorFrom();

        int getColorTo();
    }
}