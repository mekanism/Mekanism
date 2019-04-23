package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiGraph extends GuiElement {

    private final List<Integer> graphData = new ArrayList<>();
    private final GraphDataHandler dataHandler;
    private final int xPosition;
    private final int yPosition;
    private final int xSize;
    private final int ySize;

    private int currentScale = 10;
    private boolean fixedScale = false;

    public GuiGraph(IGuiWrapper gui, ResourceLocation def, int x, int y, int sizeX, int sizeY,
          GraphDataHandler handler) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "GuiGraph.png"), gui, def);
        xPosition = x;
        yPosition = y;
        xSize = sizeX;
        ySize = sizeY;
        dataHandler = handler;
    }

    public void enableFixedScale(int scale) {
        fixedScale = true;
        currentScale = scale;
    }

    public void addData(int data) {
        if (graphData.size() == xSize) {
            graphData.remove(0);
        }

        graphData.add(data);

        if (!fixedScale) {
            for (int i : graphData) {
                if (i > currentScale) {
                    currentScale = i;
                }
            }
        }
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth + xPosition, guiHeight + yPosition, xSize, ySize);
    }

    @Override
    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= xPosition && xAxis <= xPosition + xSize && yAxis >= yPosition && yAxis <= yPosition + ySize;
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        mc.renderEngine.bindTexture(RESOURCE);
        drawBlack(guiWidth, guiHeight);
        drawGraph(guiWidth, guiHeight);
        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        if (inBounds(xAxis, yAxis)) {
            int height = ySize - (yAxis - yPosition);
            int scaled = (int) (((double) height / (double) ySize) * currentScale);
            displayTooltip(dataHandler.getDataDisplay(scaled), xAxis, yAxis);
        }
    }

    @Override
    public void preMouseClicked(int xAxis, int yAxis, int button) {
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
    }

    public void drawBlack(int guiWidth, int guiHeight) {
        int xDisplays = xSize / 10 + (xSize % 10 > 0 ? 1 : 0);
        int yDisplays = ySize / 10 + (ySize % 10 > 0 ? 1 : 0);

        for (int yIter = 0; yIter < yDisplays; yIter++) {
            for (int xIter = 0; xIter < xDisplays; xIter++) {
                int width = (xSize % 10 > 0 && xIter == xDisplays - 1 ? xSize % 10 : 10);
                int height = (ySize % 10 > 0 && yIter == yDisplays - 1 ? ySize % 10 : 10);
                guiObj.drawTexturedRect(guiWidth + xPosition + (xIter * 10), guiHeight + yPosition + (yIter * 10), 0, 0,
                      width, height);
            }
        }
    }

    public void drawGraph(int guiWidth, int guiHeight) {
        for (int i = 0; i < graphData.size(); i++) {
            int data = Math.min(currentScale, graphData.get(i));
            int relativeHeight = (int) (((double) data / (double) currentScale) * ySize);
            guiObj.drawTexturedRect(guiWidth + xPosition + i, guiHeight + yPosition + (ySize - relativeHeight), 10, 0,
                  1, 1);

            int displays = (relativeHeight - 1) / 10 + ((relativeHeight - 1) % 10 > 0 ? 1 : 0);

            for (int iter = 0; iter < displays; iter++) {
                MekanismRenderer.blendOn();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.2F + (0.8F * ((float) i / (float) graphData.size())));
                int height = ((relativeHeight - 1) % 10 > 0 && iter == displays - 1 ? (relativeHeight - 1) % 10 : 10);
                guiObj.drawTexturedRect(guiWidth + xPosition + i,
                      guiHeight + yPosition + (ySize - (iter * 10)) - 10 + (10 - height), 11, 0, 1, height);
                MekanismRenderer.blendOff();
            }
        }
    }

    public interface GraphDataHandler {

        String getDataDisplay(int data);
    }
}