package mekanism.generators.common.tile.reactor;

import java.util.ArrayList;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.IActiveState;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.FusionReactor;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import io.netty.buffer.ByteBuf;

public class TileEntityReactorController extends TileEntityReactorBlock implements IActiveState
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

	public double getPlasmaTemp()
	{
		if(getReactor() == null || !getReactor().isFormed())
		{
			return 0;
		}
		return getReactor().getPlasmaTemp();
	}

	public double getCaseTemp()
	{
		if(getReactor() == null || !getReactor().isFormed())
		{
			return 0;
		}
		return getReactor().getCaseTemp();
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(getReactor() != null && getReactor().isFormed())
		{
			getReactor().simulate();
		}
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		data.add(getReactor() != null && getReactor().isFormed());
		if(getReactor() != null)
		{
			data.add(getReactor().getPlasmaTemp());
			data.add(getReactor().getCaseTemp());
			data.add(fuelTank.getStored());
			data.add(deuteriumTank.getStored());
			data.add(tritiumTank.getStored());
			data.add(waterTank.getFluidAmount());
			data.add(steamTank.getFluidAmount());
		}

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();

			if(type == 0)
			{
				formMultiblock();
			}

			return;
		}

		super.handlePacketData(dataStream);

		boolean formed = dataStream.readBoolean();
		if(formed)
		{
			if(getReactor() == null)
			{
				setReactor(new FusionReactor(this));
				MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
			}
			((FusionReactor)getReactor()).formed = true;
			getReactor().setPlasmaTemp(dataStream.readDouble());
			getReactor().setCaseTemp(dataStream.readDouble());
			fuelTank.setGas(new GasStack(GasRegistry.getGas("fusionFuelDT"), dataStream.readInt()));
			deuteriumTank.setGas(new GasStack(GasRegistry.getGas("deuterium"), dataStream.readInt()));
			tritiumTank.setGas(new GasStack(GasRegistry.getGas("tritium"), dataStream.readInt()));
			waterTank.setFluid(new FluidStack(FluidRegistry.getFluid("water"), dataStream.readInt()));
			steamTank.setFluid(new FluidStack(FluidRegistry.getFluid("steam"), dataStream.readInt()));
		}
		else if(getReactor() != null)
		{
			setReactor(null);
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
		}
	}

	@Override
	public boolean getActive()
	{
		return getReactor() != null && getReactor().isFormed();
	}

	@Override
	public void setActive(boolean active)
	{
		if(active == (getReactor() == null))
		{
			setReactor(active ? new FusionReactor(this) : null);
		}
	}

	@Override
	public boolean renderUpdate()
	{
		return true;
	}

	@Override
	public boolean lightUpdate()
	{
		return false;
	}
}
