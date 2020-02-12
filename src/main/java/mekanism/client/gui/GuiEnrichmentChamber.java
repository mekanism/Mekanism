package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityEnrichmentChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiEnrichmentChamber extends GuiElectricMachine<TileEntityEnrichmentChamber, MekanismTileContainer<TileEntityEnrichmentChamber>> {

    public GuiEnrichmentChamber(MekanismTileContainer<TileEntityEnrichmentChamber> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.BLUE;
    }
}