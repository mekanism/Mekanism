package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.PurificationChamberContainer;
import mekanism.common.tile.TileEntityPurificationChamber;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiPurificationChamber extends GuiAdvancedElectricMachine<TileEntityPurificationChamber, PurificationChamberContainer> {

    public GuiPurificationChamber(PurificationChamberContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.RED;
    }
}