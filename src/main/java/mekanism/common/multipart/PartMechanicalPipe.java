package mekanism.common.multipart;

import java.util.Collection;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.FluidNetwork;
import mekanism.common.Tier;
import mekanism.common.Tier.PipeTier;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import codechicken.lib.vec.Vector3;

public class PartMechanicalPipe extends PartTransmitter<IFluidHandler, FluidNetwork> implements IFluidHandler
{
	public static TransmitterIcons pipeIcons = new TransmitterIcons(4, 8);

	public float currentScale;

	public FluidTank buffer = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

	public FluidStack lastWrite;

	public Tier.PipeTier tier;

	public PartMechanicalPipe(Tier.PipeTier pipeTier)
	{
		super();
		tier = pipeTier;
		buffer.setCapacity(getCapacity());
	}

	@Override
	public void update()
	{
		if(!world().isRemote)
		{
			if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetworkSize() > 0)
			{
				FluidStack last = getSaveShare();

				if((last != null && !(lastWrite != null && lastWrite.amount == last.amount && lastWrite.getFluid() == last.getFluid())) || (last == null && lastWrite != null))
				{
					lastWrite = last;
					MekanismUtils.saveChunk(tile());
				}
			}

			IFluidHandler[] connectedAcceptors = PipeUtils.getConnectedAcceptors(tile());

			for(ForgeDirection side : getConnections(ConnectionType.PULL))
			{
				if(connectedAcceptors[side.ordinal()] != null)
				{
					IFluidHandler container = connectedAcceptors[side.ordinal()];

					if(container != null)
					{
						FluidStack received = container.drain(side.getOpposite(), getPullAmount(), false);

						if(received != null && received.amount != 0)
						{
							container.drain(side.getOpposite(), takeFluid(received, true), true);
						}
					}
				}
			}
		}

		super.update();
	}

	private FluidStack getSaveShare()
	{
		if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null)
		{
			int remain = getTransmitter().getTransmitterNetwork().buffer.amount%getTransmitter().getTransmitterNetwork().transmitters.size();
			int toSave = getTransmitter().getTransmitterNetwork().buffer.amount/getTransmitter().getTransmitterNetwork().transmitters.size();

			if(getTransmitter().getTransmitterNetwork().transmitters.iterator().next().equals(getTransmitter()))
			{
				toSave += remain;
			}

			return new FluidStack(getTransmitter().getTransmitterNetwork().buffer.getFluid(), toSave);
		}

		return null;
	}

	@Override
	public void onChunkUnload()
	{
		if(!world().isRemote && getTransmitter().hasTransmitterNetwork())
		{
			if(lastWrite != null && getTransmitter().getTransmitterNetwork().buffer != null)
			{
				getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;

				if(getTransmitter().getTransmitterNetwork().buffer.amount <= 0)
				{
					getTransmitter().getTransmitterNetwork().buffer = null;
				}
			}
		}

		super.onChunkUnload();
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);

		if(nbtTags.hasKey("cacheFluid"))
		{
			buffer.setFluid(FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cacheFluid")));
		}

		tier = Tier.PipeTier.values()[nbtTags.getInteger("tier")];
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);

		if(lastWrite != null && lastWrite.amount > 0)
		{
			nbtTags.setTag("cacheFluid", lastWrite.writeToNBT(new NBTTagCompound()));
		}

		nbtTags.setInteger("tier", tier.ordinal());
	}

	@Override
	public String getType()
	{
		return "mekanism:mechanical_pipe_" + tier.name().toLowerCase();
	}

	public static void registerIcons(IIconRegister register)
	{
		pipeIcons.registerCenterIcons(register, new String[] {"MechanicalPipeBasic", "MechanicalPipeAdvanced", "MechanicalPipeElite", "MechanicalPipeUltimate"});
		pipeIcons.registerSideIcons(register, new String[] {"MechanicalPipeVerticalBasic", "MechanicalPipeVerticalAdvanced", "MechanicalPipeVerticalElite", "MechanicalPipeVerticalUltimate",
				"MechanicalPipeHorizontalBasic", "MechanicalPipeHorizontalAdvanced", "MechanicalPipeHorizontalElite", "MechanicalPipeHorizontalUltimate"});
	}

	@Override
	public IIcon getCenterIcon(boolean opaque)
	{
		return pipeIcons.getCenterIcon(tier.ordinal());
	}

	@Override
	public IIcon getSideIcon(boolean opaque)
	{
		return pipeIcons.getSideIcon(tier.ordinal());
	}

	@Override
	public IIcon getSideIconRotated(boolean opaque)
	{
		return pipeIcons.getSideIcon(4+tier.ordinal());
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.FLUID;
	}

	@Override
	public TransmitterType getTransmitterType()
	{ 
		return tier.type; 
	}

	@Override
	public boolean isValidAcceptor(TileEntity acceptor, ForgeDirection side)
	{
		return PipeUtils.isValidAcceptorOnSide(acceptor, side);
	}

	@Override
	public FluidNetwork createNewNetwork()
	{
		return new FluidNetwork();
	}

	@Override
	public FluidNetwork createNetworkByMerging(Collection<FluidNetwork> networks)
	{
		return new FluidNetwork(networks);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float f, int pass)
	{
		if(pass == 0)
		{
			RenderPartTransmitter.getInstance().renderContents(this, pos);
		}
	}

	@Override
	public int getCapacity()
	{
		return tier.pipeCapacity;
	}

	@Override
	public FluidStack getBuffer()
	{
		return buffer == null ? null : buffer.getFluid();
	}

	@Override
	public void takeShare()
	{
		if(getTransmitter().hasTransmitterNetwork() && getTransmitter().getTransmitterNetwork().buffer != null && lastWrite != null)
		{
			getTransmitter().getTransmitterNetwork().buffer.amount -= lastWrite.amount;
			buffer.setFluid(lastWrite);
		}
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(getConnectionType(from) == ConnectionType.NORMAL)
		{
			return takeFluid(resource, doFill);
		}

		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return getConnectionType(from) == ConnectionType.NORMAL;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(getConnectionType(from) != ConnectionType.NONE)
		{
			return new FluidTankInfo[] {buffer.getInfo()};
		}

		return new FluidTankInfo[0];
	}

	public int getPullAmount()
	{
		return tier.pipePullAmount;
	}

	public int takeFluid(FluidStack fluid, boolean doEmit)
	{
		if(getTransmitter().hasTransmitterNetwork())
		{
			return getTransmitter().getTransmitterNetwork().emit(fluid, doEmit);
		}
		else {
			return buffer.fill(fluid, doEmit);
		}
	}
}
