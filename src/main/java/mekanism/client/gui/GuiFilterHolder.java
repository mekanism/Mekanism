package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.FilterButton;
import mekanism.client.gui.element.button.MovableFilterButton;
import mekanism.client.gui.element.scroll.GuiScrollBar;
import mekanism.common.Mekanism;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.collection.HashList;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiFilterHolder<FILTER extends IFilter<?>, TILE extends TileEntityMekanism & ITileFilterHolder<FILTER>, CONTAINER extends MekanismTileContainer<TILE>>
      extends GuiMekanismTile<TILE, CONTAINER> {

    /**
     * The number of filters that can be displayed
     */
    private static final int FILTER_COUNT = 4;
    private GuiScrollBar scrollBar;

    protected GuiFilterHolder(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        imageHeight += 86;
        inventoryLabelY = imageHeight - 92;
        dynamicSlots = true;
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiInnerScreen(this, 9, 17, 46, 140));
        //Filter holder
        addButton(new GuiElementHolder(this, 55, 17, 98, 118));
        //new filter button border
        addButton(new GuiElementHolder(this, 55, 135, 98, 22));
        scrollBar = addButton(new GuiScrollBar(this, 153, 17, 140, () -> getFilters().size(), () -> FILTER_COUNT));
        //Add each of the buttons and then just change visibility state to match filter info
        for (int i = 0; i < FILTER_COUNT; i++) {
            addFilterButton(new MovableFilterButton(this, 56, 18 + i * 29, i, scrollBar::getCurrentSelection, this::getFilters, index -> {
                if (index > 0) {
                    Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.MOVE_FILTER_UP, tile, index));
                }
            }, index -> {
                if (index < getFilters().size() - 1) {
                    Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.MOVE_FILTER_DOWN, tile, index));
                }
            }, this::onClick, filter -> {
                List<ItemStack> list = new ArrayList<>();
                if (filter != null) {
                    if (filter instanceof IItemStackFilter) {
                        list.add(((IItemStackFilter<?>) filter).getItemStack());
                    } else if (filter instanceof ITagFilter) {
                        list.addAll(getTagStacks(((ITagFilter<?>) filter).getTagName()));
                    } else if (filter instanceof IMaterialFilter) {
                        list.addAll(TagCache.getMaterialStacks(((IMaterialFilter<?>) filter).getMaterialItem()));
                    } else if (filter instanceof IModIDFilter) {
                        list.addAll(TagCache.getModIDStacks(((IModIDFilter<?>) filter).getModID(), false));
                    }
                }
                return list;
            }));
        }
    }

    protected FilterButton addFilterButton(FilterButton button) {
        return addButton(button);
    }

    protected HashList<FILTER> getFilters() {
        return tile.getFilters();
    }

    protected abstract void onClick(IFilter<?> filter, int index);

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta) || scrollBar.adjustScroll(delta);
    }

    protected abstract List<ItemStack> getTagStacks(String tagName);

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.drawForegroundText(matrix, mouseX, mouseY);
        drawString(matrix, inventory.getDisplayName(), inventoryLabelX, inventoryLabelY, titleTextColor());
    }
}