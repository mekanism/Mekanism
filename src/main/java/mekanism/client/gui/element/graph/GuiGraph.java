package mekanism.client.gui.element.graph;

import java.util.Collection;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.graph.GuiGraph.GraphDataHandler;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

public abstract class GuiGraph<COLLECTION extends Collection<?>, HANDLER extends GraphDataHandler> extends GuiElement {

    private static final Identifier TEXTURE = MekanismUtils.getResource(ResourceType.GUI, "graph.png");
    private static final int TEXTURE_WIDTH = 3;
    private static final int TEXTURE_HEIGHT = 2;

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
        super(gui, x, y, width, height);
        this.graphData = graphData;
        this.dataHandler = handler;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        //Draw Black and border
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, GuiInnerScreen.SCREEN, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
        //Draw the graph
        int size = graphData.size();
        int x = relativeX + 1;
        int y = relativeY + 1;
        int height = this.height - 2;
        for (int i = 0; i < size; i++) {
            int relativeHeight = getRelativeHeight(i, height);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + i, y + height - relativeHeight, 0, 0, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            //TODO - 26.2: rendering
            //RenderSystem.enableBlend();
            //RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + i, y + height - relativeHeight, 1, 0, 1, relativeHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT, ARGB.color(0.2F + 0.8F * i / size, CommonColors.WHITE));

            int hoverIndex = mouseX - getX();
            if (hoverIndex == i && mouseY >= getY() && mouseY < getY() + height) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + i, y, 2, 0, 1, height, TEXTURE_WIDTH, TEXTURE_HEIGHT, ARGB.color(0.5F, CommonColors.WHITE));
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + i, y + height - relativeHeight, 0, 1, 1, 1, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }

            //RenderSystem.disableBlend();
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