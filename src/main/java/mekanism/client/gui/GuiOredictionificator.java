package mekanism.client.gui;

import java.util.List;
import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.GuiTransporterConfigTab;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiOredictionificator extends GuiMekanismTile<TileEntityOredictionificator, MekanismTileContainer<TileEntityOredictionificator>> {

    /**
     * The number of filters that can be displayed
     */
    private static final int FILTER_COUNT = 3;

    private Map<OredictionificatorFilter, ItemStack> renderStacks = new Object2ObjectOpenHashMap<>();
    private GuiScrollBar scrollBar;

    public GuiOredictionificator(MekanismTileContainer<TileEntityOredictionificator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 64;
        dynamicSlots = true;
    }

    @Override
    public void init() {
        super.init();
        //Filter holder
        addButton(new GuiElementHolder(this, 9, 17, 144, 68));
        //new filter button border
        addButton(new GuiElementHolder(this, 9, 85, 144, 22));
        addButton(scrollBar = new GuiScrollBar(this, 153, 17, 90, () -> getFilters().size(), () -> FILTER_COUNT));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiSideConfigurationTab(this, tile));
        addButton(new GuiTransporterConfigTab(this, tile));
        addButton(new GuiProgress(() -> tile.didProcess ? 1 : 0, ProgressType.LARGE_RIGHT, this, 64, 119));
        addButton(new TranslationButton(this, getGuiLeft() + 10, getGuiTop() + 86, 142, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.OREDICTIONIFICATOR_FILTER, tile, -1))));
        //Add each of the buttons and then just change visibility state to match filter info
        for (int i = 0; i < FILTER_COUNT; i++) {
            addButton(new FilterButton(this, 10, 18 + i * 22, 142, 22, i, scrollBar::getCurrentSelection, this::getFilters, this::onClick));
        }
    }

    protected HashList<OredictionificatorFilter> getFilters() {
        return tile.getFilters();
    }

    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof OredictionificatorFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.OREDICTIONIFICATOR_FILTER, tile, index));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        HashList<OredictionificatorFilter> filters = getFilters();
        for (int i = 0; i < FILTER_COUNT; i++) {
            OredictionificatorFilter filter = filters.getOrNull(scrollBar.getCurrentSelection() + i);
            if (filter != null) {
                if (!renderStacks.containsKey(filter)) {
                    updateRenderStacks();
                }
                int yStart = i * 22 + 18;
                renderItem(renderStacks.get(filter), 13, yStart + 3);
                drawString(MekanismLang.FILTER.translate(), 32, yStart + 2, 0x404040);
                renderScaledText(filter.getFilterText(), 32, yStart + 2 + 9, 0x404040, 117);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return scrollBar.adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void updateRenderStacks() {
        renderStacks.clear();
        for (OredictionificatorFilter filter : getFilters()) {
            if (!filter.hasFilter()) {
                renderStacks.put(filter, ItemStack.EMPTY);
                continue;
            }
            List<Item> matchingItems = filter.getMatchingItems();
            if (matchingItems.isEmpty()) {
                renderStacks.put(filter, ItemStack.EMPTY);
                continue;
            }
            if (matchingItems.size() - 1 >= filter.index) {
                renderStacks.put(filter, new ItemStack(matchingItems.get(filter.index)));
            } else {
                renderStacks.put(filter, ItemStack.EMPTY);
            }
        }
    }
}