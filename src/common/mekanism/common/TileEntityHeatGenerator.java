package mekanism.common;

import com.google.common.io.ByteArrayDataInput;

import net.minecraft.src.*;

public class TileEntityHeatGenerator extends TileEntityGenerator
{
	public TileEntityHeatGenerator()
	{
		super("Heat Generator", 8000, 2000);
	}

	@Override
	public int getFuel(ItemStack itemstack)
	{
		return TileEntityFurnace.getItemBurnTime(itemstack);
	}
}
