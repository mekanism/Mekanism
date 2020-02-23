package mekanism.client.gui;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiScrollBar;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.sound.SoundHandler;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

public class GuiOredictionificator extends GuiMekanismTile<TileEntityOredictionificator, MekanismTileContainer<TileEntityOredictionificator>> {

    /**
     * The number of filters that can be displayed
     */
    private static final int FILTER_COUNT = 3;

    private Map<OredictionificatorFilter, ItemStack> renderStacks = new Object2ObjectOpenHashMap<>();
    private double scroll;

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
        //Scroll bar
        addButton(new GuiScrollBar(this, 153, 17, 90, () -> tile.getFilters().size() > FILTER_COUNT, () -> scroll, value -> scroll = value));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiProgress(() -> tile.didProcess ? 1 : 0, ProgressType.LARGE_RIGHT, this, 64, 119));
        addButton(new TranslationButton(this, getGuiLeft() + 10, getGuiTop() + 86, 142, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.OREDICTIONIFICATOR_FILTER, tile.getPos(), 0))));
    }

    private boolean overFilter(double xAxis, double yAxis, int yStart) {
        return xAxis > 10 && xAxis <= 152 && yAxis > yStart && yAxis <= yStart + 22;
    }

    private int getFilterIndex() {
        int size = tile.getFilters().size();
        return size <= FILTER_COUNT ? 0 : (int) (size * scroll - (3F / (float) size) * scroll);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 96) + 2, 0x404040);
        HashList<OredictionificatorFilter> filters = tile.getFilters();
        for (int i = 0; i < 3; i++) {
            if (filters.get(getFilterIndex() + i) != null) {
                OredictionificatorFilter filter = filters.get(getFilterIndex() + i);
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
    protected void drawGuiContainerBackgroundLayer(int xAxis, int yAxis) {
        super.drawGuiContainerBackgroundLayer(xAxis, yAxis);
        HashList<OredictionificatorFilter> filters = tile.getFilters();
        for (int i = 0; i < FILTER_COUNT; i++) {
            if (filters.get(getFilterIndex() + i) != null) {
                int yStart = i * 22 + 18;
                boolean mouseOver = overFilter(xAxis, yAxis, yStart);
                if (mouseOver) {
                    MekanismRenderer.color(EnumColor.GRAY);
                }
                drawTexturedRect(getGuiLeft() + 10, getGuiTop() + yStart, 0, 230, 142, 22);
                if (mouseOver) {
                    MekanismRenderer.resetColor();
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            double xAxis = mouseX - getGuiLeft();
            double yAxis = mouseY - getGuiTop();
            HashList<OredictionificatorFilter> filters = tile.getFilters();
            for (int i = 0; i < FILTER_COUNT; i++) {
                if (filters.get(getFilterIndex() + i) != null && overFilter(xAxis, yAxis, i * 22 + 18)) {
                    Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.OREDICTIONIFICATOR_FILTER, tile.getPos(), getFilterIndex() + i));
                    SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
                }
            }
        }
        return true;
    }

    @Override
    protected ResourceLocation getGuiLocation() {
        return MekanismUtils.getResource(ResourceType.GUI, "oredictionificator.png");
    }

    private void updateRenderStacks() {
        renderStacks.clear();
        for (OredictionificatorFilter filter : tile.getFilters()) {
            if (filter.hasFilter()) {
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