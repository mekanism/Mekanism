package mekanism.client.gui.element.graph;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import mekanism.api.math.MathUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.graph.GuiDoubleGraph.DoubleGraphDataHandler;
import net.minecraft.network.chat.Component;

public class GuiDoubleGraph extends GuiGraph<DoubleList, DoubleGraphDataHandler> {

    private double currentScale = 10;

    public GuiDoubleGraph(IGuiWrapper gui, int x, int y, int width, int height, DoubleGraphDataHandler handler) {
        super(gui, x, y, width, height, new DoubleArrayList(), handler);
    }

    public void enableFixedScale(double scale) {
        fixedScale = true;
        currentScale = scale;
    }

    public void setMinScale(double minScale) {
        currentScale = minScale;
    }

    public void addData(double data) {
        if (graphData.size() == width - 2) {
            graphData.removeDouble(0);
        }
        graphData.add(data);
        if (!fixedScale) {
            for (double i : graphData) {
                if (i > currentScale) {
                    currentScale = i;
                }
            }
        }
    }

    @Override
    protected int getRelativeHeight(int index, int height) {
        double data = Math.min(currentScale, graphData.getDouble(index));
        return MathUtils.clampToInt(data * height / currentScale);
    }

    @Override
    protected Component getDataDisplay(int hoverIndex) {
        return dataHandler.getDataDisplay(graphData.getDouble(hoverIndex));
    }

    @Override
    public boolean hasPersistentData() {
        return true;
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        for (double data : ((GuiDoubleGraph) element).graphData) {
            addData(data);
        }
    }

    public interface DoubleGraphDataHandler extends GraphDataHandler {

        Component getDataDisplay(double data);
    }
}