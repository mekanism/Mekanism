package mekanism.common;

import net.minecraft.item.ItemStack;

public interface ISustainedData 
{
	public void writeSustainedData(ItemStack itemStack);
	
	public void readSustainedData(ItemStack itemStack);
}
