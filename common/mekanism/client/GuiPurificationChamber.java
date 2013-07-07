package mekanism.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.common.TileEntityAdvancedElectricMachine;
import net.minecraft.entity.player.InventoryPlayer;

@SideOnly(Side.CLIENT)
public class GuiPurificationChamber extends GuiAdvancedElectricMachine
{
	public GuiPurificationChamber(InventoryPlayer inventory, TileEntityAdvancedElectricMachine tentity)
	{
		super(inventory, tentity);
	}
}
