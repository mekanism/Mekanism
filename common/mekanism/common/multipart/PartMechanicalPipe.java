package mekanism.common.multipart;

import java.util.Arrays;
import java.util.Set;

import mekanism.api.Object3D;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.PartTransmitterIcons;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.FluidNetwork;
import mekanism.common.PipeUtils;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
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
	/** The fake tank used for fluid transfer calculations. */
	public FluidTank dummyTank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME);
    public static PartTransmitterIcons pipeIcons;

	@Override
	public String getType()
	{
		return "mekanism:mechanical_pipe";
	}

    public static void registerIcons(IconRegister register){
        pipeIcons = new PartTransmitterIcons(2);
        pipeIcons.registerCenterIcons(register, new String[]{"MechanicalPipe", "MechanicalPipeActive"});
        pipeIcons.registerSideIcon(register, "MechanicalPipeSide");
    }

    @Override
    public Icon getCenterIcon()
    {
        return pipeIcons.getCenterIcon(isActive ? 1 : 0);
    }

    @Override
    public Icon getSideIcon()
    {
        return pipeIcons.getSideIcon();
    }

    @Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.FLUID;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return tile instanceof IFluidHandler;
	}

	@Override
	public FluidNetwork createNetworkFromSingleTransmitter(ITransmitter<FluidNetwork> transmitter)
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
	public void update()
	{
		if(!world().isRemote)
		{
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
			super.update();
		}
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
		return getTransmitterNetwork().getNeeded();
	}
	
	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlow();
	}
}
