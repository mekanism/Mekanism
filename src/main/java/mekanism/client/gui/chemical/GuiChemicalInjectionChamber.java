package mekanism.client.gui.chemical;

import mekanism.client.gui.GuiAdvancedElectricMachine;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityChemicalInjectionChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiChemicalInjectionChamber extends GuiAdvancedElectricMachine<TileEntityChemicalInjectionChamber, MekanismTileContainer<TileEntityChemicalInjectionChamber>> {

    public GuiChemicalInjectionChamber(MekanismTileContainer<TileEntityChemicalInjectionChamber> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.YELLOW;
    }
}