package mekanism.client.gui.element.progress;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.progress.IProgressInfoHandler.IBooleanProgressInfoHandler;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerRecipeArea;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public class GuiProgress extends GuiTexturedElement implements IRecipeViewerRecipeArea<GuiProgress>, ISupportsWarning<GuiProgress> {

    protected final IProgressInfoHandler handler;
    protected final ProgressType type;
    private IRecipeViewerRecipeType<?> @Nullable [] recipeCategories;
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
    public GuiProgress warning(WarningType type, BooleanSupplier warningSupplier) {
        this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, gui().trackWarning(type, warningSupplier));
        return this;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (handler.isActive()) {
            Identifier resource = getResource();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resource, relativeX, relativeY, 0, 0, width, height, type.getTextureWidth(), type.getTextureHeight());
            boolean warning = warningSupplier != null && warningSupplier.getAsBoolean();
            float progress = warning ? 1 : getProgress();
            if (type.isVertical()) {
                int displayInt = (int) (progress * height);
                if (displayInt > 0) {
                    int innerOffsetY = 0;
                    if (type.isReverse()) {
                        innerOffsetY += type.getTextureHeight() - displayInt;
                    }
                    blit(guiGraphics, resource, relativeX, relativeY + innerOffsetY, type.getOverlayX(warning), type.getOverlayY(warning) + innerOffsetY,
                          width, displayInt, progress, warning);
                }
            } else {
                int innerOffsetX = type == ProgressType.BAR ? 1 : 0;
                int displayInt = (int) (progress * (width - 2 * innerOffsetX));
                if (displayInt > 0) {
                    if (type.isReverse()) {
                        innerOffsetX += type.getTextureWidth() - displayInt;
                    }
                    blit(guiGraphics, resource, relativeX + innerOffsetX, relativeY, type.getOverlayX(warning) + innerOffsetX, type.getOverlayY(warning),
                          displayInt, height, progress, warning);
                }
            }
        }
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
        this.recipeCategories = recipeCategories;
        return this;
    }

    @Override
    public IRecipeViewerRecipeType<?> @Nullable [] getRecipeCategories() {
        return recipeCategories;
    }

    private void blit(GuiGraphicsExtractor guiGraphics, Identifier resource, int x, int y, float uOffset, float vOffset, int width, int height, float progress,
          boolean warning) {
        if (warning || colorDetails == null) {
            //If we are drawing a warning or don't have any color details just draw it normally
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resource, x, y, uOffset, vOffset, width, height, type.getTextureWidth(), type.getTextureHeight());
            return;
        }
        int colorFrom = colorDetails.getColorFrom();
        int colorTo = colorDetails.getColorTo();
        if (colorFrom == CommonColors.WHITE && colorTo == CommonColors.WHITE) {
            //No coloring needed, just use the normal blit method
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resource, x, y, uOffset, vOffset, width, height, type.getTextureWidth(), type.getTextureHeight());
            return;
        }
        //Merge of blit and fillGradient
        int to, from;
        if (type.isReverse()) {
            from = colorTo;
            to = ARGB.srgbLerp(progress, colorTo, colorFrom);
        } else {
            from = colorFrom;
            to = ARGB.srgbLerp(progress, colorFrom, colorTo);
        }
        float u0 = uOffset / type.getTextureWidth();
        float u1 = (uOffset + width) / type.getTextureWidth();
        float v0 = vOffset / type.getTextureHeight();
        float v1 = (vOffset + height) / type.getTextureHeight();
        guiGraphics.submitGuiElementRenderState(new ProgressRenderState(x, y, x + width, y + height, u0, u1, v0, v1, from, to, type.isVertical(),
              new Matrix3x2f(guiGraphics.pose()), minecraft.getTextureManager().getTexture(resource), guiGraphics.peekScissorStack()));

    }

    public interface ColorDetails {

        int getColorFrom();

        int getColorTo();
    }

    private record ProgressRenderState(int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int colorFrom, int colorTo, boolean vertical,
                                       Matrix3x2fc pose, TextureSetup textureSetup, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds
    ) implements GuiElementRenderState {

        public ProgressRenderState(int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int colorFrom, int colorTo, boolean vertical,
              Matrix3x2fc pose, AbstractTexture texture, @Nullable ScreenRectangle scissorArea) {
            ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
            this(x0, y0, x1, y1, u0, u1, v0, v1, colorFrom, colorTo, vertical, pose, TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()),
                  scissorArea, scissorArea == null ? bounds : scissorArea.intersection(bounds));
        }

        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            vertexConsumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(vertical ? colorTo : colorFrom);
            vertexConsumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(colorTo);
            vertexConsumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(vertical ? colorFrom : colorTo);
            vertexConsumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(colorFrom);
        }

        @Override
        public RenderPipeline pipeline() {
            return RenderPipelines.GUI_TEXTURED;
        }
    }
}