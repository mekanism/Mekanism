package mekanism.client.gui.element.bar;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiBar<INFO extends IBarInfoHandler> extends GuiTexturedElement implements ISupportsWarning<GuiBar<INFO>> {

    public static final ResourceLocation BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "base.png");

    private final INFO handler;
    protected final boolean horizontal;
    @Nullable
    private BooleanSupplier warningSupplier;
    @Nullable
    private Component lastInfo;
    @Nullable
    private Tooltip lastTooltip;

    public GuiBar(ResourceLocation resource, IGuiWrapper gui, INFO handler, int x, int y, int width, int height, boolean horizontal) {
        super(resource, gui, x, y, width + 2, height + 2);
        this.handler = handler;
        this.horizontal = horizontal;
    }

    @Override
    public GuiBar<INFO> warning(@NotNull WarningType type, @NotNull BooleanSupplier warningSupplier) {
        this.warningSupplier = ISupportsWarning.compound(this.warningSupplier, gui().trackWarning(type, warningSupplier));
        return this;
    }

    public INFO getHandler() {
        return handler;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Render the bar
        renderExtendedTexture(guiGraphics, BAR, 2, 2);
        boolean warning = warningSupplier != null && warningSupplier.getAsBoolean();
        if (warning) {
            //Draw background (we do it regardless of if we are full or not as if the thing being drawn has transparency
            // we may as well show the background)
            guiGraphics.blit(WARNING_BACKGROUND_TEXTURE, relativeX + 1, relativeY + 1, 0, 0, width - 2, height - 2, 256, 256);
        }
        //Render Contents
        drawContentsChecked(guiGraphics, mouseX, mouseY, partialTicks, handler.getLevel(), warning);
    }

    void drawContentsChecked(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel, boolean warning) {
        //If there are any contents render them
        if (handlerLevel > 0) {
            renderBarOverlay(guiGraphics, mouseX, mouseY, partialTicks, handlerLevel);
            if (warning && handlerLevel >= 0.98) {
                //Greater than 98% filled, render secondary piece anyway just to make it more visible
                //Note: We also start the drawing after half the dimension so that we are sure it will properly line up with
                // the one drawn to the background if the contents of things are translucent
                if (horizontal) {
                    int halfHeight = (height - 2) / 2;
                    guiGraphics.blit(WARNING_TEXTURE, relativeX + 1, relativeY + 1 + halfHeight, 0, halfHeight, width - 2, halfHeight, 256, 256);
                } else {//vertical
                    int halfWidth = (width - 2) / 2;
                    guiGraphics.blit(WARNING_TEXTURE, relativeX + 1 + halfWidth, relativeY + 1, halfWidth, 0, halfWidth, height - 2, 256, 256);
                }
            }
        }
    }

    protected abstract void renderBarOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, double handlerLevel);

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        Component tooltip = handler.getTooltip();
        if (!Objects.equals(tooltip, lastInfo)) {
            lastTooltip = TooltipUtils.create(tooltip);
        }
        lastInfo = tooltip;
        setTooltip(lastTooltip);
    }

    protected static int calculateScaled(double scale, int value) {
        if (scale == 1) {
            return value;
        } else if (scale < 1) {
            //Round down
            return (int) (scale * value);
        }//else > 1
        //Allow rounding up
        return (int) Math.round(scale * value);
    }

    public interface IBarInfoHandler {

        @Nullable
        default Component getTooltip() {
            return null;
        }

        double getLevel();
    }
}