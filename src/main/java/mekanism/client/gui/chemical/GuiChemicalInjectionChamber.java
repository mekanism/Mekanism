package mekanism.client.gui.chemical;

import mekanism.client.gui.GuiAdvancedElectricMachine;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiChemicalInjectionChamber extends GuiAdvancedElectricMachine {

    public GuiChemicalInjectionChamber(InventoryPlayer inventory, TileEntityAdvancedElectricMachine tile) {
        super(inventory, tile);
    }

    @Override
    public ProgressBar getProgressType() {
        return ProgressBar.YELLOW;
    }
}