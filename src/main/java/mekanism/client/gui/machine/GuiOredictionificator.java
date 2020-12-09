package mekanism.client.gui.machine;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.client.gui.GuiConfigurableTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.window.filter.GuiOredictionificatorFilter;
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
        playerInventoryTitleY = ySize - 94;
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
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new GuiProgress(() -> tile.didProcess, ProgressType.LARGE_RIGHT, this, 64, 119));
        addButton(new TranslationButton(this, guiLeft + 10, guiTop + 86, 142, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> addWindow(GuiOredictionificatorFilter.create(this, tile))));
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
            addWindow(GuiOredictionificatorFilter.edit(this, tile, (OredictionificatorFilter) filter));
        }
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        HashList<OredictionificatorFilter> filters = getFilters();
        for (int i = 0; i < FILTER_COUNT; i++) {
            OredictionificatorFilter filter = filters.getOrNull(scrollBar.getCurrentSelection() + i);
            if (filter != null) {
                if (!renderStacks.containsKey(filter)) {
                    updateRenderStacks();
                }
                int yStart = i * 22 + 18;
                renderItem(matrix, renderStacks.get(filter), 13, yStart + 3);
                drawString(matrix, MekanismLang.FILTER.translate(), 32, yStart + 2, titleTextColor());
                drawTextScaledBound(matrix, filter.getFilterText(), 32, yStart + 2 + 9, titleTextColor(), 117);
            }
        }
        super.drawForegroundText(matrix, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return scrollBar.adjustScroll(delta) || super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void updateRenderStacks() {
        renderStacks.clear();
        for (OredictionificatorFilter filter : getFilters()) {
            renderStacks.put(filter, filter.getResult());
        }
    }
}