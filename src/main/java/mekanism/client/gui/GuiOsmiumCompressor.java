package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.tile.TileEntityOsmiumCompressor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiOsmiumCompressor extends GuiAdvancedElectricMachine<TileEntityOsmiumCompressor, MekanismTileContainer<TileEntityOsmiumCompressor>> {

    public GuiOsmiumCompressor(MekanismTileContainer<TileEntityOsmiumCompressor> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.RED;
    }
}