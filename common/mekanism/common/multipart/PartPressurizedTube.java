package mekanism.common.multipart;

import java.util.Set;

import mekanism.api.gas.GasNetwork;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.PartTransmitterIcons;
import mekanism.client.render.RenderPartTransmitter;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartPressurizedTube extends PartTransmitter<GasNetwork>
{
    public static PartTransmitterIcons tubeIcons;
    
    public GasStack cacheGas;
    
    @Override
    public void update()
    {
    	super.update();
    	
    	if(!world().isRemote)
    	{
    		if(cacheGas != null)
    		{
	    		if(getTransmitterNetwork().gasStored == null)
	    		{
	    			getTransmitterNetwork().gasStored = cacheGas;
	    		}
	    		else {
	    			getTransmitterNetwork().gasStored.amount += cacheGas.amount;
	    		}
	    		
	    		cacheGas = null;
    		}
    	}
    }
    
    @Override
    public void load(NBTTagCompound nbtTags)
    {
    	super.load(nbtTags);
    	
    	if(nbtTags.hasKey("cacheGas"))
    	{
    		cacheGas = GasStack.readFromNBT(nbtTags.getCompoundTag("cacheGas"));
    	}
    }
    
    @Override
    public void save(NBTTagCompound nbtTags)
    {
    	super.save(nbtTags);
    	
    	if(getTransmitterNetwork().gasStored != null)
    	{
	    	int remains = getTransmitterNetwork().gasStored.amount%(int)getTransmitterNetwork().getMeanCapacity();
	    	int toSave = (getTransmitterNetwork().gasStored.amount-remains)/(int)getTransmitterNetwork().getMeanCapacity();
	    	toSave += remains;
	    	
	    	GasStack stack = new GasStack(getTransmitterNetwork().gasStored.getGas(), toSave);
	    	
	    	getTransmitterNetwork().gasStored.amount -= toSave;
	    	nbtTags.setCompoundTag("cacheGas", stack.write(new NBTTagCompound()));
    	}
    }
    
    @Override
    public boolean isConnectable(TileEntity tileEntity)
    {
    	if(tileEntity instanceof ITransmitter && TransmissionType.checkTransmissionType(tileEntity, getTransmissionType()))
    	{
    		ITransmitter<GasNetwork> transmitter = (ITransmitter<GasNetwork>)tileEntity;
    		
    		if(getTransmitterNetwork().gasStored == null || transmitter.getTransmitterNetwork().gasStored == null)
    		{
    			return true;
    		}
    		else if(getTransmitterNetwork().gasStored.getGas() == transmitter.getTransmitterNetwork().gasStored.getGas())
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }

	@Override
	public String getType()
	{
		return "mekanism:pressurized_tube";
	}

    public static void registerIcons(IconRegister register)
    {
        tubeIcons = new PartTransmitterIcons(1);
        tubeIcons.registerCenterIcons(register, new String[] {"PressurizedTube"});
        tubeIcons.registerSideIcon(register, "TransmitterSideSmall");
    }

    @Override
    public Icon getCenterIcon()
    {
        return tubeIcons.getCenterIcon(0);
    }

    @Override
    public Icon getSideIcon()
    {
        return tubeIcons.getSideIcon();
    }

    @Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.GAS;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return tile instanceof IGasHandler;
	}

	@Override
	public GasNetwork createNetworkFromSingleTransmitter(ITransmitter<GasNetwork> transmitter)
	{
		return new GasNetwork(transmitter);
	}

	@Override
	public GasNetwork createNetworkByMergingSet(Set<GasNetwork> networks)
	{
		return new GasNetwork(networks);
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
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float f, int pass)
	{
		RenderPartTransmitter.getInstance().renderContents(this, pos);
	}

    @Override
    public int getCapacity()
    {
        return 256;
    }
}
