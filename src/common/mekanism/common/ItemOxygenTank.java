package mekanism.common;

import mekanism.api.IStorageTank.EnumGas;
import net.minecraft.src.*;

public class ItemOxygenTank extends ItemStorageTank
{
	public ItemOxygenTank(int id)
	{
		super(id, 1600, 16, 16);
	}
	
	public EnumGas gasType()
	{
		return EnumGas.OXYGEN;
	}
}
