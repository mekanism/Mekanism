package mekanism.generators.common.content.turbine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.common.content.tank.TankUpdateProtocol;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.generators.common.GeneratorsBlocks;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.block.BlockGenerator.GeneratorType;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityPressureDisperser;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TurbineUpdateProtocol extends UpdateProtocol<SynchronizedTurbineData>
{
	public static final int FLUID_PER_TANK = TankUpdateProtocol.FLUID_PER_TANK;
	
	public TurbineUpdateProtocol(TileEntityTurbineCasing tileEntity) 
	{
		super(tileEntity);
	}

	@Override
	protected boolean isValidFrame(int x, int y, int z) 
	{
		return pointer.getWorldObj().getBlock(x, y, z) == GeneratorsBlocks.Generator && 
				GeneratorType.getFromMetadata(pointer.getWorldObj().getBlockMetadata(x, y, z)) == GeneratorType.TURBINE_CASING;
	}
	
	@Override
	protected boolean isValidInnerNode(int x, int y, int z)
	{
		if(super.isValidInnerNode(x, y, z))
		{
			return true;
		}

		TileEntity tile = pointer.getWorldObj().getTileEntity(x, y, z);
		
		return tile instanceof TileEntityTurbineRotor || tile instanceof TileEntityRotationalComplex ||
				tile instanceof TileEntityPressureDisperser || tile instanceof TileEntityElectromagneticCoil;
	}
	
	@Override
	public void killInnerNode(Coord4D coord)
	{
		TileEntity tile = coord.getTileEntity(pointer.getWorldObj());
		
		if(tile instanceof TileEntityRotationalComplex)
		{
			((TileEntityRotationalComplex)tile).setMultiblock(null);
		}
	}
	
	@Override
	protected boolean canForm(SynchronizedTurbineData structure)
	{
		if(structure.volLength % 2 == 1 && structure.volWidth % 2 == 1)
		{
			int innerRadius = (Math.min(structure.volLength, structure.volWidth)-3)/2;
			
			if(innerRadius >= Math.ceil((structure.volHeight-2)/4))
			{
				int centerX = structure.minLocation.xCoord+(structure.volLength-1)/2;
				int centerZ = structure.minLocation.zCoord+(structure.volWidth-1)/2;
				
				Coord4D complex = null;
				
				Set<Coord4D> turbines = new HashSet<Coord4D>();
				Set<Coord4D> dispersers = new HashSet<Coord4D>();
				Set<Coord4D> coils = new HashSet<Coord4D>();
				
				//Scan for complex
				for(Coord4D coord : innerNodes)
				{
					TileEntity tile = coord.getTileEntity(pointer.getWorldObj());
					
					if(tile instanceof TileEntityRotationalComplex)
					{
						if(complex != null)
						{
							return false;
						}
						else if(coord.xCoord != centerX || coord.zCoord != centerZ)
						{
							return false;
						}
						
						complex = coord;
					}
					else if(tile instanceof TileEntityTurbineRotor)
					{
						if(coord.xCoord != centerX || coord.zCoord != centerZ)
						{
							return false;
						}
						
						turbines.add(coord);
					}
					else if(tile instanceof TileEntityPressureDisperser)
					{
						dispersers.add(coord);
					}
					else if(tile instanceof TileEntityElectromagneticCoil)
					{
						coils.add(coord);
					}
				}
				
				//Terminate if complex doesn't exist
				if(complex == null)
				{
					return false;
				}
				
				//Make sure a flat, horizontal plane of dispersers exists within the multiblock around the complex
				for(int x = complex.xCoord-innerRadius; x <= complex.xCoord+innerRadius; x++)
				{
					for(int z = complex.zCoord-innerRadius; z <= complex.zCoord+innerRadius; z++)
					{
						if(!(x == centerX && z == centerZ))
						{
							TileEntity tile = pointer.getWorldObj().getTileEntity(x, complex.yCoord, z);
							
							if(!(tile instanceof TileEntityPressureDisperser))
							{
								return false;
							}
							
							dispersers.remove(new Coord4D(x, complex.yCoord, z));
						}
					}
				}
				
				//If any dispersers were not processed, they're in the wrong place
				if(dispersers.size() > 0)
				{
					return false;
				}
				
				int turbineHeight = 0;
				
				//Make sure a complete line of turbine rotors exist from the complex to the multiblock's base
				for(int y = complex.yCoord-1; y > structure.minLocation.yCoord; y--)
				{
					TileEntity tile = pointer.getWorldObj().getTileEntity(centerX, y, centerZ);
					
					if(!(tile instanceof TileEntityTurbineRotor))
					{
						return false;
					}
					
					turbineHeight++;
					turbines.remove(new Coord4D(centerX, y, centerZ, pointer.getWorldObj().provider.dimensionId));
				}
				
				//If any turbines were not processed, they're in the wrong place
				if(turbines.size() > 0)
				{
					return false;
				}
				
				Coord4D startCoord = complex.getFromSide(ForgeDirection.UP);
				TileEntity startTile = startCoord.getTileEntity(pointer.getWorldObj());
				
				if(startTile instanceof TileEntityElectromagneticCoil)
				{
					structure.coils = new CoilCounter().calculate((TileEntityElectromagneticCoil)startTile);
				}
				
				Coord4D turbineCoord = complex.getFromSide(ForgeDirection.DOWN);
				TileEntity turbineTile = turbineCoord.getTileEntity(pointer.getWorldObj());
				
				if(turbineTile instanceof TileEntityTurbineRotor)
				{
					structure.blades = ((TileEntityTurbineRotor)turbineTile).blades;
				}
				
				if(coils.size() > structure.coils)
				{
					return false;
				}
				
				for(Coord4D coord : structure.locations)
				{
					if(coord.getTileEntity(pointer.getWorldObj()) instanceof TileEntityTurbineVent)
					{
						if(coord.yCoord >= complex.yCoord)
						{
							structure.vents++;
						}
						else {
							return false;
						}
					}
				}
				
				structure.lowerVolume = structure.volLength*structure.volWidth*turbineHeight;
				
				((TileEntityRotationalComplex)complex.getTileEntity(pointer.getWorldObj())).setMultiblock(structure.inventoryID);
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected MultiblockCache<SynchronizedTurbineData> getNewCache() 
	{
		return new TurbineCache();
	}

	@Override
	protected SynchronizedTurbineData getNewStructure() 
	{
		return new SynchronizedTurbineData();
	}

	@Override
	protected MultiblockManager<SynchronizedTurbineData> getManager() 
	{
		return MekanismGenerators.turbineManager;
	}

	@Override
	protected void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<SynchronizedTurbineData> cache, MultiblockCache<SynchronizedTurbineData> merge)
	{
		if(((TurbineCache)cache).fluid == null)
		{
			((TurbineCache)cache).fluid = ((TurbineCache)merge).fluid;
		}
		else if(((TurbineCache)merge).fluid != null && ((TurbineCache)cache).fluid.isFluidEqual(((TurbineCache)merge).fluid))
		{
			((TurbineCache)cache).fluid.amount += ((TurbineCache)merge).fluid.amount;
		}
	}
	
	@Override
	protected void onFormed()
	{
		if(structureFound.fluidStored != null)
		{
			structureFound.fluidStored.amount = Math.min(structureFound.fluidStored.amount, structureFound.getFluidCapacity());
		}
	}
	
	public class CoilCounter
	{
		public Set<Coord4D> iterated = new HashSet<Coord4D>();
		
		public void loop(Coord4D pos)
		{
			iterated.add(pos);
			
			for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
			{
				Coord4D coord = pos.getFromSide(side);
				TileEntity tile = coord.getTileEntity(pointer.getWorldObj());
				
				if(!iterated.contains(coord))
				{
					if(tile instanceof TileEntityElectromagneticCoil)
					{
						loop(coord);
					}
				}
			}
		}
		
		public int calculate(TileEntityElectromagneticCoil tileEntity)
		{
			loop(Coord4D.get(tileEntity));
			
			return iterated.size();
		}
	}
}
