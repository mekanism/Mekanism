package mekanism.client.gui.machine;

import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.filter.GuiOredictionificatorFilter;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.HashList;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.tile.machine.TileEntityOredictionificator.OredictionificatorFilter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiOredictionificator extends GuiConfigurableTile<TileEntityOredictionificator, MekanismTileContainer<TileEntityOredictionificator>> {

    /**
     * The number of filters that can be displayed
     */
    private static final int FILTER_COUNT = 3;

    private final Map<OredictionificatorFilter, ItemStack> renderStacks = new Object2ObjectOpenHashMap<>();
    private GuiScrollBar scrollBar;

    public GuiOredictionificator(MekanismTileContainer<TileEntityOredictionificator> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        ySize += 64;
        dynamicSlots = true;
    }

    @Override
    public void func_231160_c_() {
        super.func_231160_c_();
        //Filter holder
        func_230480_a_(new GuiElementHolder(this, 9, 17, 144, 68));
        //new filter button border
        func_230480_a_(new GuiElementHolder(this, 9, 85, 144, 22));
        func_230480_a_(scrollBar = new GuiScrollBar(this, 153, 17, 90, () -> getFilters().size(), () -> FILTER_COUNT));
        func_230480_a_(new GuiRedstoneControlTab(this, tile));
        func_230480_a_(new GuiSecurityTab<>(this, tile));
        func_230480_a_(new GuiProgress(() -> tile.didProcess ? 1 : 0, ProgressType.LARGE_RIGHT, this, 64, 119));
        func_230480_a_(new TranslationButton(this, getGuiLeft() + 10, getGuiTop() + 86, 142, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> addWindow(GuiOredictionificatorFilter.create(this, tile))));
        //Add each of the buttons and then just change visibility state to match filter info
        for (int i = 0; i < FILTER_COUNT; i++) {
            func_230480_a_(new FilterButton(this, 10, 18 + i * 22, 142, 22, i, scrollBar::getCurrentSelection, this::getFilters, this::onClick));
        }
    }

    protected HashList<OredictionificatorFilter> getFilters() {
        return tile.getFilters();
    }

    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof OredictionificatorFilter) {
            addWindow(GuiOredictionificatorFilter.edit(this, tile, (OredictionificatorFilter) filter));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        renderTitleText();
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, titleTextColor());
        HashList<OredictionificatorFilter> filters = getFilters();
        for (int i = 0; i < FILTER_COUNT; i++) {
            OredictionificatorFilter filter = filters.getOrNull(scrollBar.getCurrentSelection() + i);
            if (filter != null) {
                if (!renderStacks.containsKey(filter)) {
                    updateRenderStacks();
                }
                int yStart = i * 22 + 18;
                renderItem(renderStacks.get(filter), 13, yStart + 3);
                drawString(MekanismLang.FILTER.translate(), 32, yStart + 2, titleTextColor());
                drawTextScaledBound(filter.getFilterText(), 32, yStart + 2 + 9, titleTextColor(), 117);
            }
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean func_231043_a_(double mouseX, double mouseY, double delta) {
        return scrollBar.adjustScroll(delta) || super.func_231043_a_(mouseX, mouseY, delta);
    }

    private void updateRenderStacks() {
        renderStacks.clear();
        for (OredictionificatorFilter filter : getFilters()) {
            renderStacks.put(filter, filter.getResult());
        }
    }
}