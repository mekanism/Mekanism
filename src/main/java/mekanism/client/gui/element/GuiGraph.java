package mekanism.client.gui.element;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.text.ITextComponent;

public class GuiGraph extends GuiTexturedElement {

    private static int textureWidth = 2;
    private static int textureHeight = 10;

    private final GuiInnerScreen innerScreen;
    private final LongList graphData = new LongArrayList();
    private final GraphDataHandler dataHandler;

    private long currentScale = 10;
    private boolean fixedScale = false;

    public GuiGraph(IGuiWrapper gui, int x, int y, int width, int height, GraphDataHandler handler) {
        super(MekanismUtils.getResource(ResourceType.GUI, "graph.png"), gui, x, y, width, height);
        innerScreen = new GuiInnerScreen(gui, x - 1, y - 1, width + 2, height + 2);
        dataHandler = handler;
    }

    public void enableFixedScale(long scale) {
        fixedScale = true;
        currentScale = scale;
    }

    public void addData(long data) {
        if (graphData.size() == width) {
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
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        //Draw Black and border
        innerScreen.renderButton(mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(getResource());
        //Draw the graph
        int size = graphData.size();
        for (int i = 0; i < size; i++) {
            long data = Math.min(currentScale, graphData.getLong(i));
            int relativeHeight = (int) (data * height / (double) currentScale);
            blit(x + i, y + height - relativeHeight, 0, 0, 1, 1, textureWidth, textureHeight);

            int relativeModulo = (relativeHeight - 1) % 10;
            int displays = (relativeHeight - 1) / 10 + (relativeModulo > 0 ? 1 : 0);

            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.color4f(1, 1, 1, 0.2F + 0.8F * i / size);
            for (int iter = 0; iter < displays; iter++) {
                int heightComponent = relativeModulo > 0 && iter == displays - 1 ? relativeModulo : 10;
                blit(x + i, y + height - heightComponent - 10 * iter, 11, 0, 1, heightComponent, textureWidth, textureHeight);
            }
            MekanismRenderer.resetColor();
            RenderSystem.disableBlend();
            RenderSystem.enableAlphaTest();
        }
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        //TODO: Check
        int heightCalculated = height - (mouseY - guiObj.getTop() - y);
        long scaled = (long) (heightCalculated * currentScale / (double) height);
        displayTooltip(dataHandler.getDataDisplay(scaled), mouseX, mouseY);
    }

    public interface GraphDataHandler {

        ITextComponent getDataDisplay(long data);
    }
}