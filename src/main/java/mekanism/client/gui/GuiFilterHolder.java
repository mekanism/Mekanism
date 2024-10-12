package mekanism.client.gui;

import java.util.Collections;
import java.util.List;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.client.gui.element.button.MovableFilterButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiFilterHolder<FILTER extends IFilter<?>, TILE extends TileEntityMekanism & ITileFilterHolder<FILTER>, CONTAINER extends MekanismTileContainer<TILE>>
      extends GuiMekanismTile<TILE, CONTAINER> {

    /**
     * The number of filters that can be displayed
     */
    private static final int FILTER_COUNT = 4;
    protected GuiInnerScreen leftScreen;
    private GuiScrollBar scrollBar;

    protected GuiFilterHolder(CONTAINER container, Inventory inv, Component title) {
        super(container, inv, title);
        imageHeight += 88;
        imageWidth += 100;
        inventoryLabelX += 50;
        inventoryLabelY = imageHeight - 94;
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
            addFilterButton(new MovableFilterButton(this, 96, 18 + i * 29, i, scrollBar::getCurrentSelection, filterManager, index -> {
                if (index > 0) {
                    GuiInteraction interaction = hasShiftDown() ? GuiInteraction.MOVE_FILTER_TO_TOP : GuiInteraction.MOVE_FILTER_UP;
                    PacketUtils.sendToServer(new PacketGuiInteract(interaction, tile, index));
                }
            }, index -> {
                if (index < filterManager.count() - 1) {
                    GuiInteraction interaction = hasShiftDown() ? GuiInteraction.MOVE_FILTER_TO_BOTTOM : GuiInteraction.MOVE_FILTER_DOWN;
                    PacketUtils.sendToServer(new PacketGuiInteract(interaction, tile, index));
                }
            }, this::onClick, index -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.TOGGLE_FILTER_STATE, tile, index)), this::getRenderStacks));
        }
    }

    protected void drawScreenText(GuiGraphics guiGraphics, Component text, int y) {
        drawScreenText(guiGraphics, text, 0, y);
    }

    protected void drawScreenText(GuiGraphics guiGraphics, Component text, int x, int y) {
        //TODO: Do we want to make usages of this method eventually set the text to be rendered within the gui element for the screen?
        if (leftScreen != null) {//Validate it was properly set
            leftScreen.drawScaledScrollingString(guiGraphics, text, x, y, TextAlignment.LEFT, screenTextColor(), leftScreen.getXSize() - x, 5, false, 0.8F);
        }
    }

    private List<ItemStack> getRenderStacks(@Nullable IFilter<?> filter) {
        if (filter != null) {
            return switch (filter) {
                case IItemStackFilter<?> itemFilter -> List.of(itemFilter.getItemStack());
                case ITagFilter<?> tagFilter -> getTagStacks(tagFilter.getTagName());
                case IModIDFilter<?> modIDFilter -> getModIDStacks(modIDFilter.getModID());
                default -> Collections.emptyList();
            };
        }
        return Collections.emptyList();
    }

    protected FilterButton addFilterButton(FilterButton button) {
        return addRenderableWidget(button);
    }

    protected FilterManager<FILTER> getFilterManager() {
        return tile.getFilterManager();
    }

    protected abstract void onClick(IFilter<?> filter, int index);

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return super.mouseScrolled(mouseX, mouseY, xDelta, yDelta) || scrollBar.adjustScroll(yDelta);
    }

    protected abstract List<ItemStack> getTagStacks(String tagName);

    protected abstract List<ItemStack> getModIDStacks(String tagName);

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
        renderInventoryText(guiGraphics);
    }
}