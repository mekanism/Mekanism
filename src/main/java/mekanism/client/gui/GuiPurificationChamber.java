package mekanism.client.gui;

import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import net.minecraft.entity.player.InventoryPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPurificationChamber extends GuiAdvancedElectricMachine
{
	public GuiPurificationChamber(InventoryPlayer inventory, TileEntityAdvancedElectricMachine tentity)
	{
		super(inventory, tentity);
	}
}
