package mekanism.client.gui;

import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEnergizedSmelter extends GuiElectricMachine {

    public GuiEnergizedSmelter(InventoryPlayer inventory, TileEntityElectricMachine tile) {
        super(inventory, tile);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.GREEN;
    }
}