package mekanism.common.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.IConfigurable;
import mekanism.common.IRedstoneControl.RedstoneControl;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import com.google.common.io.ByteArrayDataInput;

public class TileEntitySalinationController extends TileEntitySalinationTank implements IConfigurable
{
	public static int MAX_WATER = 100000;
	public static int MAX_BRINE = 1000;

	public FluidTank waterTank = new FluidTank(MAX_WATER);
	public FluidTank brineTank = new FluidTank(MAX_BRINE);

	public Set<TileEntitySalinationTank> tankParts = new HashSet<TileEntitySalinationTank>();
	public TileEntityAdvancedSolarGenerator[] solars = new TileEntityAdvancedSolarGenerator[4];

	public boolean temperatureSet = false;
	
	public double partialWater = 0;
	public double partialBrine = 0;
	
	public float baseTemperature = 0;
	
	public int height = 0;
	
	public boolean structured = false;
	public boolean controllerConflict = false;
	public boolean isLeftOnFace;
	
	public boolean updatedThisTick = false;

	public int clientSolarAmount;
	
	public TileEntitySalinationController()
	{
		super("SalinationController");
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			if(ticker == 5)
			{
				refresh();
			}
			
			setTemperature();
	
			if(canOperate())
			{
				partialWater += baseTemperature * (height + 7)/8;
				
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
		
		refresh();
	}
	
	@Override
	public void onNeighborChange(int id)
	{
		super.onNeighborChange(id);
		
		refresh();
	}
	
	protected void refresh()
	{
		if(!worldObj.isRemote)
		{
			if(!updatedThisTick)
			{
				structured = buildStructure();
				
				if(!structured)
				{
					clearStructure();
				}
			}
		}
	}

	public boolean canOperate()
	{
		if(!structured || height < 1 || waterTank.getFluid() == null || !waterTank.getFluid().containsFluid(FluidRegistry.getFluidStack("water", 100)))
		{
			return false;
		}

		boolean solarsActive = true;

		for(TileEntityAdvancedSolarGenerator solarPanel : solars)
		{
			if(solarPanel == null || solarPanel.isInvalid())
			{
				clearStructure();
				return false;
			}
			
			solarsActive &= solarPanel.seesSun;
		}
		
		return solarsActive;
	}

	public void setTemperature()
	{
		if(!temperatureSet)
		{
			baseTemperature = worldObj.getBiomeGenForCoordsBody(xCoord, zCoord).getFloatTemperature();
			temperatureSet = true;
		}
	}

	public boolean buildStructure()
	{
		controllerConflict = false;
		updatedThisTick = true;
		ForgeDirection right = MekanismUtils.getRight(facing);

		height = 0;
		
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
		
		if(middle != height-2)
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
	
	public ForgeDirection[] getMatrix()
	{
		return new ForgeDirection[] {MekanismUtils.getBack(facing), MekanismUtils.getLeft(facing), 
				ForgeDirection.getOrientation(facing), MekanismUtils.getRight(facing)};
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
						if(pointerTile != this && pointerTile instanceof TileEntitySalinationController)
						return false;
					}
				}
			}
		}

		return true;
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
					if(!addTankPart(pointer.getTileEntity(worldObj))) 
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
		if(tile instanceof TileEntitySalinationTank && !(tile instanceof TileEntitySalinationController))
		{
			((TileEntitySalinationTank)tile).addToStructure(this);
			tankParts.add((TileEntitySalinationTank)tile);
			
			return true;
		}
		else {
			return false;
		}
	}

	public boolean addSolarPanel(TileEntity tile, int i)
	{
		if(tile instanceof TileEntityAdvancedSolarGenerator)
		{
			solars[i] = (TileEntityAdvancedSolarGenerator)tile;
			return true;
		}
		else {
			return false;
		}
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
		
		structured = dataStream.readBoolean();
		controllerConflict = dataStream.readBoolean();
		clientSolarAmount = dataStream.readInt();
		
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
		data.add(getSolarAmount());
		
		return data;
	}
	
	private int getSolarAmount()
	{
		int ret = 0;
		
		for(TileEntityAdvancedSolarGenerator solar : solars)
		{
			if(solar != null)
			{
				ret++;
			}
		}
		
		return ret;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        waterTank.readFromNBT(nbtTags.getCompoundTag("waterTank"));
        brineTank.readFromNBT(nbtTags.getCompoundTag("brineTank"));
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setCompoundTag("waterTank", waterTank.writeToNBT(new NBTTagCompound()));
        nbtTags.setCompoundTag("brineTank", brineTank.writeToNBT(new NBTTagCompound()));
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
