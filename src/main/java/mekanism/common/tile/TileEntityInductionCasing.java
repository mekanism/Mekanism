package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.matrix.MatrixCache;
import mekanism.common.content.matrix.MatrixUpdateProtocol;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.integration.IComputerIntegration;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityInductionCasing extends TileEntityMultiblock<SynchronizedMatrixData> implements IStrictEnergyStorage, IComputerIntegration
{
	public int clientCells;
	public int clientProviders;
	
	public TileEntityInductionCasing() 
	{
		this("InductionCasing");
	}
	
	public TileEntityInductionCasing(String name)
	{
		super(name);
		inventory = new ItemStack[2];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			if(structure != null && isRendering)
			{
				structure.lastInput = structure.transferCap-structure.remainingInput;
				structure.remainingInput = structure.transferCap;
				
				structure.lastOutput = structure.transferCap-structure.remainingOutput;
				structure.remainingOutput = structure.transferCap;
				
				ChargeUtils.charge(0, this);
				ChargeUtils.discharge(1, this);
			}
		}
	}
	
	@Override
	public boolean onActivate(EntityPlayer player)
	{
		if(!player.isSneaking() && structure != null)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList<Object>())), new Range4D(Coord4D.get(this)));
			player.openGui(Mekanism.instance, 49, worldObj, getPos().getX(), getPos().getY(), getPos().getZ());
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public ArrayList<Object> getNetworkedData(ArrayList<Object> data)
	{
		super.getNetworkedData(data);
		
		if(structure != null)
		{
			data.add(structure.getEnergy(worldObj));
			data.add(structure.storageCap);
			data.add(structure.transferCap);
			data.add(structure.lastInput);
			data.add(structure.lastOutput);
			
			data.add(structure.volWidth);
			data.add(structure.volHeight);
			data.add(structure.volLength);
			
			data.add(structure.cells.size());
			data.add(structure.providers.size());
		}
		
		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(worldObj.isRemote)
		{
			if(clientHasStructure)
			{
				structure.clientEnergy = dataStream.readDouble();
				structure.storageCap = dataStream.readDouble();
				structure.transferCap = dataStream.readDouble();
				structure.lastInput = dataStream.readDouble();
				structure.lastOutput = dataStream.readDouble();
				
				structure.volWidth = dataStream.readInt();
				structure.volHeight = dataStream.readInt();
				structure.volLength = dataStream.readInt();
				
				clientCells = dataStream.readInt();
				clientProviders = dataStream.readInt();
			}
		}
	}

	@Override
	protected SynchronizedMatrixData getNewStructure() 
	{
		return new SynchronizedMatrixData();
	}
	
	@Override
	public MatrixCache getNewCache()
	{
		return new MatrixCache();
	}

	@Override
	protected MatrixUpdateProtocol getProtocol() 
	{
		return new MatrixUpdateProtocol(this);
	}

	@Override
	public MultiblockManager<SynchronizedMatrixData> getManager() 
	{
		return Mekanism.matrixManager;
	}
	
	@Override
	public String getName()
	{
		return LangUtils.localize("gui.inductionMatrix");
	}
	
	public int getScaledEnergyLevel(int i)
	{
		return (int)(getEnergy()*i / getMaxEnergy());
	}

	@Override
	public double getEnergy()
	{
		if(!worldObj.isRemote)
		{
			return structure != null ? structure.getEnergy(worldObj) : 0;
		}
		else {
			return structure != null ? structure.clientEnergy : 0;
		}
	}

	@Override
	public void setEnergy(double energy)
	{
		if(structure != null)
		{
			structure.setEnergy(worldObj, Math.max(Math.min(energy, getMaxEnergy()), 0));
			MekanismUtils.saveChunk(this);
		}
	}

	@Override
	public double getMaxEnergy()
	{
		return structure != null ? structure.storageCap : 0;
	}

	public static final String[] methods = new String[] {"getEnergy", "getMaxEnergy", "getInput", "getOutput", "getTransferCap"};

	@Override
	public String[] getMethods()
	{
		return methods;
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws Exception
	{
		if(structure == null)
		{
			return new Object[] {"Unformed."};
		}

		switch(method)
		{
			case 0:
				return new Object[] {getEnergy()};
			case 1:
				return new Object[] {getMaxEnergy()};
			case 2:
				return new Object[] {structure.lastInput};
			case 3:
				return new Object[] {structure.lastOutput};
			case 4:
				return new Object[] {structure.transferCap};
			default:
				return new Object[] {"Unknown command."};
		}
	}

	@Override
	public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, net.minecraft.util.EnumFacing facing)
	{
		return capability == Capabilities.ENERGY_STORAGE_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, net.minecraft.util.EnumFacing facing)
	{
		if (capability == Capabilities.ENERGY_STORAGE_CAPABILITY)
			return (T) this;
		return super.getCapability(capability, facing);
	}
}
