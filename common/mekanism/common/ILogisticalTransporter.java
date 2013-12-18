package mekanism.common;

import mekanism.api.EnumColor;
import mekanism.api.Object3D;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.transporter.TransporterStack;
import net.minecraft.item.ItemStack;

public interface ILogisticalTransporter
{
	public ItemStack insert(Object3D original, ItemStack itemStack, EnumColor color, boolean doEmit, int min);
	
	public ItemStack insertRR(TileEntityLogisticalSorter outputter, ItemStack itemStack, EnumColor color, boolean doEmit, int min);
	
	public void entityEntering(TransporterStack stack);
}
