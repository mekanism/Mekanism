package mekanism.client.gui.element.graph;

import java.util.Collection;
import mekanism.client.SpecialColors;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.graph.GuiGraph.GraphDataHandler;
import mekanism.client.gui.tooltip.TooltipUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

public abstract class GuiGraph<COLLECTION extends Collection<?>, HANDLER extends GraphDataHandler> extends GuiTexturedElement {

    protected final COLLECTION graphData;
    protected final HANDLER dataHandler;

    @Nullable
    private Component lastInfo = null;
    @Nullable
    private Tooltip lastTooltip;
    @Nullable
    private ScreenRectangle cachedTooltipRect;

    protected boolean fixedScale = false;

    protected GuiGraph(IGuiWrapper gui, int x, int y, int width, int height, COLLECTION graphData, HANDLER handler) {
        super(GuiInnerScreen.SCREEN, gui, x, y, width, height);
        this.graphData = graphData;
        this.dataHandler = handler;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Draw Black and border
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        //Draw the graph
        int size = graphData.size();
        int x = relativeX + 1;
        int y = relativeY + 1;
        int height = this.height - 2;
        int hoverIndex = mouseX - getX();
        for (int i = 0; i < size; i++) {
            int relativeHeight = getRelativeHeight(i, height);
            int xStart = x + i;
            int yStart = y + height - relativeHeight;
            GuiUtils.fill(guiGraphics, xStart, yStart, 1, 1, SpecialColors.GRAPH_GRAPHED_VALUE.argb());
            GuiUtils.fill(guiGraphics, xStart, yStart, 1, relativeHeight, ARGB.color(0.2F + 0.8F * i / size, SpecialColors.GRAPH_INTEGRAL.argb()));

            if (hoverIndex == i && mouseY >= getY() && mouseY < getY() + height) {
                GuiUtils.fill(guiGraphics, xStart, y, 1, height, ARGB.color(0.5F, SpecialColors.GRAPH_HOVERED_COLUMN.argb()));
                GuiUtils.fill(guiGraphics, xStart, yStart, 1, 1, SpecialColors.GRAPH_HOVERED_VALUE.argb());
            }
        }
    }

    protected abstract int getRelativeHeight(int index, int height);

    protected abstract Component getDataDisplay(int hoverIndex);

    @Override
    protected ScreenRectangle getTooltipRectangle(int mouseX, int mouseY) {
        return cachedTooltipRect == null ? super.getTooltipRectangle(mouseX, mouseY) : cachedTooltipRect;
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        int hoverIndex = mouseX - getX();
        if (hoverIndex >= 0 && hoverIndex < graphData.size()) {
            Component info = getDataDisplay(hoverIndex);
            if (!info.equals(lastInfo)) {
                lastInfo = info;
                lastTooltip = TooltipUtils.create(info);
            }
            cachedTooltipRect = new ScreenRectangle(getX(), getGuiTop() + getButtonY(), 1, getButtonHeight());
        } else {
            lastInfo = null;
            lastTooltip = null;
            cachedTooltipRect = null;
        }
        setTooltip(lastTooltip);
    }

    public interface GraphDataHandler {
    }
}