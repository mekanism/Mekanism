package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.ISalinationSolar;
import mekanism.api.Range4D;
import mekanism.client.SparkleAnimation.INodeChecker;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ITankManager;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.inputs.FluidInput;
import mekanism.common.recipe.machines.SolarEvaporationRecipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntitySolarEvaporationController extends TileEntitySolarEvaporationBlock implements IActiveState, ITankManager
{
	public static final int MAX_OUTPUT = 10000;
	public static final int MAX_SOLARS = 4;
	public static final int WARMUP = 10000;

	public FluidTank inputTank = new FluidTank(0);
	public FluidTank outputTank = new FluidTank(MAX_OUTPUT);

	public Set<TileEntitySolarEvaporationBlock> tankParts = new HashSet<TileEntitySolarEvaporationBlock>();
	public ISalinationSolar[] solars = new ISalinationSolar[4];

	public boolean temperatureSet = false;
	
	public double partialInput = 0;
	public double partialOutput = 0;
	
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
	
	public TileEntitySolarEvaporationController()
	{
		super("SolarEvaporationController");
		
		inventory = new ItemStack[4];
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			updatedThisTick = false;
			
			if(ticker == 5)
			{
				refresh();
			}
			
			updateTemperature();
			manageBuckets();
			
			SolarEvaporationRecipe recipe = getRecipe();
	
			if(canOperate(recipe))
			{
				int outputNeeded = outputTank.getCapacity()-outputTank.getFluidAmount();
				int inputStored = inputTank.getFluidAmount();
				
				partialInput += Math.min(inputTank.getFluidAmount(), getTemperature()*recipe.recipeInput.ingredient.amount);
				
				if(partialInput >= 1)
				{
					int inputInt = (int)Math.floor(partialInput);
					inputTank.drain(inputInt, true);
					partialInput %= 1;
					partialOutput += ((double)inputInt)/recipe.recipeInput.ingredient.amount;
				}
				
				if(partialOutput >= 1)
				{
					int outputInt = (int)Math.floor(partialOutput);
					outputTank.fill(new FluidStack(recipe.recipeOutput.output.getFluid(), outputInt), true);
					partialOutput %= 1;
				}
			}
			
			if(structured)
			{
				if(Math.abs((float)inputTank.getFluidAmount()/inputTank.getCapacity()-prevScale) > 0.01)
				{
					Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(this)));
					prevScale = (float)inputTank.getFluidAmount()/inputTank.getCapacity();
				}
			}
		}
	}
	
	public SolarEvaporationRecipe getRecipe()
	{
		return RecipeHandler.getSolarEvaporationRecipe(new FluidInput(inputTank.getFluid()));
	}
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		
		refresh();
	}
	
	@Override
	public void onNeighborChange(Block block)
	{
		super.onNeighborChange(block);
		
		refresh();
	}
	
	public boolean hasRecipe(Fluid fluid)
	{
		if(fluid == null)
		{
			return false;
		}
		
		return Recipe.SOLAR_EVAPORATION_PLANT.containsRecipe(fluid);
	}
	
	protected void refresh()
	{
		if(!worldObj.isRemote)
		{
			if(!updatedThisTick)
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
					inputTank.setCapacity(getMaxFluid());
					
					if(inputTank.getFluid() != null)
					{
						inputTank.getFluid().amount = Math.min(inputTank.getFluid().amount, getMaxFluid());
					}
					
					temperature = Math.min(getMaxTemperature(), getTemperature());
				}
				else {
					clearStructure();
				}
			}
		}
	}

	public boolean canOperate(SolarEvaporationRecipe recipe)
	{
		if(!structured || height < 3 || height > 18 || inputTank.getFluid() == null || getTempMultiplier() == 0)
		{
			return false;
		}
		
		if(recipe != null && recipe.canOperate(inputTank, outputTank))
		{
			return true;
		}
		
		return false;
	}
	
	private void manageBuckets()
	{
		if(inventory[2] != null)
		{
			if(outputTank.getFluid() != null && outputTank.getFluid().amount >= FluidContainerRegistry.BUCKET_VOLUME)
			{
				if(FluidContainerRegistry.isEmptyContainer(inventory[2]))
				{
					ItemStack tempStack = FluidContainerRegistry.fillFluidContainer(outputTank.getFluid(), inventory[2]);
					
					if(tempStack != null)
					{
						if(inventory[3] == null)
						{
							outputTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
							
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
							outputTank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
							
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
				
				if((inputTank.getFluid() == null && itemFluid.amount <= getMaxFluid()) || inputTank.getFluid().amount+itemFluid.amount <= getMaxFluid())
				{
					if(!hasRecipe(itemFluid.getFluid()) || (inputTank.getFluid() != null && !inputTank.getFluid().isFluidEqual(itemFluid)))
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
						inputTank.fill(itemFluid, true);
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
		
		while(startPoint.getFromSide(ForgeDirection.UP).getTileEntity(worldObj) instanceof TileEntitySolarEvaporationBlock)
		{
			startPoint.step(ForgeDirection.UP);
		}

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
		
		markDirty();
		
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
	
	public int getMaxFluid()
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
		
		while(baseBlock.getFromSide(ForgeDirection.DOWN).getTileEntity(worldObj) instanceof TileEntitySolarEvaporationBlock)
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
		if(tile instanceof TileEntitySolarEvaporationBlock && (tile == this || !(tile instanceof TileEntitySolarEvaporationController)))
		{
			if(tile != this)
			{
				((TileEntitySolarEvaporationBlock)tile).addToStructure(this);
				tankParts.add((TileEntitySolarEvaporationBlock)tile);
			}
			
			return true;
		}
		else {
			if(tile != this && tile instanceof TileEntitySolarEvaporationController)
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
	
	public int getScaledInputLevel(int i)
	{
		return getMaxFluid() > 0 ? (inputTank.getFluid() != null ? inputTank.getFluid().amount*i / getMaxFluid() : 0) : 0;
	}
	
	public int getScaledOutputLevel(int i)
	{
		return outputTank.getFluid() != null ? outputTank.getFluid().amount*i / MAX_OUTPUT : 0;
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
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(dataStream.readBoolean())
		{
			inputTank.setFluid(new FluidStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			inputTank.setFluid(null);
		}
		
		if(dataStream.readBoolean())
		{
			outputTank.setFluid(new FluidStack(dataStream.readInt(), dataStream.readInt()));
		}
		else {
			outputTank.setFluid(null);
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
			inputTank.setCapacity(getMaxFluid());
			worldObj.func_147479_m(xCoord, yCoord, zCoord);
			
			if(structured)
			{
				Mekanism.proxy.doGenericSparkle(this, new INodeChecker() {
					@Override
					public boolean isNode(TileEntity tile)
					{
						return tile instanceof TileEntitySolarEvaporationBlock;
					}
				});
			}
		}
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(inputTank.getFluid() != null)
		{
			data.add(true);
			data.add(inputTank.getFluid().fluidID);
			data.add(inputTank.getFluid().amount);
		}
		else {
			data.add(false);
		}
		
		if(outputTank.getFluid() != null)
		{
			data.add(true);
			data.add(outputTank.getFluid().fluidID);
			data.add(outputTank.getFluid().amount);
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

        inputTank.readFromNBT(nbtTags.getCompoundTag("waterTank"));
        outputTank.readFromNBT(nbtTags.getCompoundTag("brineTank"));
        
        temperature = nbtTags.getFloat("temperature");
        
        partialInput = nbtTags.getDouble("partialWater");
        partialOutput = nbtTags.getDouble("partialBrine");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setTag("waterTank", inputTank.writeToNBT(new NBTTagCompound()));
        nbtTags.setTag("brineTank", outputTank.writeToNBT(new NBTTagCompound()));
        
        nbtTags.setFloat("temperature", temperature);
        
        nbtTags.setDouble("partialWater", partialInput);
        nbtTags.setDouble("partialBrine", partialOutput);
    }
	
	@Override
	public boolean canSetFacing(int side)
	{
		return side != 0 && side != 1;
	}

	public void clearStructure()
	{
		for(TileEntitySolarEvaporationBlock tankPart : tankParts)
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

	@Override
	public boolean getActive()
	{
		return structured;
	}

	@Override
	public void setActive(boolean active)
	{

	}

	@Override
	public boolean renderUpdate()
	{
		return false;
	}

	@Override
	public boolean lightUpdate()
	{
		return false;
	}
	
	@Override
	public Object[] getTanks() 
	{
		return new Object[] {inputTank, outputTank};
	}
}
