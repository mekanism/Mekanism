package mekanism.common.tile;

import java.util.HashSet;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.IConfigurable;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;

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
	public float temperature = 0;
	public int height = 0;
	public boolean structured = false;

	public boolean isLeftOnFace;

	public TileEntitySalinationController()
	{
		super("SalinationController");
	}

	@Override
	public void onUpdate()
	{
		setTemperature();

		if(canOperate())
		{
			partialWater += temperature * (height + 7)/8;
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
			temperature = worldObj.getBiomeGenForCoordsBody(xCoord, zCoord).getFloatTemperature();
			temperatureSet = true;
		}
	}

	public boolean buildStructure()
	{
		ForgeDirection right = MekanismUtils.getRight(facing);

		height = 0;
		
		if(!findBottomLayer())
		{ 
			return false; 
		}
		
		Coord4D startPoint = Coord4D.get(this).getFromSide(right);
		startPoint = isLeftOnFace ? startPoint : startPoint.getFromSide(right);

		while(findMiddleLayer(startPoint))
		{
			startPoint = startPoint.getFromSide(ForgeDirection.UP);
			height++;
		}

		structured =  findTopLayer(startPoint);
		height = structured ? height + 1 : 0;
		return structured;
	}
	
	public ForgeDirection[] getMatrix()
	{
		return new ForgeDirection[] {MekanismUtils.getBack(facing), MekanismUtils.getLeft(facing), 
				ForgeDirection.getOrientation(facing), MekanismUtils.getRight(facing)};
	}

	public boolean findTopLayer(Coord4D current)
	{
		ForgeDirection[] matrix = getMatrix();
		
		for(int side = 0; side < matrix.length; side++)
		{
			for(int i = 1; i <= 2; i++)
			{
				current = current.getFromSide(matrix[side]);
				TileEntity tile = current.getTileEntity(worldObj);
				
				if(!addTankPart(tile)) 
				{ 
					return false;
				}
			}

			current = current.getFromSide(matrix[side]);
			TileEntity solar = current.getTileEntity(worldObj);
			
			if(!addSolarPanel(solar, side))
			{ 
				return false;
			}
		}

		return true;
	}

	public boolean findMiddleLayer(Coord4D current)
	{
		ForgeDirection[] matrix = getMatrix();

		for(ForgeDirection side : matrix)
		{
			for(int i = 1; i <= 3; i++)
			{
				current = current.getFromSide(side);
				TileEntity tile = current.getTileEntity(worldObj);
				
				if(!addTankPart(tile)) 
				{ 
					return false;
				}
			}
		}

		return true;
	}

	public boolean findBottomLayer()
	{
		Coord4D baseBlock = Coord4D.get(this).getFromSide(ForgeDirection.DOWN);

		ForgeDirection left = MekanismUtils.getLeft(facing);
		ForgeDirection right = MekanismUtils.getRight(facing);

		if(!findBottomRow(baseBlock)) 
		{ 
			return false;
		};
		
		if(!findBottomRow(baseBlock.getFromSide(left))) 
		{ 
			return false;
		};
		
		if(!findBottomRow(baseBlock.getFromSide(right))) 
		{ 
			return false;
		};

		boolean twoLeft = findBottomRow(baseBlock.getFromSide(left).getFromSide(left));
		boolean twoRight = findBottomRow(baseBlock.getFromSide(right).getFromSide(right));

		if(twoLeft == twoRight) 
		{ 
			return false;
		}

		isLeftOnFace = twoLeft;
		
		return true;
	}

	public boolean findBottomRow(Coord4D start)
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
		if(tile instanceof TileEntitySalinationTank)
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
		structured = buildStructure();
		player.sendChatToPlayer(ChatMessageComponent.createFromText("Height: " + height + ", Structured: " + structured));
		System.out.println(solars[0] + " " + solars[1] + " " + solars[2] + " " + solars[3]);
		return true;
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
