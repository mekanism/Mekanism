package mekanism.common.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.IConfigurable;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tank.TankUpdateProtocol;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import com.google.common.io.ByteArrayDataInput;

public class TileEntitySalinationController extends TileEntitySalinationTank implements IConfigurable
{
	public static final int MAX_BRINE = 1000;
	
	public static final int MAX_SOLARS = 4;
	public static final int WARMUP = 10000;

	public FluidTank waterTank = new FluidTank(0);
	public FluidTank brineTank = new FluidTank(MAX_BRINE);

	public Set<TileEntitySalinationTank> tankParts = new HashSet<TileEntitySalinationTank>();
	public TileEntityAdvancedSolarGenerator[] solars = new TileEntityAdvancedSolarGenerator[4];

	public boolean temperatureSet = false;
	
	public double partialWater = 0;
	public double partialBrine = 0;
	
	public float biomeTemp = 0;
	public float temperature = 0;
	
	public int height = 0;
	
	public boolean structured = false;
	public boolean controllerConflict = false;
	public boolean isLeftOnFace;
	
	public boolean updatedThisTick = false;

	public int clientSolarAmount;
	
	public boolean cacheStructure = false;
	
	public TileEntitySalinationController()
	{
		super("SalinationController");
		
		inventory = new ItemStack[4];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			updatedThisTick = false;
			
			if(ticker == 5 && cacheStructure)
			{
				refresh(true);
				cacheStructure = false;
			}
			
			updateTemperature();
	
			if(canOperate())
			{
				int brineNeeded = brineTank.getCapacity()-brineTank.getFluidAmount();
				int waterStored = waterTank.getFluidAmount();
				
				double waterUse = Math.min(brineTank.getFluidAmount(), getTemperature()*100);
				
				if(partialWater >= 1)
				{
					int waterInt = (int)Math.floor(partialWater);
					waterTank.drain(waterInt, true);
					partialWater %= 1;
					partialBrine += ((double)waterInt)/100D;
				}
				
				if(partialBrine >= 1)
				{
					int brineInt = (int)Math.floor(partialBrine);
					brineTank.fill(FluidRegistry.getFluidStack("brine", brineInt), true);
					partialBrine %= 1;
				}
			}
		}
	}
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		refresh(false);
	}
	
	@Override
	public void onNeighborChange(int id)
	{
		super.onNeighborChange(id);
		
		refresh(false);
	}
	
	protected void refresh(boolean canCreate)
	{
		if(!worldObj.isRemote)
		{
			if((structured || canCreate) && !updatedThisTick)
			{
				boolean prev = structured;
				
				clearStructure();
				structured = buildStructure();
				
				if(structured != prev)
				{
					PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this), 50D);
				}
				
				if(structured)
				{
					waterTank.setCapacity(getMaxWater());
					
					if(waterTank.getFluid() != null)
					{
						waterTank.getFluid().amount = Math.min(waterTank.getFluid().amount, getMaxWater());
					}
					
					temperature = Math.min(getMaxTemperature(), getTemperature());
				}
			}
		}
	}

	public boolean canOperate()
	{
		if(!structured || height < 3 || height > 18 || waterTank.getFluid() == null)
		{
			return false;
		}
		
		if(!waterTank.getFluid().containsFluid(FluidRegistry.getFluidStack("water", 1)) || brineTank.getCapacity()-brineTank.getFluidAmount() == 0)
		{
			return false;
		}
		
		return true;
	}
	
	public void updateTemperature()
	{
		float max = getMaxTemperature();
		float incr = (max/WARMUP)*getTempMultiplier();
		
		if(getTempMultiplier() == 0)
		{
			temperature = Math.max(0, getTemperature()-(incr*2));
		}
		else {
			temperature = Math.min(max, getTemperature()+incr);
		}
	}
	
	public float getTemperature()
	{
		return temperature;
	}
	
	public float getMaxTemperature()
	{
		if(!structured)
		{
			return 0;
		}
		
		return 1 + (height-3)*0.5F;
	}
	
	public float getTempMultiplier()
	{
		if(!temperatureSet)
		{
			biomeTemp = worldObj.getBiomeGenForCoordsBody(xCoord, zCoord).getFloatTemperature();
			temperatureSet = true;
		}
		
		return biomeTemp*((float)getActiveSolars()/MAX_SOLARS);
	}
	
	public int getActiveSolars()
	{
		if(worldObj.isRemote)
		{
			return clientSolarAmount;
		}
		
		int ret = 0;
		
		for(TileEntityAdvancedSolarGenerator solar : solars)
		{
			if(solar != null && solar.seesSun)
			{
				ret++;
			}
		}
		
		return ret;
	}

	public boolean buildStructure()
	{
		ForgeDirection right = MekanismUtils.getRight(facing);

		height = 0;
		controllerConflict = false;
		updatedThisTick = true;
		
		if(!scanBottomLayer())
		{
			height = 0;
			return false; 
		}
		
		Coord4D startPoint = Coord4D.get(this).getFromSide(right);
		startPoint = isLeftOnFace ? startPoint.getFromSide(right) : startPoint;

		int middle = 0;
		
		Coord4D middlePointer = startPoint.getFromSide(ForgeDirection.DOWN);
		
		while(scanMiddleLayer(middlePointer))
		{
			middlePointer = middlePointer.getFromSide(ForgeDirection.DOWN);
			middle++;
		}
		
		if(height < 3 || height > 18 || middle != height-2)
		{
			height = 0;
			return false;
		}

		structured = scanTopLayer(startPoint);
		height = structured ? height : 0;
		
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType().blockID);
		
		for(TileEntitySalinationTank tank : tankParts)
		{
			if(tank != this && tank instanceof TileEntitySalinationValve)
			{
				worldObj.notifyBlocksOfNeighborChange(tank.xCoord, tank.yCoord, tank.zCoord, tank.getBlockType().blockID);
			}
		}
		
		return structured;
	}

	public boolean scanTopLayer(Coord4D current)
	{
		ForgeDirection left = MekanismUtils.getLeft(facing);
		ForgeDirection back = MekanismUtils.getBack(facing);

		for(int x = 0; x < 4; x++)
		{
			for(int z = 0; z < 4; z++)
			{
				Coord4D pointer = current.getFromSide(left, x).getFromSide(back, z);
				
				int corner = getCorner(x, z);
				
				if(corner != -1)
				{
					if(addSolarPanel(pointer.getTileEntity(worldObj), corner))
					{
						continue;
					}
					else if(!addTankPart(pointer.getTileEntity(worldObj)))
					{
						return false;
					}
				}
				else {
					if((x == 1 || x == 2) && (z == 1 || z == 2))
					{
						if(!pointer.isAirBlock(worldObj))
						{
							return false;
						}
					}
					else {
						TileEntity pointerTile = pointer.getTileEntity(worldObj);
						
						if(!addTankPart(pointerTile))
						{
							return false;
						}
					}
				}
			}
		}

		return true;
	}
	
	public int getMaxWater()
	{
		return height*4*TankUpdateProtocol.FLUID_PER_TANK;
	}
	
	public int getCorner(int x, int z)
	{
		if(x == 0 && z == 0)
		{
			return 0;
		}
		else if(x == 0 && z == 3)
		{
			return 1;
		}
		else if(x == 3 && z == 0)
		{
			return 2;
		}
		else if(x == 3 && z == 3)
		{
			return 3;
		}
		
		return -1;
	}

	public boolean scanMiddleLayer(Coord4D current)
	{
		ForgeDirection left = MekanismUtils.getLeft(facing);
		ForgeDirection back = MekanismUtils.getBack(facing);

		for(int x = 0; x < 4; x++)
		{
			for(int z = 0; z < 4; z++)
			{
				Coord4D pointer = current.getFromSide(left, x).getFromSide(back, z);
				
				if((x == 1 || x == 2) && (z == 1 || z == 2))
				{
					if(!pointer.isAirBlock(worldObj))
					{
						return false;
					}
				}
				else {
					TileEntity pointerTile = pointer.getTileEntity(worldObj);
					
					if(!addTankPart(pointerTile)) 
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	public boolean scanBottomLayer()
	{
		height = 1;
		Coord4D baseBlock = Coord4D.get(this);
		
		while(baseBlock.getFromSide(ForgeDirection.DOWN).getTileEntity(worldObj) instanceof TileEntitySalinationTank)
		{
			baseBlock.step(ForgeDirection.DOWN);
			height++;
		}

		ForgeDirection left = MekanismUtils.getLeft(facing);
		ForgeDirection right = MekanismUtils.getRight(facing);

		if(!scanBottomRow(baseBlock)) 
		{
			return false;
		};
		
		if(!scanBottomRow(baseBlock.getFromSide(left))) 
		{
			return false;
		};
		
		if(!scanBottomRow(baseBlock.getFromSide(right))) 
		{
			return false;
		};

		boolean twoLeft = scanBottomRow(baseBlock.getFromSide(left).getFromSide(left));
		boolean twoRight = scanBottomRow(baseBlock.getFromSide(right).getFromSide(right));

		if(twoLeft == twoRight) 
		{
			return false;
		}

		isLeftOnFace = twoRight;
		
		return true;
	}

	/**
	 * Scans the bottom row of this multiblock, going in a line across the base.
	 * @param start
	 * @return
	 */
	public boolean scanBottomRow(Coord4D start)
	{
		ForgeDirection back = MekanismUtils.getBack(facing);
		Coord4D current = start;

		for(int i = 1; i <= 4; i++)
		{
			TileEntity tile = current.getTileEntity(worldObj);
			
			if(!addTankPart(tile)) 
			{
				return false;
			}
			
			current = current.getFromSide(back);
		}

		return true;
	}

	public boolean addTankPart(TileEntity tile)
	{
		if(tile instanceof TileEntitySalinationTank && (tile == this || !(tile instanceof TileEntitySalinationController)))
		{
			if(tile != this)
			{
				((TileEntitySalinationTank)tile).addToStructure(this);
				tankParts.add((TileEntitySalinationTank)tile);
			}
			
			return true;
		}
		else {
			if(tile != this && tile instanceof TileEntitySalinationController)
			{
				controllerConflict = true;
			}
			
			return false;
		}
	}

	public boolean addSolarPanel(TileEntity tile, int i)
	{
		if(tile instanceof TileEntityAdvancedSolarGenerator && !tile.isInvalid())
		{
			solars[i] = (TileEntityAdvancedSolarGenerator)tile;
			return true;
		}
		else {
			return false;
		}
	}
	
	public int getScaledWaterLevel(int i)
	{
		return waterTank.getFluid() != null ? waterTank.getFluid().amount*i / 10000 : 0;
	}
	
	public int getScaledBrineLevel(int i)
	{
		return brineTank.getFluid() != null ? brineTank.getFluid().amount*i / 10000 : 0;
	}
	
	public int getScaledTempLevel(int i)
	{
		return (int)(getMaxTemperature() == 0 ? 0 : getTemperature()*i/getMaxTemperature());
	}

	@Override
	public boolean onSneakRightClick(EntityPlayer player, int side)
	{
		return false;
	}

	@Override
	public boolean onRightClick(EntityPlayer player, int side)
	{
		return false;
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		if(!worldObj.isRemote)
		{
			int type = dataStream.readInt();
			
			if(type == 0)
			{
				refresh(true);
			}
			
			return;
		}
		
		super.handlePacketData(dataStream);
		
		if(dataStream.readBoolean())
		{
			waterTank.setFluid(new FluidStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			waterTank.setFluid(null);
		}
		
		if(dataStream.readBoolean())
		{
			brineTank.setFluid(new FluidStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			brineTank.setFluid(null);
		}
		
		boolean prev = structured;
		
		structured = dataStream.readBoolean();
		controllerConflict = dataStream.readBoolean();
		clientSolarAmount = dataStream.readInt();
		height = dataStream.readInt();
		temperature = dataStream.readFloat();
		biomeTemp = dataStream.readFloat();
		
		if(structured != prev)
		{
			worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
		}
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(waterTank.getFluid() != null)
		{
			data.add(true);
			data.add(waterTank.getFluid().fluidID);
			data.add(waterTank.getFluid().amount);
		}
		else {
			data.add(false);
		}
		
		if(brineTank.getFluid() != null)
		{
			data.add(true);
			data.add(brineTank.getFluid().fluidID);
			data.add(brineTank.getFluid().amount);
		}
		else {
			data.add(false);
		}
		
		data.add(structured);
		data.add(controllerConflict);
		data.add(getActiveSolars());
		data.add(height);
		data.add(temperature);
		data.add(biomeTemp);
		
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        waterTank.readFromNBT(nbtTags.getCompoundTag("waterTank"));
        brineTank.readFromNBT(nbtTags.getCompoundTag("brineTank"));
        
        temperature = nbtTags.getFloat("temperature");
        
        partialWater = nbtTags.getDouble("partialWater");
        partialBrine = nbtTags.getDouble("partialBrine");
        
        cacheStructure = nbtTags.getBoolean("cacheStructure");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setCompoundTag("waterTank", waterTank.writeToNBT(new NBTTagCompound()));
        nbtTags.setCompoundTag("brineTank", brineTank.writeToNBT(new NBTTagCompound()));
        
        nbtTags.setFloat("temperature", temperature);
        
        nbtTags.setDouble("partialWater", partialWater);
        nbtTags.setDouble("partialBrine", partialBrine);
        
        nbtTags.setBoolean("cacheStructure", structured);
    }

	public void clearStructure()
	{
		for(TileEntitySalinationTank tankPart : tankParts)
		{
			tankPart.controllerGone();
		}
		
		tankParts.clear();
		solars = new TileEntityAdvancedSolarGenerator[] {null, null, null, null};
	}
}
