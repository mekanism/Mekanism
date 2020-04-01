package mekanism.client.gui;

import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.progress.GuiProgress;
import mekanism.client.gui.element.progress.ProgressType;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.Module;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityModificationStation;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiModificationStation extends GuiMekanismTile<TileEntityModificationStation, MekanismTileContainer<TileEntityModificationStation>> {

    private GuiModuleScrollList scrollList;
    private Module selectedModule;

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
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 163, 29));
        addButton(new GuiEnergyInfo(tile.getEnergyContainer(), this));
        addButton(new GuiProgress(tile::getScaledProgress, ProgressType.LARGE_RIGHT, this, 65, 109));

        addButton(scrollList = new GuiModuleScrollList(this, 34, 20, 108, 74, tile.moduleSlot.getStack(), this::onModuleSelected));
    }

    public void onModuleSelected(Module module) {
        selectedModule = module;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(tile.getName(), (getXSize() / 2) - (getStringWidth(tile.getName()) / 2), 6, 0x404040);
        drawString(MekanismLang.INVENTORY.translate(), 8, (getYSize() - 94) + 2, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }
}
