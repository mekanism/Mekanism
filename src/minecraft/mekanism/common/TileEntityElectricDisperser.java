package mekanism.common;

import net.minecraft.item.ItemStack;
import net.minecraftforge.liquids.LiquidTank;

public class TileEntityElectricDisperser extends TileEntityElectricBlock
{
	public LiquidTank liquidTank;
	
	public TileEntityElectricDisperser()
	{
		super("Electric Disperser", 10000);
		liquidTank = new LiquidTank(10000);
		inventory = new ItemStack[3];
	}
}
