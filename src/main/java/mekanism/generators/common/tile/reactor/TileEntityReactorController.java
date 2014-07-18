package mekanism.generators.common.tile.reactor;

import mekanism.api.gas.GasTank;
import mekanism.common.Mekanism;
import mekanism.generators.common.FusionReactor;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidTank;

public class TileEntityReactorController extends TileEntityReactorBlock
{
	public static final int MAX_WATER = 100 * FluidContainerRegistry.BUCKET_VOLUME;

	public static final int MAX_FUEL = 1 * FluidContainerRegistry.BUCKET_VOLUME;

	public FluidTank waterTank = new FluidTank(MAX_WATER);
	public FluidTank steamTank = new FluidTank(MAX_WATER*1000);

	public GasTank deuteriumTank = new GasTank(MAX_FUEL);
	public GasTank tritiumTank = new GasTank(MAX_FUEL);

	public GasTank fuelTank = new GasTank(MAX_FUEL);

	public TileEntityReactorController()
	{
		super("ReactorController", 1000000000);
		inventory = new ItemStack[1];
	}

	@Override
	public boolean isFrame()
	{
		return false;
	}

	public void radiateNeutrons(int neutrons)
	{
	}

	public void formMultiblock()
	{
		if(getReactor() == null)
		{
			setReactor(new FusionReactor(this));
		}
		getReactor().formMultiblock();
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(getReactor() != null && !worldObj.isRemote)
		{
			getReactor().simulate();
		}
	}
}
