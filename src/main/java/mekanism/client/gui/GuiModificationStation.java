package mekanism.client.gui;

import mekanism.api.Coord4D;
import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketRemoveModule;
import mekanism.common.tile.TileEntityModificationStation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiModificationStation extends GuiMekanismTile<TileEntityModificationStation, MekanismTileContainer<TileEntityModificationStation>> {

    private GuiModuleScrollList scrollList;
    private Module selectedModule;
    private TranslationButton removeButton;

    public GuiModificationStation(MekanismTileContainer<TileEntityModificationStation> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        ySize += 64;
    }

    @Override
    public void init() {
        super.init();

        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 154, 40));
        addButton(new GuiEnergyInfo(tile.getEnergyContainer(), this));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 65, 123));
        addButton(removeButton = new TranslationButton(this, getGuiLeft() + 34, getGuiTop() + 96, 108, 17, MekanismLang.BUTTON_REMOVE, () -> {
            Mekanism.packetHandler.sendToServer(new PacketRemoveModule(Coord4D.get(getTileEntity()), selectedModule.getData()));
            scrollList.clearSelection();
        }));
        removeButton.active = false;

        addButton(scrollList = new GuiModuleScrollList(this, 34, 20, 108, 74, () -> tile.containerSlot.getStack().copy(), this::onModuleSelected));
        scrollList.setResetOnChanged();
    }

    public void onModuleSelected(Module module) {
        selectedModule = module;
        removeButton.active = module != null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
