package mekanism.common.base;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.transmitters.grid.InventoryNetwork;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public interface ILogisticalTransporter extends IGridTransmitter<TileEntity, InventoryNetwork>, IBlockableConnection
{
	public ItemStack insert(Coord4D original, ItemStack itemStack, EnumColor color, boolean doEmit, int min);

	public ItemStack insertRR(TileEntityLogisticalSorter outputter, ItemStack itemStack, EnumColor color, boolean doEmit, int min);

	public void entityEntering(TransporterStack stack, int progress);

	public EnumColor getColor();

	public void setColor(EnumColor c);

	public boolean canEmitTo(TileEntity tileEntity, EnumFacing side);

	public boolean canReceiveFrom(TileEntity tileEntity, EnumFacing side);

	public double getCost();
}
