package mekanism.common.multipart;

import java.util.Arrays;
import java.util.Set;

import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.FluidNetwork;
import mekanism.common.PipeUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class PartMechanicalPipe extends PartTransmitter<FluidNetwork, FluidStack> implements IFluidHandler
{
	/** The fake tank used for fluid transfer calculations. */
	public FluidTank dummyTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
	
	/** The scale (0F -> 1F) of this pipe's fluid level. */
	public float fluidScale;

	@Override
	public String getType()
	{
		return "mekanism:mechanical_pipe";
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.FLUID;
	}
	
	public void clientUpdate(FluidStack fluidStack)
	{
		if(fluidStack.isFluidEqual(transmitting))
		{
			fluidScale = Math.min(1, fluidScale+((float)fluidStack.amount/50F));
		}
		else if(transmitting == null)
		{
			transmitting = fluidStack.copy();
			fluidScale += Math.min(1, ((float)fluidStack.amount/50F));
		}
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return tile instanceof IFluidHandler;
	}

	@Override
	public FluidNetwork createNetworkFromSingleTransmitter(ITransmitter<FluidNetwork, FluidStack> transmitter)
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
		RenderPartTransmitter.getInstance().renderContents(this, pos);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(!isActive)
		{
			return getTransmitterNetwork().emit(resource, doFill, Object3D.get(tile()).getFromSide(from).getTileEntity(world()));
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
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) 
	{
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) 
	{
		return new FluidTankInfo[] {dummyTank.getInfo()};
	}
	
	@Override
	public boolean doesTick()
	{
		return true;
	}

	@Override
	public void update()
	{
		if(world().isRemote)
		{
			if(fluidScale > 0)
			{
				fluidScale -= .01;
			}
			else {
				transmitting = null;
			}
		}	
		else {		
			if(isActive)
			{
				IFluidHandler[] connectedAcceptors = PipeUtils.getConnectedAcceptors(tile());
				
				for(IFluidHandler container : connectedAcceptors)
				{
					ForgeDirection side = ForgeDirection.getOrientation(Arrays.asList(connectedAcceptors).indexOf(container));
					
					if(container != null)
					{
						FluidStack received = container.drain(side, 100, false);
						
						if(received != null && received.amount != 0)
						{
							container.drain(side, getTransmitterNetwork().emit(received, true, Object3D.get(tile()).getFromSide(side).getTileEntity(world())), true);
						}
					}
				}
			}
		}
	}
}
