package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.content.matrix.MatrixCache;
import mekanism.common.content.matrix.MatrixUpdateProtocol;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.integration.IComputerIntegration;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

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
	public ArrayList getNetworkedData(ArrayList data)
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
	public String getInventoryName()
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
			return structure.clientEnergy;
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

	public static final String[] methods = new String[] {"getStored", "getMaxEnergy", "getInput", "getOutput", "getTransferCap"};

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
}
