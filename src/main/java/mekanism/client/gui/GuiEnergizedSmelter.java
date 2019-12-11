package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.EnergizedSmelterContainer;
import mekanism.common.tile.TileEntityEnergizedSmelter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiEnergizedSmelter extends GuiElectricMachine<TileEntityEnergizedSmelter, EnergizedSmelterContainer> {

    public GuiEnergizedSmelter(EnergizedSmelterContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.GREEN;
    }
}