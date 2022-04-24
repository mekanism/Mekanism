package mekanism.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterFilerSelect;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterItemStackFilter;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterMaterialFilter;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterModIDFilter;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterTagFilter;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.content.transporter.SorterMaterialFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiLogisticalSorter extends GuiFilterHolder<SorterFilter<?>, TileEntityLogisticalSorter, MekanismTileContainer<TileEntityLogisticalSorter>> {

    public GuiLogisticalSorter(MekanismTileContainer<TileEntityLogisticalSorter> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 12, 136).setRenderAboveSlots());
        addRenderableWidget(new TranslationButton(this, 56, 136, 96, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> addWindow(new GuiSorterFilerSelect(this, tile))));
        addRenderableWidget(new MekanismImageButton(this, 12, 58, 14, getButtonLocation("single"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.SINGLE_ITEM_BUTTON, tile)),
              getOnHover(MekanismLang.SORTER_SINGLE_ITEM_DESCRIPTION)));
        addRenderableWidget(new MekanismImageButton(this, 12, 84, 14, getButtonLocation("round_robin"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.ROUND_ROBIN_BUTTON, tile)),
              getOnHover(MekanismLang.SORTER_ROUND_ROBIN_DESCRIPTION)));
        addRenderableWidget(new MekanismImageButton(this, 12, 110, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_EJECT_BUTTON, tile)),
              getOnHover(MekanismLang.SORTER_AUTO_EJECT_DESCRIPTION)));
        addRenderableWidget(new ColorButton(this, 13, 137, 16, 16, () -> tile.color,
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.CHANGE_COLOR, tile,
                    hasShiftDown() ? -1 : TransporterUtils.getColorIndex(TransporterUtils.increment(tile.color)))),
              () -> Mekanism.packetHandler().sendToServer(new PacketGuiInteract(GuiInteraction.CHANGE_COLOR, tile,
                    TransporterUtils.getColorIndex(TransporterUtils.decrement(tile.color))))));
    }

    @Override
    protected void drawForegroundText(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.drawForegroundText(matrix, mouseX, mouseY);
        // Write to info display
        renderTitleText(matrix);
        drawTextWithScale(matrix, MekanismLang.FILTERS.translate(), 14, 22, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.FILTER_COUNT.translate(getFilters().size()), 14, 31, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.SORTER_SINGLE_ITEM.translate(), 14, 48, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, OnOff.of(tile.getSingleItem()).getTextComponent(), 27, 60, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.SORTER_ROUND_ROBIN.translate(), 14, 74, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, OnOff.of(tile.getRoundRobin()).getTextComponent(), 27, 86, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.SORTER_AUTO_EJECT.translate(), 14, 100, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, OnOff.of(tile.getAutoEject()).getTextComponent(), 27, 112, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.SORTER_DEFAULT.translate(), 14, 126, screenTextColor(), 0.8F);
    }

    @Override
    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof IItemStackFilter) {
            addWindow(GuiSorterItemStackFilter.edit(this, tile, (SorterItemStackFilter) filter));
        } else if (filter instanceof ITagFilter) {
            addWindow(GuiSorterTagFilter.edit(this, tile, (SorterTagFilter) filter));
        } else if (filter instanceof IMaterialFilter) {
            addWindow(GuiSorterMaterialFilter.edit(this, tile, (SorterMaterialFilter) filter));
        } else if (filter instanceof IModIDFilter) {
            addWindow(GuiSorterModIDFilter.edit(this, tile, (SorterModIDFilter) filter));
        }
    }

    @Override
    protected List<ItemStack> getTagStacks(String tagName) {
        return TagCache.getItemTagStacks(tagName);
    }

    @Override
    protected List<ItemStack> getModIDStacks(String tagName) {
        return TagCache.getItemModIDStacks(tagName);
    }
}