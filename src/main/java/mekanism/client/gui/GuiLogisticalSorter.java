package mekanism.client.gui;

import java.util.List;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterFilerSelect;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterItemStackFilter;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterModIDFilter;
import mekanism.client.gui.element.window.filter.transporter.GuiSorterTagFilter;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiLogisticalSorter extends GuiFilterHolder<SorterFilter<?>, TileEntityLogisticalSorter, MekanismTileContainer<TileEntityLogisticalSorter>> {

    public GuiLogisticalSorter(MekanismTileContainer<TileEntityLogisticalSorter> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiSlot(SlotType.NORMAL, this, 12, 136).setRenderAboveSlots());
        addRenderableWidget(new TranslationButton(this, 96, 136, 156, 20, MekanismLang.BUTTON_NEW_FILTER, (element, mouseX, mouseY) -> {
            GuiLogisticalSorter gui = (GuiLogisticalSorter) element.gui();
            gui.addWindow(new GuiSorterFilerSelect(gui, gui.tile));
            return true;
        }));
        addRenderableWidget(new MekanismImageButton(this, 12, 46, 14, getButtonLocation("single"),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.SINGLE_ITEM_BUTTON, ((GuiLogisticalSorter) element.gui()).tile))))
              .setTooltip(MekanismLang.SORTER_SINGLE_ITEM_DESCRIPTION);
        addRenderableWidget(new MekanismImageButton(this, 12, 76, 14, getButtonLocation("round_robin"),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.ROUND_ROBIN_BUTTON, ((GuiLogisticalSorter) element.gui()).tile))))
              .setTooltip(MekanismLang.SORTER_ROUND_ROBIN_DESCRIPTION);
        addRenderableWidget(new MekanismImageButton(this, 12, 106, 14, getButtonLocation("auto_eject"),
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_EJECT_BUTTON, ((GuiLogisticalSorter) element.gui()).tile))))
              .setTooltip(MekanismLang.SORTER_AUTO_EJECT_DESCRIPTION);
        addRenderableWidget(new ColorButton(this, 13, 137, 16, 16, () -> tile.color, (element, mouseX, mouseY) -> {
            TileEntityLogisticalSorter tile = ((GuiLogisticalSorter) element.gui()).tile;
            return PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.CHANGE_COLOR, tile, hasShiftDown() ? -1 : TransporterUtils.getColorIndex(TransporterUtils.increment(tile.color))));
        }, (element, mouseX, mouseY) -> {
            TileEntityLogisticalSorter tile = ((GuiLogisticalSorter) element.gui()).tile;
            return PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.CHANGE_COLOR, tile, TransporterUtils.getColorIndex(TransporterUtils.decrement(tile.color))));
        }));
    }

    @Override
    protected void drawForegroundText(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.drawForegroundText(guiGraphics, mouseX, mouseY);
        // Write to info display
        renderTitleText(guiGraphics);
        drawScreenText(guiGraphics, MekanismLang.FILTER_COUNT.translate(getFilterManager().count()), 4);
        drawScreenText(guiGraphics, MekanismLang.SORTER_SINGLE_ITEM.translate(), 19);
        drawScreenText(guiGraphics, OnOff.of(tile.getSingleItem()).getTextComponent(), 14, 32);
        drawScreenText(guiGraphics, MekanismLang.SORTER_ROUND_ROBIN.translate(), 49);
        drawScreenText(guiGraphics, OnOff.of(tile.getRoundRobin()).getTextComponent(), 14, 62);
        drawScreenText(guiGraphics, MekanismLang.SORTER_AUTO_EJECT.translate(), 79);
        drawScreenText(guiGraphics, OnOff.of(tile.getAutoEject()).getTextComponent(), 14, 92);
        drawScreenText(guiGraphics, MekanismLang.SORTER_DEFAULT.translate(), 109);
    }

    @Override
    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof IItemStackFilter) {
            addWindow(GuiSorterItemStackFilter.edit(this, tile, (SorterItemStackFilter) filter));
        } else if (filter instanceof ITagFilter) {
            addWindow(GuiSorterTagFilter.edit(this, tile, (SorterTagFilter) filter));
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