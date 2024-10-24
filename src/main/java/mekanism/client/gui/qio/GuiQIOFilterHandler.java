package mekanism.client.gui.qio;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MovableFilterButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.window.filter.qio.GuiQIOItemStackFilter;
import mekanism.client.gui.element.window.filter.qio.GuiQIOModIDFilter;
import mekanism.client.gui.element.window.filter.qio.GuiQIOTagFilter;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class GuiQIOFilterHandler<TILE extends TileEntityQIOFilterHandler> extends GuiMekanismTile<TILE, MekanismTileContainer<TILE>> {

    private static final int FILTER_COUNT = 3;

    static Supplier<List<Component>> getFrequencyText(IQIOFrequencyHolder holder) {
        return () -> {
            QIOFrequency freq = holder.getQIOFrequency();
            if (freq == null) {
                return List.of(MekanismLang.NO_FREQUENCY.translate());
            }
            return List.of(MekanismLang.FREQUENCY.translate(freq.getKey()));
        };
    }

    static Supplier<List<Component>> getFrequencyTooltip(IQIOFrequencyHolder holder) {
        return () -> {
            QIOFrequency freq = holder.getQIOFrequency();
            if (freq == null) {
                return Collections.emptyList();
            }
            return List.of(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextUtils.format(freq.getTotalItemCount()),
                        TextUtils.format(freq.getTotalItemCountCapacity())),
                  MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO, TextUtils.format(freq.getTotalItemTypes(true)),
                        TextUtils.format(freq.getTotalItemTypeCapacity()))
            );
        };
    }

    private GuiScrollBar scrollBar;

    public GuiQIOFilterHandler(MekanismTileContainer<TILE> container, Inventory inv, Component title) {
        super(container, inv, title);
        dynamicSlots = true;
        imageHeight += 74;
        inventoryLabelY = imageHeight - 94;
        imageWidth += 60;
        inventoryLabelX += 30;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiQIOFrequencyTab(this, tile));
        addRenderableWidget(new GuiInnerScreen(this, 9, 16, imageWidth - 18, 12, getFrequencyText(tile)).tooltip(getFrequencyTooltip(tile)));
        //Filter holder
        addRenderableWidget(new GuiElementHolder(this, 9, 30, 204, 68));
        //new filter button border
        addRenderableWidget(new GuiElementHolder(this, 9, 98, 204, 22));
        addRenderableWidget(new TranslationButton(this, 10, 99, 202, 20, MekanismLang.BUTTON_NEW_FILTER, (element, mouseX, mouseY) -> {
            GuiQIOFilterHandler<?> gui = (GuiQIOFilterHandler<?>) element.gui();
            gui.addWindow(new GuiQIOFilerSelect(gui, gui.tile));
            return true;
        }));
        SortableFilterManager<QIOFilter<?>> filterManager = tile.getFilterManager();
        scrollBar = addRenderableWidget(new GuiScrollBar(this, 213, 30, 90, filterManager::count, () -> FILTER_COUNT));
        //Add each of the buttons and then just change visibility state to match filter info
        for (int i = 0; i < FILTER_COUNT; i++) {
            addRenderableWidget(new MovableFilterButton(this, 10, 31 + i * 22, 202, 22, i, scrollBar::getCurrentSelection, filterManager, index -> {
                if (index > 0) {
                    GuiInteraction interaction = hasShiftDown() ? GuiInteraction.MOVE_FILTER_TO_TOP : GuiInteraction.MOVE_FILTER_UP;
                    PacketUtils.sendToServer(new PacketGuiInteract(interaction, tile, index));
                }
            }, index -> {
                if (index < filterManager.count() - 1) {
                    GuiInteraction interaction = hasShiftDown() ? GuiInteraction.MOVE_FILTER_TO_BOTTOM : GuiInteraction.MOVE_FILTER_DOWN;
                    PacketUtils.sendToServer(new PacketGuiInteract(interaction, tile, index));
                }
            }, this::onClick, index -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.TOGGLE_FILTER_STATE, tile, index)), filter -> {
                if (filter != null) {
                    return switch (filter) {
                        case IItemStackFilter<?> itemFilter -> List.of(itemFilter.getItemStack());
                        case ITagFilter<?> tagFilter -> {
                            String name = tagFilter.getTagName();
                            if (name != null && !name.isEmpty()) {
                                yield TagCache.getItemTagStacks(tagFilter.getTagName());
                            }
                            yield Collections.emptyList();
                        }
                        case IModIDFilter<?> modIDFilter -> TagCache.getItemModIDStacks(modIDFilter.getModID());
                        default -> Collections.emptyList();
                    };
                }
                return Collections.emptyList();
            }));
        }
    }

    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof IItemStackFilter) {
            addWindow(GuiQIOItemStackFilter.edit(this, tile, (QIOItemStackFilter) filter));
        } else if (filter instanceof ITagFilter) {
            addWindow(GuiQIOTagFilter.edit(this, tile, (QIOTagFilter) filter));
        } else if (filter instanceof IModIDFilter) {
            addWindow(GuiQIOModIDFilter.edit(this, tile, (QIOModIDFilter) filter));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xDelta, double yDelta) {
        return super.mouseScrolled(mouseX, mouseY, xDelta, yDelta) || scrollBar.adjustScroll(yDelta);
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        renderTitleText(guiGraphics);
        renderInventoryText(guiGraphics);
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
    }
}