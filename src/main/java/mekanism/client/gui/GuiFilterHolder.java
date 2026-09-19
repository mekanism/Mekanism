package mekanism.client.gui;

import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.client.gui.element.button.MovableFilterButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jspecify.annotations.Nullable;

public abstract class GuiFilterHolder<FILTER extends IFilter<?>, TILE extends TileEntityMekanism & ITileFilterHolder<FILTER>, CONTAINER extends MekanismTileContainer<TILE>>
      extends GuiMekanismTile<TILE, CONTAINER> {

    /// The number of filters that can be displayed
    private static final int FILTER_COUNT = 4;
    @Nullable
    private GuiInnerScreen leftScreen;
    @Nullable
    private GuiScrollBar scrollBar;

    protected GuiFilterHolder(CONTAINER container, Inventory inv, Component title) {
        super(container, inv, title, DEFAULT_IMAGE_WIDTH + 100, DEFAULT_IMAGE_HEIGHT + 88);
        inventoryLabelX += 50;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        leftScreen = addRenderableWidget(new GuiInnerScreen(this, 9, 17, 85, 140));
        //Filter holder
        addRenderableWidget(new GuiElementHolder(this, 95, 17, 158, 118));
        //new filter button border
        addRenderableWidget(new GuiElementHolder(this, 95, 135, 158, 22));
        FilterManager<FILTER> filterManager = getFilterManager();
        scrollBar = addRenderableWidget(new GuiScrollBar(this, 253, 17, 140, filterManager::count, () -> FILTER_COUNT));
        //Add each of the buttons and then just change visibility state to match filter info
        for (int i = 0; i < FILTER_COUNT; i++) {
            addFilterButton(new MovableFilterButton(this, 96, 18 + i * 29, i, scrollBar::getCurrentSelection, filterManager, (event, index) -> {
                if (index > 0) {
                    GuiInteraction interaction = event.hasShiftDown() ? GuiInteraction.MOVE_FILTER_TO_TOP : GuiInteraction.MOVE_FILTER_UP;
                    PacketUtils.sendToServer(new PacketGuiInteract(interaction, tile, index));
                }
            },  (event, index) -> {
                if (index < filterManager.count() - 1) {
                    GuiInteraction interaction = event.hasShiftDown() ? GuiInteraction.MOVE_FILTER_TO_BOTTOM : GuiInteraction.MOVE_FILTER_DOWN;
                    PacketUtils.sendToServer(new PacketGuiInteract(interaction, tile, index));
                }
            }, this::onClick, index -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.TOGGLE_FILTER_STATE, tile, index))));
        }
    }

    protected void drawScreenText(GuiGraphicsExtractor guiGraphics, Component text, int x, int y) {
        //TODO: Do we want to make usages of this method eventually set the text to be rendered within the gui element for the screen?
        if (leftScreen != null) {//Validate it was properly set
            leftScreen.drawScaledScrollingString(guiGraphics, text, x, y, TextAlignment.LEFT, screenTextColor(), leftScreen.getImageWidth() - x, 5, false, 0.8F);
        }
    }

    protected FilterButton addFilterButton(FilterButton button) {
        return addRenderableWidget(button);
    }

    protected FilterManager<FILTER> getFilterManager() {
        return tile.getFilterManager();
    }

    protected abstract void onClick(@Nullable IFilter<?> filter, int index);

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return super.mouseScrolled(mouseX, mouseY, xDelta, yDelta) || scrollBar != null && scrollBar.adjustScroll(yDelta);
    }

    @Override
    protected void drawForegroundText(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
        renderInventoryText(guiGraphics);
    }
}