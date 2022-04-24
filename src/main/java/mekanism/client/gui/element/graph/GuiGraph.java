package mekanism.client.gui.element.graph;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.element.graph.GuiGraph.GraphDataHandler;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.network.chat.Component;

public abstract class GuiGraph<COLLECTION extends Collection<?>, HANDLER extends GraphDataHandler> extends GuiTexturedElement {

    private static final int TEXTURE_WIDTH = 3;
    private static final int TEXTURE_HEIGHT = 2;

    protected final COLLECTION graphData;
    protected final HANDLER dataHandler;

    protected boolean fixedScale = false;

    protected GuiGraph(IGuiWrapper gui, int x, int y, int width, int height, COLLECTION graphData, HANDLER handler) {
        super(MekanismUtils.getResource(ResourceType.GUI, "graph.png"), gui, x, y, width, height);
        this.graphData = graphData;
        this.dataHandler = handler;
    }

    @Override
    public void drawBackground(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        //Draw Black and border
        renderBackgroundTexture(matrix, GuiInnerScreen.SCREEN, GuiInnerScreen.SCREEN_SIZE, GuiInnerScreen.SCREEN_SIZE);
        RenderSystem.setShaderTexture(0, getResource());
        //Draw the graph
        int size = graphData.size();
        int x = this.x + 1;
        int y = this.y + 1;
        int height = this.height - 2;
        for (int i = 0; i < size; i++) {
            int relativeHeight = getRelativeHeight(i, height);
            blit(matrix, x + i, y + height - relativeHeight, 0, 0, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

            RenderSystem.setShaderColor(1, 1, 1, 0.2F + 0.8F * i / size);
            blit(matrix, x + i, y + height - relativeHeight, 1, 0, 1, relativeHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            int hoverIndex = mouseX - getButtonX();
            if (hoverIndex == i && mouseY >= getButtonY() && mouseY < getButtonY() + height) {
                RenderSystem.setShaderColor(1, 1, 1, 0.5F);
                blit(matrix, x + i, y, 2, 0, 1, height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                MekanismRenderer.resetColor();
                blit(matrix, x + i, y + height - relativeHeight, 0, 1, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }

            MekanismRenderer.resetColor();
            RenderSystem.disableBlend();
        }
    }

    protected abstract int getRelativeHeight(int index, int height);

    protected abstract Component getDataDisplay(int hoverIndex);

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        int hoverIndex = mouseX - x;
        if (hoverIndex >= 0 && hoverIndex < graphData.size()) {
            displayTooltips(matrix, mouseX, mouseY, getDataDisplay(hoverIndex));
        }
    }

    public interface GraphDataHandler {
    }
}