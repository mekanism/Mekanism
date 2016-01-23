package mekanism.generators.common.tile.turbine;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import mekanism.generators.common.content.turbine.TurbineCache;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityTurbineCasing extends TileEntityMultiblock<SynchronizedTurbineData>
{
	public int clientCapacity;

	public float prevScale;
	
	public TileEntityTurbineCasing() 
	{
		this("TurbineCasing");
	}
	
	public TileEntityTurbineCasing(String name)
	{
		super(name);
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(worldObj.isRemote)
		{
			if(structure != null && clientHasStructure && isRendering)
			{
				float targetScale = (float)(structure.fluidStored != null ? structure.fluidStored.amount : 0)/clientCapacity;

				if(Math.abs(prevScale - targetScale) > 0.01)
				{
					prevScale = (9*prevScale + targetScale)/10;
				}
			}
		}

		if(!worldObj.isRemote)
		{
			if(structure != null)
			{
				if(structure.fluidStored != null && structure.fluidStored.amount <= 0)
				{
					structure.fluidStored = null;
					markDirty();
				}
				
				if(isRendering)
				{
					if(structure.needsRenderUpdate())
					{
						sendPacketToRenderer();
					}
					
					structure.prevFluid = structure.fluidStored;
				}
			}
		}
	}
	
	@Override
	public boolean onActivate(EntityPlayer player)
	{
		if(!player.isSneaking() && structure != null)
		{
			Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
			//player.openGui(Mekanism.instance, 49, worldObj, xCoord, yCoord, zCoord);
			
			return true;
		}
		
		return false;
	}
	
	public int getScaledFluidLevel(int i)
	{
		if(clientCapacity == 0 || structure.fluidStored == null)
		{
			return 0;
		}

		return structure.fluidStored.amount*i / clientCapacity;
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(structure != null)
		{
			data.add(structure.getFluidCapacity());
			
			if(structure.fluidStored != null)
			{
				data.add(1);
				data.add(structure.fluidStored.getFluidID());
				data.add(structure.fluidStored.amount);
			}
			else {
				data.add(0);
			}
		}

		return data;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(clientHasStructure)
		{
			clientCapacity = dataStream.readInt();
			
			if(dataStream.readInt() == 1)
			{
				structure.fluidStored = new FluidStack(FluidRegistry.getFluid(dataStream.readInt()), dataStream.readInt());
			}
			else {
				structure.fluidStored = null;
			}
		}
	}

	@Override
	protected SynchronizedTurbineData getNewStructure() 
	{
		return new SynchronizedTurbineData();
	}

	@Override
	public MultiblockCache<SynchronizedTurbineData> getNewCache() 
	{
		return new TurbineCache();
	}

	@Override
	protected UpdateProtocol<SynchronizedTurbineData> getProtocol() 
	{
		return new TurbineUpdateProtocol(this);
	}

	@Override
	public MultiblockManager<SynchronizedTurbineData> getManager() 
	{
		return MekanismGenerators.turbineManager;
	}
}
