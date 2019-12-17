package mekanism.client.gui.element;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

public class GuiGraph extends GuiTexturedElement {

    private final List<Integer> graphData = new ArrayList<>();
    private final GraphDataHandler dataHandler;

    private int currentScale = 10;
    private boolean fixedScale = false;

    //TODO: Convert
    public GuiGraph(IGuiWrapper gui, ResourceLocation def, int x, int y, int xSize, int ySize, GraphDataHandler handler) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "graph.png"), gui, def, x, y, xSize, ySize);
        dataHandler = handler;
    }

    public void enableFixedScale(int scale) {
        fixedScale = true;
        currentScale = scale;
    }

    public void addData(int data) {
        if (graphData.size() == width) {
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
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        drawBlack();
        drawGraph();
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        //TODO: Check
        int heightCalculated = height - (mouseY - guiObj.getTop() - y);
        int scaled = (int) (((double) heightCalculated / (double) height) * currentScale);
        displayTooltip(dataHandler.getDataDisplay(scaled), mouseX, mouseY);
    }

    public void drawBlack() {
        int xDisplays = width / 10 + (width % 10 > 0 ? 1 : 0);
        int yDisplays = height / 10 + (height % 10 > 0 ? 1 : 0);

        for (int yIter = 0; yIter < yDisplays; yIter++) {
            for (int xIter = 0; xIter < xDisplays; xIter++) {
                int widthCalculated = width % 10 > 0 && xIter == xDisplays - 1 ? width % 10 : 10;
                int heightCalculated = height % 10 > 0 && yIter == yDisplays - 1 ? height % 10 : 10;
                guiObj.drawModalRectWithCustomSizedTexture(x + (xIter * 10), y + (yIter * 10), 0, 0, widthCalculated, heightCalculated, 12, 10);
            }
        }
    }

    public void drawGraph() {
        for (int i = 0; i < graphData.size(); i++) {
            int data = Math.min(currentScale, graphData.get(i));
            int relativeHeight = (int) (((double) data / (double) currentScale) * height);
            guiObj.drawModalRectWithCustomSizedTexture(x + i, y + (height - relativeHeight), 10, 0, 1, 1, 12, 10);

            int displays = (relativeHeight - 1) / 10 + ((relativeHeight - 1) % 10 > 0 ? 1 : 0);

            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            for (int iter = 0; iter < displays; iter++) {
                RenderSystem.color4f(1, 1, 1, 0.2F + (0.8F * ((float) i / (float) graphData.size())));
                int height = (relativeHeight - 1) % 10 > 0 && iter == displays - 1 ? (relativeHeight - 1) % 10 : 10;
                guiObj.drawModalRectWithCustomSizedTexture(x + i, y + (height - (iter * 10)) - 10 + (10 - height), 11, 0, 1, height, 12, 10);
            }
            MekanismRenderer.resetColor();
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
        }
    }

    public interface GraphDataHandler {

        ITextComponent getDataDisplay(int data);
    }
}