package mekanism.client.gui.qio;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.MovableFilterButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.client.gui.element.window.filter.qio.GuiQIOItemStackFilter;
import mekanism.client.gui.element.window.filter.qio.GuiQIOTagFilter;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import mekanism.common.util.text.TextUtils;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiQIOFilterHandler<TILE extends TileEntityQIOFilterHandler> extends GuiMekanismTile<TILE, MekanismTileContainer<TILE>> {

    private static final int FILTER_COUNT = 3;

    private GuiScrollBar scrollBar;

    public GuiQIOFilterHandler(MekanismTileContainer<TILE> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        ySize += 74;
        playerInventoryTitleY = ySize - 94;
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiInnerScreen(this, 9, 16, xSize - 18, 12, () -> {
            List<ITextComponent> list = new ArrayList<>();
            QIOFrequency freq = tile.getQIOFrequency();
            if (freq != null) {
                list.add(MekanismLang.FREQUENCY.translate(freq.getKey()));
            } else {
                list.add(MekanismLang.NO_FREQUENCY.translate());
            }
            return list;
        }).tooltip(() -> {
            List<ITextComponent> list = new ArrayList<>();
            QIOFrequency freq = tile.getQIOFrequency();
            if (freq != null) {
                list.add(MekanismLang.QIO_ITEMS_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      TextUtils.format(freq.getTotalItemCount()), TextUtils.format(freq.getTotalItemCountCapacity())));
                list.add(MekanismLang.QIO_TYPES_DETAIL.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
                      TextUtils.format(freq.getTotalItemTypes(true)), TextUtils.format(freq.getTotalItemTypeCapacity())));
            }
            return list;
        }));
        //Filter holder
        addButton(new GuiElementHolder(this, 9, 30, 144, 68));
        //new filter button border
        addButton(new GuiElementHolder(this, 9, 98, 144, 22));
        addButton(scrollBar = new GuiScrollBar(this, 153, 30, 90, () -> tile.getFilters().size(), () -> FILTER_COUNT));
        //Add each of the buttons and then just change visibility state to match filter info
        for (int i = 0; i < FILTER_COUNT; i++) {
            addButton(new MovableFilterButton(this, 10, 31 + i * 22, 142, 22, i, scrollBar::getCurrentSelection, tile::getFilters, index -> {
                if (index > 0) {
                    Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.MOVE_FILTER_UP, tile, index));
                }
            }, index -> {
                if (index < tile.getFilters().size() - 1) {
                    Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.MOVE_FILTER_DOWN, tile, index));
                }
            }, this::onClick, filter -> {
                List<ItemStack> list = new ArrayList<>();
                if (filter != null) {
                    if (filter instanceof IItemStackFilter) {
                        list.add(((IItemStackFilter<?>) filter).getItemStack());
                    } else if (filter instanceof ITagFilter) {
                        String name = ((ITagFilter<?>) filter).getTagName();
                        if (name != null && !name.isEmpty()) {
                            list.addAll(TagCache.getItemTagStacks(((ITagFilter<?>) filter).getTagName()));
                        }
                    }
                }
                return list;
            }));
        }
        addButton(new TranslationButton(this, guiLeft + 10, guiTop + 99, 71, 20, MekanismLang.BUTTON_ITEMSTACK_FILTER,
              () -> addWindow(GuiQIOItemStackFilter.create(this, tile))));
        addButton(new TranslationButton(this, guiLeft + 81, guiTop + 99, 71, 20, MekanismLang.BUTTON_TAG_FILTER,
              () -> addWindow(GuiQIOTagFilter.create(this, tile))));
    }

    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof IItemStackFilter) {
            addWindow(GuiQIOItemStackFilter.edit(this, tile, (QIOItemStackFilter) filter));
        } else if (filter instanceof ITagFilter) {
            addWindow(GuiQIOTagFilter.edit(this, tile, (QIOTagFilter) filter));
        }
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        renderTitleText(matrix);
        drawString(matrix, playerInventory.getDisplayName(), playerInventoryTitleX, playerInventoryTitleY, titleTextColor());
        super.drawForegroundText(matrix, mouseX, mouseY);
    }
}