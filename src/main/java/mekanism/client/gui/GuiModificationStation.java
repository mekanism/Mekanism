package mekanism.client.gui;

import mekanism.client.gui.element.GuiEnergyInfo;
import mekanism.client.gui.element.GuiRedstoneControl;
import mekanism.client.gui.element.bar.GuiVerticalPowerBar;
import mekanism.client.gui.element.scroll.GuiModuleScrollList;
import mekanism.client.gui.element.tab.GuiSecurityTab;
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
    }

    @Override
    public void init() {
        super.init();

        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControl(this, tile));
        addButton(new GuiVerticalPowerBar(this, tile.getEnergyContainer(), 159, 15));
        addButton(new GuiEnergyInfo(tile.getEnergyContainer(), this));
        addButton(scrollList = new GuiModuleScrollList(this, 30, 20, 108, 98, tile.inputSlot.getStack(), this::onModuleSelected));
    }

    public void onModuleSelected(Module module) {
        selectedModule = module;
    }
}
