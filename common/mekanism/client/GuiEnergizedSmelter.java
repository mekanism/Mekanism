package mekanism.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.common.TileEntityElectricMachine;
import net.minecraft.entity.player.InventoryPlayer;

@SideOnly(Side.CLIENT)
public class GuiEnergizedSmelter extends GuiElectricMachine
{
	public GuiEnergizedSmelter(InventoryPlayer inventory, TileEntityElectricMachine tentity)
	{
		super(inventory, tentity);
	}
}
