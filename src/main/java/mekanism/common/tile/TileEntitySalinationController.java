package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IConfigurable;
import mekanism.api.ISalinationSolar;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tank.TankUpdateProtocol;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntitySalinationController extends TileEntitySalinationTank implements IConfigurable
{
	public static final int MAX_BRINE = 10000;
	public static final int MAX_SOLARS = 4;
	public static final int WARMUP = 10000;
	public static final double WATER_USAGE = 40;

	public FluidTank waterTank = new FluidTank(0);
	public FluidTank brineTank = new FluidTank(MAX_BRINE);

	public Set<TileEntitySalinationTank> tankParts = new HashSet<TileEntitySalinationTank>();
	public ISalinationSolar[] solars = new ISalinationSolar[4];

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
	
	public float prevScale;
	
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
			manageBuckets();
	
			if(canOperate())
			{
				int brineNeeded = brineTank.getCapacity()-brineTank.getFluidAmount();
				int waterStored = waterTank.getFluidAmount();
				
				partialWater += Math.min(waterTank.getFluidAmount(), getTemperature()*WATER_USAGE);
				
				if(partialWater >= 1)
				{
					int waterInt = (int)Math.floor(partialWater);
					waterTank.drain(waterInt, true);
					partialWater %= 1;
					partialBrine += ((double)waterInt)/WATER_USAGE;
				}
				
				if(partialBrine >= 1)
				{
					int brineInt = (int)Math.floor(partialBrine);
					brineTank.fill(FluidRegistry.getFluidStack("brine", brineInt), true);
					partialBrine %= 1;
				}
			}
			
			if(structured)
			{
				if(Math.abs((float)waterTank.getFluidAmount()/waterTank.getCapacity()-prevScale) > 0.01)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
					prevScale = (float)waterTank.getFluidAmount()/waterTank.getCapacity();
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
	public void onNeighborChange(Block block)
	{
		super.onNeighborChange(block);
		
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
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
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
		if(!structured || height < 3 || height > 18 || waterTank.getFluid() == null || getTempMultiplier() == 0)
		{
			return false;
		}
		
		if(!waterTank.getFluid().containsFluid(FluidRegistry.getFluidStack("water", 1)) || brineTank.getCapacity()-brineTank.getFluidAmount() == 0)
		{
			return false;
		}
		
		return true;
	}
	
	private void manageBuckets()
	{
		if(inventory[2] != null)
		{
			if(brineTank.getFluid() != null && brineTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
			{
				if(FluidContainerRegistry.isEmptyContainer(inventory[2]))
				{
					ItemStack tempStack = FluidContainerRegistry.fillFluidContainer(brineTank.getFluid(), inventory[2]);
					
					if(tempStack != null)
					{
						if(inventory[3] == null)
						{
							brineTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
							
							inventory[3] = tempStack;
							inventory[2].stackSize--;
							
							if(inventory[2].stackSize <= 0)
							{
								inventory[2] = null;
							}
							
							markDirty();
						}
						else if(tempStack.isItemEqual(inventory[3]) && tempStack.getMaxStackSize() > inventory[3].stackSize)
						{
							brineTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
							
							inventory[3].stackSize++;
							inventory[2].stackSize--;
							
							if(inventory[2].stackSize <= 0)
							{
								inventory[2] = null;
							}
							
							markDirty();
						}
					}
				}
			}
		}
		
		if(structured)
		{
			if(FluidContainerRegistry.isFilledContainer(inventory[0]))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(inventory[0]);
				
				if((waterTank.getFluid() == null && itemFluid.amount <= getMaxWater()) || waterTank.getFluid().amount+itemFluid.amount <= getMaxWater())
				{
					if(itemFluid.getFluid() != FluidRegistry.WATER || (waterTank.getFluid() != null && !waterTank.getFluid().isFluidEqual(itemFluid)))
					{
						return;
					}
					
					ItemStack containerItem = inventory[0].getItem().getContainerItem(inventory[0]);
					
					boolean filled = false;
					
					if(containerItem != null)
					{
						if(inventory[1] == null || (inventory[1].isItemEqual(containerItem) && inventory[1].stackSize+1 <= containerItem.getMaxStackSize()))
						{
							inventory[0] = null;
							
							if(inventory[1] == null)
							{
								inventory[1] = containerItem;
							}
							else {
								inventory[1].stackSize++;
							}
							
							filled = true;
						}
					}
					else {						
						inventory[0].stackSize--;
						
						if(inventory[0].stackSize == 0)
						{
							inventory[0] = null;
						}
						
						filled = true;
					}
					
					if(filled)
					{
						waterTank.fill(itemFluid, true);
					}
				}
			}
		}
	}
	
	private void updateTemperature()
	{
		float max = getMaxTemperature();
		float incr = (max/WARMUP)*getTempMultiplier();
		
		if(getTempMultiplier() == 0)
		{
			temperature = Math.max(0, getTemperature()-(max/WARMUP));
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
			biomeTemp = worldObj.getBiomeGenForCoordsBody(xCoord, zCoord).getFloatTemperature(xCoord, yCoord, zCoord);
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
		
		for(ISalinationSolar solar : solars)
		{
			if(solar != null && solar.seesSun())
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
		
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		
		for(TileEntitySalinationTank tank : tankParts)
		{
			if(tank != this && tank instanceof TileEntitySalinationValve)
			{
				worldObj.notifyBlocksOfNeighborChange(tank.xCoord, tank.yCoord, tank.zCoord, tank.getBlockType());
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
		if(tile instanceof ISalinationSolar && !tile.isInvalid())
		{
			solars[i] = (ISalinationSolar)tile;
			return true;
		}
		else {
			return false;
		}
	}
	
	public int getScaledWaterLevel(int i)
	{
		return getMaxWater() > 0 ? (waterTank.getFluid() != null ? waterTank.getFluid().amount*i / getMaxWater() : 0) : 0;
	}
	
	public int getScaledBrineLevel(int i)
	{
		return brineTank.getFluid() != null ? brineTank.getFluid().amount*i / MAX_BRINE : 0;
	}
	
	public int getScaledTempLevel(int i)
	{
		return (int)(getMaxTemperature() == 0 ? 0 : getTemperature()*i/getMaxTemperature());
	}
	
	public Coord4D getRenderLocation()
	{
		if(!structured)
		{
			return null;
		}
		
		ForgeDirection right = MekanismUtils.getRight(facing);
		Coord4D startPoint = Coord4D.get(this).getFromSide(right);
		startPoint = isLeftOnFace ? startPoint.getFromSide(right) : startPoint;
		
		startPoint = startPoint.getFromSide(right.getOpposite()).getFromSide(MekanismUtils.getBack(facing));
		startPoint.translate(0, -(height-2), 0);
		
		return startPoint;
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
	public void handlePacketData(ByteBuf dataStream)
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
		isLeftOnFace = dataStream.readBoolean();
		
		if(structured != prev)
		{
			waterTank.setCapacity(getMaxWater());
			worldObj.func_147479_m(xCoord, yCoord, zCoord);
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
		data.add(isLeftOnFace);
		
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
        
        nbtTags.setTag("waterTank", waterTank.writeToNBT(new NBTTagCompound()));
        nbtTags.setTag("brineTank", brineTank.writeToNBT(new NBTTagCompound()));
        
        nbtTags.setFloat("temperature", temperature);
        
        nbtTags.setDouble("partialWater", partialWater);
        nbtTags.setDouble("partialBrine", partialBrine);
        
        nbtTags.setBoolean("cacheStructure", structured);
    }
	
	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	public void clearStructure()
	{
		for(TileEntitySalinationTank tankPart : tankParts)
		{
			tankPart.controllerGone();
		}
		
		tankParts.clear();
		solars = new ISalinationSolar[] {null, null, null, null};
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
	}
}
