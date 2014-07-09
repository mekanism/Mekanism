package mekanism.common.multipart;

import java.util.Set;

import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.FluidNetwork;
import mekanism.common.Tier;
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
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartMechanicalPipe extends PartTransmitter<FluidNetwork> implements IFluidHandler
{
	public Tier.PipeTier tier;
	/** The fake tank used for fluid transfer calculations. */
	public FluidTank dummyTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);

	public static TransmitterIcons pipeIcons = new TransmitterIcons(4, 1);

	public float currentScale;

	public FluidStack cacheFluid;
	public FluidStack lastWrite;

	public PartMechanicalPipe(Tier.PipeTier pipeTier)
	{
		tier = pipeTier;
	}

	@Override
	public void update()
	{
		if(!world().isRemote)
		{
			if(cacheFluid != null)
			{
				if(getTransmitterNetwork().fluidStored == null)
				{
					getTransmitterNetwork().fluidStored = cacheFluid;
				}
				else {
					getTransmitterNetwork().fluidStored.amount += cacheFluid.amount;
				}

				cacheFluid = null;
			}

			if(getTransmitterNetwork(false) != null && getTransmitterNetwork(false).getSize() > 0)
			{
				int last = lastWrite != null ? lastWrite.amount : 0;

				if(last != getSaveShare())
				{
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
							container.drain(side.getOpposite(), getTransmitterNetwork().emit(received, true), true);
						}
					}
				}
			}
		}

		super.update();
	}

	private int getSaveShare()
	{
		if(getTransmitterNetwork().fluidStored != null)
		{
			int remain = getTransmitterNetwork().fluidStored.amount%getTransmitterNetwork().transmitters.size();
			int toSave = getTransmitterNetwork().fluidStored.amount/getTransmitterNetwork().transmitters.size();

			if(getTransmitterNetwork().isFirst((IGridTransmitter<FluidNetwork>)tile()))
			{
				toSave += remain;
			}

			return toSave;
		}

		return 0;
	}

	@Override
	public void onChunkUnload()
	{
		if(!world().isRemote)
		{
			if(lastWrite != null)
			{
				if(getTransmitterNetwork().fluidStored != null)
				{
					getTransmitterNetwork().fluidStored.amount -= lastWrite.amount;

					if(getTransmitterNetwork().fluidStored.amount <= 0)
					{
						getTransmitterNetwork().fluidStored = null;
					}
				}
			}
		}

		super.onChunkUnload();
	}

	@Override
	public void preSingleMerge(FluidNetwork network)
	{
		if(cacheFluid != null)
		{
			if(network.fluidStored == null)
			{
				network.fluidStored = cacheFluid;
			}
			else {
				network.fluidStored.amount += cacheFluid.amount;
			}

			cacheFluid = null;
		}
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);

		if(nbtTags.hasKey("cacheFluid"))
		{
			cacheFluid = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cacheFluid"));
		}

		tier = Tier.PipeTier.values()[nbtTags.getInteger("tier")];
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);

		if(getTransmitterNetwork(false) != null && getTransmitterNetwork(false).getSize() > 0 && getTransmitterNetwork(false).fluidStored != null)
		{
			int remain = getTransmitterNetwork().fluidStored.amount%getTransmitterNetwork().transmitters.size();
			int toSave = getTransmitterNetwork().fluidStored.amount/getTransmitterNetwork().transmitters.size();

			if(getTransmitterNetwork().isFirst((IGridTransmitter<FluidNetwork>)tile()))
			{
				toSave += remain;
			}

			if(toSave > 0)
			{
				FluidStack stack = new FluidStack(getTransmitterNetwork().fluidStored.getFluid(), toSave);
				lastWrite = stack;
				nbtTags.setTag("cacheFluid", stack.writeToNBT(new NBTTagCompound()));
			}
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
		pipeIcons.registerCenterIcons(register, new String[] {"MechanicalPipeBasic", "MechanicalPipeAdvanced",
				"MechanicalPipeElite", "MechanicalPipeUltimate"});
		pipeIcons.registerSideIcons(register, new String[] {"MechanicalPipeSide"});
	}

	@Override
	public IIcon getCenterIcon()
	{
		return pipeIcons.getCenterIcon(tier.ordinal());
	}

	@Override
	public IIcon getSideIcon()
	{
		return pipeIcons.getSideIcon(0);
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.FLUID;
	}

	@Override
	public TransmitterType getTransmitter() 
	{ 
		return tier.type; 
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return PipeUtils.getConnections(tile())[side.ordinal()];
	}

	@Override
	public FluidNetwork createNetworkFromSingleTransmitter(IGridTransmitter<FluidNetwork> transmitter)
	{
		return new FluidNetwork(transmitter);
	}

	@Override
	public FluidNetwork createNetworkByMergingSet(Set<FluidNetwork> networks)
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
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(getConnectionType(from) == ConnectionType.NORMAL)
		{
			return getTransmitterNetwork().emit(resource, doFill);
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
			return new FluidTankInfo[] {dummyTank.getInfo()};
		}

		return new FluidTankInfo[0];
	}

	@Override
	public int getTransmitterNetworkSize()
	{
		return getTransmitterNetwork().getSize();
	}

	@Override
	public int getTransmitterNetworkAcceptorSize()
	{
		return getTransmitterNetwork().getAcceptorSize();
	}

	@Override
	public String getTransmitterNetworkNeeded()
	{
		return getTransmitterNetwork().getNeededInfo();
	}

	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlowInfo();
	}

	@Override
	public int getCapacity()
	{
		return tier.pipeCapacity;
	}

	public int getPullAmount()
	{
		return tier.pipePullAmount;
	}
}
