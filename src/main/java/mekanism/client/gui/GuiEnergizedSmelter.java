package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiEnergizedSmelter extends GuiElectricMachine<TileEntityEnergizedSmelter, MekanismTileContainer<TileEntityEnergizedSmelter>> {

    public GuiEnergizedSmelter(MekanismTileContainer<TileEntityEnergizedSmelter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.GREEN;
    }
}