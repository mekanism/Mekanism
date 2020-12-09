package mekanism.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
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
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiLogisticalSorter extends GuiFilterHolder<SorterFilter<?>, TileEntityLogisticalSorter, MekanismTileContainer<TileEntityLogisticalSorter>> {

    public GuiLogisticalSorter(MekanismTileContainer<TileEntityLogisticalSorter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSlot(SlotType.NORMAL, this, 12, 136).setRenderAboveSlots());
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiSecurityTab(this, tile));
        addButton(new TranslationButton(this, guiLeft + 56, guiTop + 136, 96, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> addWindow(new GuiSorterFilerSelect(this, tile))));
        addButton(new MekanismImageButton(this, guiLeft + 12, guiTop + 58, 14, getButtonLocation("single"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.SINGLE_ITEM_BUTTON, tile)),
              getOnHover(MekanismLang.SORTER_SINGLE_ITEM_DESCRIPTION)));
        addButton(new MekanismImageButton(this, guiLeft + 12, guiTop + 84, 14, getButtonLocation("round_robin"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.ROUND_ROBIN_BUTTON, tile)),
              getOnHover(MekanismLang.SORTER_ROUND_ROBIN_DESCRIPTION)));
        addButton(new MekanismImageButton(this, guiLeft + 12, guiTop + 110, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.AUTO_EJECT_BUTTON, tile)),
              getOnHover(MekanismLang.SORTER_AUTO_EJECT_DESCRIPTION)));
        addButton(new ColorButton(this, guiLeft + 13, guiTop + 137, 16, 16, () -> tile.color,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CHANGE_COLOR, tile,
                    hasShiftDown() ? -1 : TransporterUtils.getColorIndex(TransporterUtils.increment(tile.color)))),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.CHANGE_COLOR, tile,
                    TransporterUtils.getColorIndex(TransporterUtils.decrement(tile.color))))));
    }

    @Override
    protected void drawForegroundText(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.drawForegroundText(matrix, mouseX, mouseY);
        // Write to info display
        renderTitleText(matrix);
        drawTextWithScale(matrix, MekanismLang.FILTERS.translate(), 14, 22, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.FILTER_COUNT.translate(getFilters().size()), 14, 31, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.SORTER_SINGLE_ITEM.translate(), 14, 48, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, OnOff.of(tile.singleItem).getTextComponent(), 27, 60, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.SORTER_ROUND_ROBIN.translate(), 14, 74, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, OnOff.of(tile.roundRobin).getTextComponent(), 27, 86, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, MekanismLang.SORTER_AUTO_EJECT.translate(), 14, 100, screenTextColor(), 0.8F);
        drawTextWithScale(matrix, OnOff.of(tile.autoEject).getTextComponent(), 27, 112, screenTextColor(), 0.8F);
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
}