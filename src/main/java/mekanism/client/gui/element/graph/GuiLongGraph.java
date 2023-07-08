package mekanism.client.gui.element.graph;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.graph.GuiLongGraph.LongGraphDataHandler;
import net.minecraft.network.chat.Component;

public class GuiLongGraph extends GuiGraph<LongList, LongGraphDataHandler> {

    private long currentScale = 10;

    public GuiLongGraph(IGuiWrapper gui, int x, int y, int width, int height, LongGraphDataHandler handler) {
        super(gui, x, y, width, height, new LongArrayList(), handler);
    }

    public void enableFixedScale(long scale) {
        fixedScale = true;
        currentScale = scale;
    }

    public void setMinScale(long minScale) {
        currentScale = minScale;
    }

    public void addData(long data) {
        if (graphData.size() == width - 2) {
            graphData.removeLong(0);
        }
        graphData.add(data);
        if (!fixedScale) {
            for (long i : graphData) {
                if (i > currentScale) {
                    currentScale = i;
                }
            }
        }
    }

    @Override
    protected int getRelativeHeight(int index, int height) {
        long data = Math.min(currentScale, graphData.getLong(index));
        return MathUtils.clampToInt(data * height / (double) currentScale);
    }

    @Override
    protected Component getDataDisplay(int hoverIndex) {
        return dataHandler.getDataDisplay(graphData.getLong(hoverIndex));
    }

    @Override
    public boolean hasPersistentData() {
        return true;
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        for (long data : ((GuiLongGraph) element).graphData) {
            addData(data);
        }
    }

    public interface LongGraphDataHandler extends GraphDataHandler {

        Component getDataDisplay(long data);
    }
}