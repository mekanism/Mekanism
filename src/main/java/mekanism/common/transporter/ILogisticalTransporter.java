package mekanism.common.transporter;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.transmitters.IBlockableConnection;
import mekanism.common.tile.TileEntityLogisticalSorter;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public interface ILogisticalTransporter extends IBlockableConnection
{
	public ItemStack insert(Coord4D original, ItemStack itemStack, EnumColor color, boolean doEmit, int min);

	public ItemStack insertRR(TileEntityLogisticalSorter outputter, ItemStack itemStack, EnumColor color, boolean doEmit, int min);

	public void entityEntering(TransporterStack stack);

	public EnumColor getColor();

	public void setColor(EnumColor c);

	public TileEntity getTile();

	public boolean canEmitTo(TileEntity tileEntity, ForgeDirection side);

	public boolean canReceiveFrom(TileEntity tileEntity, ForgeDirection side);

	public int getCost();
}
