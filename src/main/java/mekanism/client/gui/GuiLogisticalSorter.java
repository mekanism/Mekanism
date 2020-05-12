package mekanism.client.gui;

import java.util.List;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.TagCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiLogisticalSorter extends GuiFilterHolder<TransporterFilter<?>, TileEntityLogisticalSorter, EmptyTileContainer<TileEntityLogisticalSorter>> {

    public GuiLogisticalSorter(EmptyTileContainer<TileEntityLogisticalSorter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSlot(SlotType.NORMAL, this, 12, 136));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));

        addButton(new TranslationButton(this, getGuiLeft() + 56, getGuiTop() + 136, 96, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_SELECT_FILTER_TYPE, tile))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 12, getGuiTop() + 58, 14, getButtonLocation("single"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.SINGLE_ITEM_BUTTON, tile)),
              getOnHover(MekanismLang.SORTER_SINGLE_ITEM_DESCRIPTION)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 12, getGuiTop() + 84, 14, getButtonLocation("round_robin"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.ROUND_ROBIN_BUTTON, tile)),
              getOnHover(MekanismLang.SORTER_ROUND_ROBIN_DESCRIPTION)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 12, getGuiTop() + 110, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_EJECT_BUTTON, tile)),
              getOnHover(MekanismLang.SORTER_AUTO_EJECT_DESCRIPTION)));
        addButton(new ColorButton(this, getGuiLeft() + 13, getGuiTop() + 137, 16, 16, () -> tile.color,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CHANGE_COLOR, tile,
                    hasShiftDown() ? -1 : TransporterUtils.getColorIndex(TransporterUtils.increment(tile.color)))),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CHANGE_COLOR, tile,
                    TransporterUtils.getColorIndex(TransporterUtils.decrement(tile.color))))));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Write to info display
        renderTitleText();
        drawTextWithScale(MekanismLang.FILTERS.translate(), 14, 22, screenTextColor(), 0.8F);
        drawTextWithScale(MekanismLang.FILTER_COUNT.translate(getFilters().size()), 14, 31, screenTextColor(), 0.8F);
        drawTextWithScale(MekanismLang.SORTER_SINGLE_ITEM.translate(), 14, 48, screenTextColor(), 0.8F);
        drawTextWithScale(OnOff.of(tile.singleItem).getTextComponent(), 27, 60, screenTextColor(), 0.8F);
        drawTextWithScale(MekanismLang.SORTER_ROUND_ROBIN.translate(), 14, 74, screenTextColor(), 0.8F);
        drawTextWithScale(OnOff.of(tile.roundRobin).getTextComponent(), 27, 86, screenTextColor(), 0.8F);
        drawTextWithScale(MekanismLang.SORTER_AUTO_EJECT.translate(), 14, 100, screenTextColor(), 0.8F);
        drawTextWithScale(OnOff.of(tile.autoEject).getTextComponent(), 27, 112, screenTextColor(), 0.8F);
        drawTextWithScale(MekanismLang.SORTER_DEFAULT.translate(), 14, 126, screenTextColor(), 0.8F);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof IItemStackFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_ITEMSTACK, tile, index));
        } else if (filter instanceof ITagFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_TAG, tile, index));
        } else if (filter instanceof IMaterialFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_MATERIAL, tile, index));
        } else if (filter instanceof IModIDFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_MOD_ID, tile, index));
        }
    }

    @Override
    protected List<ItemStack> getTagStacks(String tagName) {
        return TagCache.getItemTagStacks(tagName);
    }
}