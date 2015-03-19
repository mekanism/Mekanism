package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.common.Mekanism;
import mekanism.common.content.matrix.MatrixUpdateProtocol;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;

public class TileEntityInductionCasing extends TileEntityMultiblock<SynchronizedMatrixData> implements IStrictEnergyStorage
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
				structure.lastOutput = structure.outputCap-structure.remainingOutput;
				structure.remainingOutput = structure.outputCap;
				
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
			data.add(structure.outputCap);
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
			structure.outputCap = dataStream.readDouble();
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
		return MekanismUtils.localize("gui.inductionMatrix");
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
}
