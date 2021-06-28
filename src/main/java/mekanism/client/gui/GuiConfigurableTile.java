package mekanism.client.gui;

import mekanism.client.gui.element.tab.window.GuiSideConfigurationTab;
import mekanism.client.gui.element.tab.window.GuiTransporterConfigTab;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiConfigurableTile<TILE extends TileEntityMekanism & ISideConfiguration, CONTAINER extends MekanismTileContainer<TILE>>
      extends GuiMekanismTile<TILE, CONTAINER> {

    private GuiSideConfigurationTab<TILE> sideConfigTab;
    private GuiTransporterConfigTab<TILE> transporterConfigTab;

    protected GuiConfigurableTile(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        sideConfigTab = addButton(new GuiSideConfigurationTab<>(this, tile, () -> sideConfigTab));
        transporterConfigTab = addButton(new GuiTransporterConfigTab<>(this, tile, () -> transporterConfigTab));
    }
}