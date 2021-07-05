package mekanism.client.gui.element.progress;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.progress.IProgressInfoHandler.IBooleanProgressInfoHandler;
import mekanism.client.gui.warning.WarningTracker.WarningType;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;

public class GuiProgress extends GuiTexturedElement implements IJEIRecipeArea<GuiProgress> {

    protected final IProgressInfoHandler handler;
    protected final ProgressType type;
    private ResourceLocation[] recipeCategories;
    @Nullable
    private ColorDetails colorDetails;
    @Nullable
    private BooleanSupplier warningSupplier;
    private boolean useFullProgressForWarning;

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

    //TODO - WARNING SYSTEM: Hook up usage of warnings
    public GuiProgress warning(@Nonnull WarningType type, @Nonnull BooleanSupplier warningSupplier) {
        return warning(type, warningSupplier, true);
    }

    public GuiProgress warning(@Nonnull WarningType type, @Nonnull BooleanSupplier warningSupplier, boolean useFullProgressForWarning) {
        this.warningSupplier = gui().trackWarning(type, warningSupplier);
        //TODO - WARNING SYSTEM: Evaluate if we even want this to be a thing?
        this.useFullProgressForWarning = useFullProgressForWarning;
        return this;
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        if (handler.isActive()) {
            minecraft.textureManager.bind(getResource());
            blit(matrix, x, y, 0, 0, width, height, type.getTextureWidth(), type.getTextureHeight());
            boolean warning = warningSupplier != null && warningSupplier.getAsBoolean();
            double progress = warning && useFullProgressForWarning ? 1 : getProgress();
            if (type.isVertical()) {
                int displayInt = (int) (progress * height);
                if (displayInt > 0) {
                    int innerOffsetY = 0;
                    if (type.isReverse()) {
                        innerOffsetY += type.getTextureHeight() - displayInt;
                    }
                    blit(matrix, x, y + innerOffsetY, type.getOverlayX(warning), type.getOverlayY(warning) + innerOffsetY, width, displayInt,
                          type.getTextureWidth(), type.getTextureHeight(), progress, warning);
                }
            } else {
                int innerOffsetX = type == ProgressType.BAR ? 1 : 0;
                int displayInt = (int) (progress * (width - 2 * innerOffsetX));
                if (displayInt > 0) {
                    if (type.isReverse()) {
                        innerOffsetX += type.getTextureWidth() - displayInt;
                    }
                    blit(matrix, x + innerOffsetX, y, type.getOverlayX(warning) + innerOffsetX, type.getOverlayY(warning), displayInt, height,
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

    private void blit(MatrixStack matrixStack, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight, double progress,
          boolean warning) {
        if (warning || colorDetails == null) {
            //If we are drawing a warning or don't have any color details just draw it normally
            blit(matrixStack, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
            return;
        }
        int colorFrom = colorDetails.getColorFrom();
        int colorTo = colorDetails.getColorTo();
        if (colorFrom == 0xFFFFFFFF && colorTo == 0xFFFFFFFF) {
            //No coloring needed, just use the normal blit method
            blit(matrixStack, x, y, uOffset, vOffset, width, height, textureWidth, textureHeight);
            return;
        }
        //Merge of blit and fillGradient
        int x2 = x + width;
        int y2 = y + height;
        Matrix4f matrix = matrixStack.last().pose();
        float minU = uOffset / (float) textureWidth;
        float maxU = (uOffset + width) / (float) textureWidth;
        float minV = vOffset / (float) textureHeight;
        float maxV = (vOffset + height) / (float) textureHeight;

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
        //Prep fill gradient
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);

        if (type.isVertical()) {
            builder.vertex(matrix, x, y2, 0).color(redTo, greenTo, blueTo, alphaTo).uv(minU, maxV).endVertex();
            builder.vertex(matrix, x2, y2, 0).color(redTo, greenTo, blueTo, alphaTo).uv(maxU, maxV).endVertex();
            builder.vertex(matrix, x2, y, 0).color(redFrom, greenFrom, blueFrom, alphaFrom).uv(maxU, minV).endVertex();
            builder.vertex(matrix, x, y, 0).color(redFrom, greenFrom, blueFrom, alphaFrom).uv(minU, minV).endVertex();
        } else {
            builder.vertex(matrix, x, y2, 0).color(redFrom, greenFrom, blueFrom, alphaFrom).uv(minU, maxV).endVertex();
            builder.vertex(matrix, x2, y2, 0).color(redTo, greenTo, blueTo, alphaTo).uv(maxU, maxV).endVertex();
            builder.vertex(matrix, x2, y, 0).color(redTo, greenTo, blueTo, alphaTo).uv(maxU, minV).endVertex();
            builder.vertex(matrix, x, y, 0).color(redFrom, greenFrom, blueFrom, alphaFrom).uv(minU, minV).endVertex();
        }

        tessellator.end();
        //Reset blit and fill gradient states
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        MekanismRenderer.resetColor();
    }

    public interface ColorDetails {

        int getColorFrom();

        int getColorTo();
    }
}