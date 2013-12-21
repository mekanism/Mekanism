package mekanism.common;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.tileentity.TileEntityLogisticalSorter;
import mekanism.common.transporter.TransporterStack;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public interface ILogisticalTransporter
{
	public ItemStack insert(Coord4D original, ItemStack itemStack, EnumColor color, boolean doEmit, int min);
	
	public ItemStack insertRR(TileEntityLogisticalSorter outputter, ItemStack itemStack, EnumColor color, boolean doEmit, int min);
	
	public void entityEntering(TransporterStack stack);
	
	public EnumColor getColor();
	
	public void setColor(EnumColor c);
	
	public TileEntity getTile();
	
	boolean canConnect(TileEntity tileEntity, ForgeDirection side);
	
	public boolean canConnectMutual(TileEntity tileEntity, ForgeDirection side);
}
