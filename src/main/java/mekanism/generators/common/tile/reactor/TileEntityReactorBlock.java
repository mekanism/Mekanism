package mekanism.generators.common.tile.reactor;

import java.util.EnumSet;

import mekanism.api.reactor.IFusionReactor;
import mekanism.api.reactor.IReactorBlock;
import mekanism.common.tile.TileEntityElectricBlock;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileEntityReactorBlock extends TileEntityElectricBlock implements IReactorBlock
{
	public IFusionReactor fusionReactor;
	public boolean changed;

	public TileEntityReactorBlock()
	{
		super("ReactorBlock", 0);
		inventory = new ItemStack[0];
	}

	public TileEntityReactorBlock(String name, double maxEnergy)
	{
		super(name, maxEnergy);
	}

	@Override
	public void setReactor(IFusionReactor reactor)
	{
		if(reactor != fusionReactor)
		{
			changed = true;
		}
		fusionReactor = reactor;
	}

	@Override
	public IFusionReactor getReactor()
	{
		return fusionReactor;
	}

	@Override
	public void invalidate()
	{
		if(getReactor() != null)
		{
			getReactor().formMultiblock();
		}
	}

	@Override
	public void onUpdate()
	{
		if(changed)
		{
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
			changed = false;
		}
	}

	@Override
	public double transferEnergyToAcceptor(ForgeDirection side, double energy)
	{
		return 0;
	}

	@Override
	public boolean canReceiveEnergy(ForgeDirection side)
	{
		return false;
	}

	public EnumSet<ForgeDirection> getOutputtingSides()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}

	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}
}
