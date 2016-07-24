package mekanism.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IEvaporationSolar;
import mekanism.api.MekanismConfig.general;
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
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.FluidChecker;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidContainerItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityThermalEvaporationController extends TileEntityThermalEvaporationBlock implements IActiveState, ITankManager
{
	public static final int MAX_OUTPUT = 10000;
	public static final int MAX_SOLARS = 4;
	public static final int MAX_HEIGHT = 18;

	public FluidTank inputTank = new FluidTank(0);
	public FluidTank outputTank = new FluidTank(MAX_OUTPUT);

	public Set<Coord4D> tankParts = new HashSet<Coord4D>();
	public IEvaporationSolar[] solars = new IEvaporationSolar[4];

	public boolean temperatureSet = false;
	
	public double partialInput = 0;
	public double partialOutput = 0;
	
	public float biomeTemp = 0;
	public float temperature = 0;
	public float heatToAbsorb = 0;
	
	public float lastGain = 0;
	
	public int height = 0;
	
	public boolean structured = false;
	public boolean controllerConflict = false;
	public boolean isLeftOnFace;
	public int renderY;
	
	public boolean updatedThisTick = false;

	public int clientSolarAmount;
	
	public boolean cacheStructure = false;
	
	public float prevScale;
	
	public float totalLoss = 0;
	
	public TileEntityThermalEvaporationController()
	{
		super("ThermalEvaporationController");
		
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
			
			if(structured)
			{
				updateTemperature();
			}
			
			manageBuckets();
			
			ThermalEvaporationRecipe recipe = getRecipe();
	
			if(canOperate(recipe))
			{
				int outputNeeded = outputTank.getCapacity()-outputTank.getFluidAmount();
				int inputStored = inputTank.getFluidAmount();
				double outputRatio = (double)recipe.recipeOutput.output.amount/(double)recipe.recipeInput.ingredient.amount;
				
				double tempMult = Math.max(0, getTemperature())*general.evaporationTempMultiplier;
				double inputToUse = (tempMult*recipe.recipeInput.ingredient.amount)*((float)height/(float)MAX_HEIGHT);
				inputToUse = Math.min(inputTank.getFluidAmount(), inputToUse);
				inputToUse = Math.min(inputToUse, outputNeeded/outputRatio);
				
				lastGain = (float)inputToUse/(float)recipe.recipeInput.ingredient.amount;
				partialInput += inputToUse;
				
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
			else {
				lastGain = 0;
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
	
	public ThermalEvaporationRecipe getRecipe()
	{
		return RecipeHandler.getThermalEvaporationRecipe(new FluidInput(inputTank.getFluid()));
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
		
		return Recipe.THERMAL_EVAPORATION_PLANT.containsRecipe(fluid);
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
				}
				else {
					clearStructure();
				}
			}
		}
	}

	public boolean canOperate(ThermalEvaporationRecipe recipe)
	{
		if(!structured || height < 3 || height > MAX_HEIGHT || inputTank.getFluid() == null)
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
		if(inventory[2] != null && outputTank.getFluid() != null)
		{
			if(inventory[2].getItem() instanceof IFluidContainerItem)
			{
				FluidContainerUtils.handleContainerItemFill(this, outputTank, 2, 3);
			}
			else if(FluidContainerRegistry.isEmptyContainer(inventory[2]))
			{
				FluidContainerUtils.handleRegistryItemFill(this, outputTank, 2, 3);
			}
		}
		
		if(structured)
		{
			if(inventory[0] != null)
			{
				if(inventory[0].getItem() instanceof IFluidContainerItem)
				{
					FluidContainerUtils.handleContainerItemEmpty(this, inputTank, 0, 1, new FluidChecker() {
						@Override
						public boolean isValid(Fluid f)
						{
							return hasRecipe(f);
						}
					});
				}
				else if(FluidContainerRegistry.isFilledContainer(inventory[0]))
				{
					FluidContainerUtils.handleRegistryItemEmpty(this, inputTank, 0, 1, new FluidChecker() {
						@Override
						public boolean isValid(Fluid f)
						{
							return hasRecipe(f);
						}
					});
				}
			}
		}
	}
	
	private void updateTemperature()
	{
		if(!temperatureSet)
		{
			biomeTemp = worldObj.getBiomeGenForCoordsBody(xCoord, zCoord).getFloatTemperature(xCoord, yCoord, zCoord);
			temperatureSet = true;
		}
		
		heatToAbsorb += getActiveSolars()*general.evaporationSolarMultiplier;
		temperature += heatToAbsorb/(float)height;
		
		float biome = biomeTemp-0.5F;
		float base = biome > 0 ? biome*20 : biomeTemp*40;
		
		if(Math.abs(temperature-base) < 0.001)
		{
			temperature = base;
		}
		
		float incr = (float)Math.sqrt(Math.abs(temperature-base))*(float)general.evaporationHeatDissipation;
		
		if(temperature > base)
		{
			incr = -incr;
		}
		
		float prev = temperature;
		temperature = (float)Math.min(general.evaporationMaxTemp, temperature + incr/(float)height);
		
		if(incr < 0)
		{
			totalLoss = prev-temperature;
		}
		else {
			totalLoss = 0;
		}
		
		heatToAbsorb = 0;
		
		MekanismUtils.saveChunk(this);
	}
	
	public float getTemperature()
	{
		return temperature;
	}
	
	public int getActiveSolars()
	{
		if(worldObj.isRemote)
		{
			return clientSolarAmount;
		}
		
		int ret = 0;
		
		for(IEvaporationSolar solar : solars)
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
		ForgeDirection left = MekanismUtils.getLeft(facing);

		height = 0;
		controllerConflict = false;
		updatedThisTick = true;
		
		Coord4D startPoint = Coord4D.get(this);
		
		while(startPoint.getFromSide(ForgeDirection.UP).getTileEntity(worldObj) instanceof TileEntityThermalEvaporationBlock)
		{
			startPoint.step(ForgeDirection.UP);
		}
		
		Coord4D test = startPoint.getFromSide(ForgeDirection.DOWN).getFromSide(right, 2);
		isLeftOnFace = test.getTileEntity(worldObj) instanceof TileEntityThermalEvaporationBlock;
		
		startPoint = startPoint.getFromSide(left, isLeftOnFace ? 1 : 2);
		
		if(!scanTopLayer(startPoint))
		{
			return false;
		}

		height = 1;
		
		Coord4D middlePointer = startPoint.getFromSide(ForgeDirection.DOWN);
		
		while(scanLowerLayer(middlePointer))
		{
			middlePointer = middlePointer.getFromSide(ForgeDirection.DOWN);
		}
		
		renderY = middlePointer.yCoord+1;
		
		if(height < 3 || height > MAX_HEIGHT)
		{
			height = 0;
			return false;
		}

		structured = true;
		
		markDirty();
		
		return true;
	}

	public boolean scanTopLayer(Coord4D current)
	{
		ForgeDirection right = MekanismUtils.getRight(facing);
		ForgeDirection back = MekanismUtils.getBack(facing);

		for(int x = 0; x < 4; x++)
		{
			for(int z = 0; z < 4; z++)
			{
				Coord4D pointer = current.getFromSide(right, x).getFromSide(back, z);
				TileEntity pointerTile = pointer.getTileEntity(worldObj);
				
				int corner = getCorner(x, z);
				
				if(corner != -1)
				{
					if(addSolarPanel(pointer.getTileEntity(worldObj), corner))
					{
						continue;
					}
					else if(pointer.getFromSide(ForgeDirection.UP).getTileEntity(worldObj) instanceof TileEntityThermalEvaporationBlock || !addTankPart(pointerTile))
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
						if(pointer.getFromSide(ForgeDirection.UP).getTileEntity(worldObj) instanceof TileEntityThermalEvaporationBlock || !addTankPart(pointerTile))
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

	public boolean scanLowerLayer(Coord4D current)
	{
		ForgeDirection right = MekanismUtils.getRight(facing);
		ForgeDirection back = MekanismUtils.getBack(facing);
		
		boolean foundCenter = false;

		for(int x = 0; x < 4; x++)
		{
			for(int z = 0; z < 4; z++)
			{
				Coord4D pointer = current.getFromSide(right, x).getFromSide(back, z);
				TileEntity pointerTile = pointer.getTileEntity(worldObj);
				
				if((x == 1 || x == 2) && (z == 1 || z == 2))
				{
					if(pointerTile instanceof TileEntityThermalEvaporationBlock)
					{
						if(!foundCenter)
						{
							if(x == 1 && z == 1)
							{
								foundCenter = true;
							}
							else {
								height = -1;
								return false;
							}
						}
					}
					else {
						if(foundCenter || !pointer.isAirBlock(worldObj))
						{
							height = -1;
							return false;
						}
					}
				}
				else {
					if(!addTankPart(pointerTile)) 
					{
						height = -1;
						return false;
					}
				}
			}
		}

		height++;
		
		return !foundCenter;
	}

	public boolean addTankPart(TileEntity tile)
	{
		if(tile instanceof TileEntityThermalEvaporationBlock && (tile == this || !(tile instanceof TileEntityThermalEvaporationController)))
		{
			if(tile != this)
			{
				((TileEntityThermalEvaporationBlock)tile).addToStructure(Coord4D.get(this));
				tankParts.add(Coord4D.get(tile));
			}
			
			return true;
		}
		else {
			if(tile != this && tile instanceof TileEntityThermalEvaporationController)
			{
				controllerConflict = true;
			}
			
			return false;
		}
	}

	public boolean addSolarPanel(TileEntity tile, int i)
	{
		if(tile instanceof IEvaporationSolar && !tile.isInvalid())
		{
			solars[i] = (IEvaporationSolar)tile;
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
		return (int)(i*Math.min(1, getTemperature()/general.evaporationMaxTemp));
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
		startPoint.yCoord = renderY;
		
		return startPoint;
	}
	
	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(worldObj.isRemote)
		{
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
			lastGain = dataStream.readFloat();
			totalLoss = dataStream.readFloat();
			renderY = dataStream.readInt();
			
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
							return tile instanceof TileEntityThermalEvaporationBlock;
						}
					});
				}
			}
			
			MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(inputTank.getFluid() != null)
		{
			data.add(true);
			data.add(inputTank.getFluid().getFluidID());
			data.add(inputTank.getFluid().amount);
		}
		else {
			data.add(false);
		}
		
		if(outputTank.getFluid() != null)
		{
			data.add(true);
			data.add(outputTank.getFluid().getFluidID());
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
		data.add(lastGain);
		data.add(totalLoss);
		data.add(renderY);
		
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
	
	@Override
	public TileEntityThermalEvaporationController getController()
	{
		return structured ? this : null;
	}

	public void clearStructure()
	{
		for(Coord4D tankPart : tankParts)
		{
			TileEntity tile = tankPart.getTileEntity(worldObj);
			
			if(tile instanceof TileEntityThermalEvaporationBlock)
			{
				((TileEntityThermalEvaporationBlock)tile).controllerGone();
			}
		}
		
		tankParts.clear();
		solars = new IEvaporationSolar[] {null, null, null, null};
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
	public void setActive(boolean active) {}

	@Override
	public boolean renderUpdate()
	{
		return true;
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
