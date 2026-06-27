package mekanism.client.gui.element.bar;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.Mekanism;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public abstract class GuiBar<INFO extends IBarInfoHandler> extends GuiTexturedElement implements ISupportsWarning<GuiBar<INFO>> {

    public static final Identifier BAR = Mekanism.rl("bar/base");

    private final INFO handler;
    protected final boolean horizontal;
    @Nullable
    private BooleanSupplier warningSupplier;
    @Nullable
    private Component lastInfo;
    @Nullable
    private Tooltip lastTooltip;

    public GuiBar(IGuiWrapper gui, INFO handler, int x, int y, int width, int height, boolean horizontal) {
        this(BAR, gui, handler, x, y, width, height, horizontal);
    }

    public GuiBar(Identifier resource, IGuiWrapper gui, INFO handler, int x, int y, int width, int height, boolean horizontal) {
        super(resource, gui, x, y, width + 2, height + 2);
        this.handler = handler;
        this.horizontal = horizontal;
    }

    @Override
    public GuiBar<INFO> warning(WarningType type, BooleanSupplier warningSupplier) {
        this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, gui().trackWarning(type, warningSupplier));
        return this;
    }

    public INFO getHandler() {
        return handler;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Render the bar
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        boolean warning = warningSupplier != null && warningSupplier.getAsBoolean();
        if (warning) {
            //Draw background (we do it regardless of if we are full or not as if the thing being drawn has transparency we may as well show the background)
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, WARNING_BACKGROUND_TEXTURE, relativeX + 1, relativeY + 1, width - 2, height - 2);
        }
        double handlerLevel = handler.getLevel();
        //If there are any contents render them
        if (handlerLevel > 0) {
            renderBarContents(guiGraphics, mouseX, mouseY, partialTicks, handlerLevel);
            if (warning && handlerLevel >= 0.98) {
                //Greater than 98% filled, render secondary piece anyway just to make it more visible
                int x0 = relativeX + 1;
                int y0 = relativeY + 1;
                int targetWidth = width - 2;
                int targetHeight = height - 2;
                //Note: We also scissor the drawing to start after half the dimension so that we are sure it will properly line up with
                // the one drawn to the background if the contents of things are translucent
                if (horizontal) {
                    targetHeight /= 2;
                    y0 += targetHeight;
                } else {//vertical
                    targetWidth /= 2;
                    x0 += targetWidth;
                }
                guiGraphics.enableScissor(x0, y0, x0 + targetWidth, y0 + targetHeight);
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, WARNING_TEXTURE, relativeX + 1, relativeY + 1, width - 2, height - 2);
                guiGraphics.disableScissor();
            }
        }
    }

    protected void renderBarContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel) {
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        Component tooltip = handler.getTooltip();
        if (!Objects.equals(tooltip, lastInfo)) {
            lastTooltip = TooltipUtils.create(tooltip);
        }
        lastInfo = tooltip;
        setTooltip(lastTooltip);
    }

    protected static int calculateSize(double progress, int size) {
        //Based on how AbstractFurnaceScreen calculates the flame progress height to always have at least 1 pixel showing if it is active
        return Mth.ceil(progress * (size - 1)) + 1;
    }

    public interface IBarInfoHandler {

        @Nullable
        default Component getTooltip() {
            return null;
        }

        double getLevel();
    }
}