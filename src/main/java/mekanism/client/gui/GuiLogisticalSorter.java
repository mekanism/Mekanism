package mekanism.client.gui;

import java.util.List;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.GuiSlot;
import mekanism.client.gui.element.GuiSlot.SlotType;
import mekanism.client.gui.element.button.ColorButton;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.HashList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.OreDictCache;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IMaterialFilter;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiLogisticalSorter extends GuiFilterHolder<TransporterFilter<?>, TileEntityLogisticalSorter, EmptyTileContainer<TileEntityLogisticalSorter>> {

    public GuiLogisticalSorter(EmptyTileContainer<TileEntityLogisticalSorter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiSlot(SlotType.NORMAL, this, 12, 136));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));

        addButton(new TranslationButton(this, getGuiLeft() + 56, getGuiTop() + 136, 96, 20, MekanismLang.BUTTON_NEW_FILTER,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_SELECT_FILTER_TYPE, tile.getPos()))));
        addButton(new MekanismImageButton(this, getGuiLeft() + 12, getGuiTop() + 58, 14, getButtonLocation("single"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(5))),
              getOnHover(MekanismLang.SORTER_SINGLE_ITEM_DESCRIPTION)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 12, getGuiTop() + 84, 14, getButtonLocation("round_robin"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(2))),
              getOnHover(MekanismLang.SORTER_ROUND_ROBIN_DESCRIPTION)));
        addButton(new MekanismImageButton(this, getGuiLeft() + 12, getGuiTop() + 110, 14, getButtonLocation("auto_eject"),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(1))),
              getOnHover(MekanismLang.SORTER_AUTO_EJECT_DESCRIPTION)));
        addButton(new ColorButton(this, getGuiLeft() + 13, getGuiTop() + 137, 16, 16, () -> tile.color,
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0, InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(),
                    GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0))),
              () -> Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0, 1)))));
    }

    @Override
    protected void upButtonPress(int index) {
        if (index > 0) {
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(3, index)));
        }
    }

    @Override
    protected void downButtonPress(int index) {
        if (index < getFilters().size() - 1) {
            Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(4, index)));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        HashList<TransporterFilter<?>> filters = getFilters();
        // Write to info display
        drawString(tile.getName(), 43, 6, 0x404040);
        drawString(MekanismLang.FILTERS.translate(), 11, 19, 0x00CD00);
        drawString(MekanismLang.FILTER_COUNT.translate(filters.size()), 11, 28, 0x00CD00);
        drawString(MekanismLang.SORTER_SINGLE_ITEM.translate(), 12, 48, 0x00CD00);
        drawString(OnOff.of(tile.singleItem).getTextComponent(), 27, 60, 0x00CD00);
        drawString(MekanismLang.SORTER_ROUND_ROBIN.translate(), 12, 74, 0x00CD00);
        drawString(OnOff.of(tile.roundRobin).getTextComponent(), 27, 86, 0x00CD00);
        drawString(MekanismLang.SORTER_AUTO_EJECT.translate(), 12, 100, 0x00CD00);
        drawString(OnOff.of(tile.autoEject).getTextComponent(), 27, 112, 0x00CD00);
        drawString(MekanismLang.SORTER_DEFAULT.translate(), 12, 126, 0x00CD00);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void onClick(IFilter<?> filter, int index) {
        if (filter instanceof IItemStackFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_ITEMSTACK, tile.getPos(), index));
        } else if (filter instanceof ITagFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_TAG, tile.getPos(), index));
        } else if (filter instanceof IMaterialFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_MATERIAL, tile.getPos(), index));
        } else if (filter instanceof IModIDFilter) {
            Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.LS_FILTER_MOD_ID, tile.getPos(), index));
        }
    }

    @Override
    protected List<ItemStack> getTagStacks(String tagName) {
        return OreDictCache.getItemTagStacks(tagName);
    }
}