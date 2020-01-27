package mekanism.generators.common.block;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import mekanism.api.MekanismConfig.client;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.IEnergizedItem;
import mekanism.common.CTMData;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IBlockCTM;
import mekanism.common.base.IBoundingBlock;
import mekanism.common.base.ISpecialBounds;
import mekanism.common.base.ISustainedData;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.base.ISustainedTank;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityContainerBlock;
import mekanism.common.tile.TileEntityElectricBlock;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.GeneratorsBlocks;
import mekanism.generators.common.GeneratorsItems;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.TileEntityAdvancedSolarGenerator;
import mekanism.generators.common.tile.TileEntityBioGenerator;
import mekanism.generators.common.tile.TileEntityGasGenerator;
import mekanism.generators.common.tile.TileEntityHeatGenerator;
import mekanism.generators.common.tile.TileEntitySolarGenerator;
import mekanism.generators.common.tile.TileEntityWindGenerator;
import mekanism.generators.common.tile.turbine.TileEntityElectromagneticCoil;
import mekanism.generators.common.tile.turbine.TileEntityRotationalComplex;
import mekanism.generators.common.tile.turbine.TileEntitySaturatingCondenser;
import mekanism.generators.common.tile.turbine.TileEntityTurbineCasing;
import mekanism.generators.common.tile.turbine.TileEntityTurbineRotor;
import mekanism.generators.common.tile.turbine.TileEntityTurbineValve;
import mekanism.generators.common.tile.turbine.TileEntityTurbineVent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple generator block IDs.
 * 0: Heat Generator
 * 1: Solar Generator
 * 3: Hydrogen Generator
 * 4: Bio-Generator
 * 5: Advanced Solar Generator
 * 6: Wind Generator
 * 7: Turbine Rotor
 * 8: Rotational Complex
 * 9: Electromagnetic Coil
 * 10: Turbine Casing
 * 11: Turbine Valve
 * 12: Turbine Vent
 * 13: Saturating Condenser
 * @author AidanBrady
 *
 */
public class BlockGenerator extends BlockContainer implements ISpecialBounds, IBlockCTM
{
	public IIcon[][] icons = new IIcon[16][16];
	
	public CTMData[] ctms = new CTMData[16];
	
	public IIcon BASE_ICON;
	
	public Random machineRand = new org.bogdang.modifications.random.XSTR();

	public BlockGenerator()
	{
		super(Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register)
	{
		BASE_ICON = register.registerIcon("mekanism:SteelCasing");
		
		ctms[9] = new CTMData("ctm/ElectromagneticCoil", this, Arrays.asList(9)).registerIcons(register);
		ctms[10] = new CTMData("ctm/TurbineCasing", this, Arrays.asList(10, 11, 12)).registerIcons(register);
		ctms[11] = new CTMData("ctm/TurbineValve", this, Arrays.asList(10, 11, 12)).registerIcons(register);
		ctms[12] = new CTMData("ctm/TurbineVent", this, Arrays.asList(10, 11, 12)).registerIcons(register);
		ctms[13] = new CTMData("ctm/SaturatingCondenser", this, Arrays.asList(13)).registerIcons(register);
		
		icons[7][0] = register.registerIcon("mekanism:TurbineRod");
		icons[8][0] = register.registerIcon("mekanism:RotationalComplexSide");
		icons[8][1] = register.registerIcon("mekanism:RotationalComplexTop");
		
		icons[9][0] = ctms[9].mainTextureData.icon;
		icons[10][0] = ctms[10].mainTextureData.icon;
		icons[11][0] = ctms[11].mainTextureData.icon;
		icons[12][0] = ctms[12].mainTextureData.icon;
		icons[13][0] = ctms[13].mainTextureData.icon;
	}
	
	@Override
	public CTMData getCTMData(IBlockAccess world, int x, int y, int z, int meta)
	{
		return ctms[meta];
	}
	
	@Override
	public boolean shouldRenderBlock(IBlockAccess world, int x, int y, int z, int meta)
	{
		return !GeneratorType.getFromMetadata(meta).hasModel;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
		setBlockBoundsBasedOnState(world, x, y, z);
		
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		GeneratorType type = GeneratorType.getFromMetadata(meta);
		
		if(type == GeneratorType.ROTATIONAL_COMPLEX)
		{
			if(side != 0 && side != 1)
			{
				return icons[meta][0];
			}
			else {
				return icons[meta][1];
			}
		}
		else if(!type.hasModel)
		{
			return icons[meta][0];
		}
		else {
			return BASE_ICON;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		
		return getIcon(side, meta);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			
			if(tileEntity instanceof IMultiblock)
			{
				((IMultiblock)tileEntity).update();
			}

			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(block);
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);

		int side = MathHelper.floor_double((double)(entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		int height = Math.round(entityliving.rotationPitch);
		int change = 3;

		if(!GeneratorType.getFromMetadata(world.getBlockMetadata(x, y, z)).hasModel && tileEntity.canSetFacing(0) && tileEntity.canSetFacing(1))
		{
			if(height >= 65)
			{
				change = 1;
			}
			else if(height <= -65)
			{
				change = 0;
			}
		}

		if(change != 0 && change != 1)
		{
			switch(side)
			{
				case 0: change = 2; break;
				case 1: change = 5; break;
				case 2: change = 3; break;
				case 3: change = 4; break;
			}
		}

		tileEntity.setFacing((short)change);
		tileEntity.redstone = world.isBlockIndirectlyGettingPowered(x, y, z);

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onPlace();
		}
		
		if(!world.isRemote && tileEntity instanceof IMultiblock)
		{
			((IMultiblock)tileEntity).update();
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		if(client.enableAmbientLighting)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);

			if(tileEntity instanceof IActiveState && !(tileEntity instanceof TileEntitySolarGenerator))
			{
				if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
				{
					return client.ambientLightingLevel;
				}
			}
		}

		return 0;
	}


	@Override
	public int damageDropped(int i)
	{
		return i;
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		
		return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(player, world, x, y, z) : 0.0F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item i, CreativeTabs creativetabs, List list)
	{
		for(GeneratorType type : GeneratorType.values())
		{
			list.add(new ItemStack(i, 1, type.meta));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random random)
	{
		int metadata = world.getBlockMetadata(x, y, z);
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);
		
		if(MekanismUtils.isActive(world, x, y, z))
		{
			float xRandom = (float)x + 0.5F;
			float yRandom = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
			float zRandom = (float)z + 0.5F;
			float iRandom = 0.52F;
			float jRandom = random.nextFloat() * 0.6F - 0.3F;

			if(tileEntity.facing == 4)
			{
				switch(GeneratorType.getFromMetadata(metadata))
				{
					case HEAT_GENERATOR:
						world.spawnParticle("smoke", (double)(xRandom - iRandom), (double)yRandom + 0.5F, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
						world.spawnParticle("flame", (double)(xRandom - iRandom), (double)yRandom + 0.5F, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
						break;
					case BIO_GENERATOR:
						world.spawnParticle("smoke", x+.25, y+.2, z+.5, 0.0D, 0.0D, 0.0D);
						break;
					default:
						break;
				}
			}
			else if(tileEntity.facing == 5)
			{
				switch(GeneratorType.getFromMetadata(metadata))
				{
					case HEAT_GENERATOR:
						world.spawnParticle("smoke", (double)(xRandom + iRandom), (double)yRandom + 0.5F, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
						world.spawnParticle("flame", (double)(xRandom + iRandom), (double)yRandom + 0.5F, (double)(zRandom - jRandom), 0.0D, 0.0D, 0.0D);
						break;
					case BIO_GENERATOR:
						world.spawnParticle("smoke", x+.75, y+.2, z+.5, 0.0D, 0.0D, 0.0D);
						break;
					default:
						break;
				}
			}
			else if(tileEntity.facing == 2)
			{
				switch(GeneratorType.getFromMetadata(metadata))
				{
					case HEAT_GENERATOR:
						world.spawnParticle("smoke", (double)(xRandom - jRandom), (double)yRandom + 0.5F, (double)(zRandom - iRandom), 0.0D, 0.0D, 0.0D);
						world.spawnParticle("flame", (double)(xRandom - jRandom), (double)yRandom + 0.5F, (double)(zRandom - iRandom), 0.0D, 0.0D, 0.0D);
						break;
					case BIO_GENERATOR:
						world.spawnParticle("smoke", x+.5, y+.2, z+.25, 0.0D, 0.0D, 0.0D);
						break;
					default:
						break;
				}
			}
			else if(tileEntity.facing == 3)
			{
				switch(GeneratorType.getFromMetadata(metadata))
				{
					case HEAT_GENERATOR:
						world.spawnParticle("smoke", (double)(xRandom - jRandom), (double)yRandom + 0.5F, (double)(zRandom + iRandom), 0.0D, 0.0D, 0.0D);
						world.spawnParticle("flame", (double)(xRandom - jRandom), (double)yRandom + 0.5F, (double)(zRandom + iRandom), 0.0D, 0.0D, 0.0D);
						break;
					case BIO_GENERATOR:
						world.spawnParticle("smoke", x+.5, y+.2, z+.75, 0.0D, 0.0D, 0.0D);
						break;
					default:
						break;
				}
			}
		}
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z)
	{
		//This method doesn't actually seem to be used in MC code...
		if(world.getBlockMetadata(x, y, z) == GeneratorType.ADVANCED_SOLAR_GENERATOR.meta)
		{
			boolean canPlace = super.canPlaceBlockAt(world, x, y, z);

			boolean nonAir = false;
			nonAir |= world.isAirBlock(x, y, z);
			nonAir |= world.isAirBlock(x, y+1, x);

			for(int xPos=-1;xPos<=1;xPos++)
			{
				for(int zPos=-1;zPos<=1;zPos++)
				{
					nonAir |= world.isAirBlock(x+xPos, y+2, z+zPos);
				}
			}

			return (!nonAir) && canPlace;
		}
		else if(world.getBlockMetadata(x, y, z) == GeneratorType.WIND_GENERATOR.meta)
		{
			boolean canPlace = super.canPlaceBlockAt(world, x, y, z);

			boolean nonAir = false;

			for(int yPos = y+1; yPos <= y+4; yPos++)
			{
				nonAir |= world.isAirBlock(x, yPos, z);
			}

			return (!nonAir) && canPlace;
		}

		return super.canPlaceBlockAt(world, x, y, z);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);
		
		if(!world.isRemote && tileEntity instanceof TileEntityTurbineRotor)
		{
			int amount = ((TileEntityTurbineRotor)tileEntity).getHousedBlades();
			
			if(amount > 0)
			{
				float motion = 0.7F;
				double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
				double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
				double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

				EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, new ItemStack(GeneratorsItems.TurbineBlade, amount));

				world.spawnEntityInWorld(entityItem);
			}
		}

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onBreak();
		}

		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int side, float playerX, float playerY, float playerZ)
	{
		if(world.isRemote)
		{
			return true;
		}

		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);

		if(entityplayer.getCurrentEquippedItem() != null)
		{
			Item tool = entityplayer.getCurrentEquippedItem().getItem();

			if(MekanismUtils.hasUsableWrench(entityplayer, x, y, z))
			{
				if(SecurityUtils.canAccess(entityplayer, tileEntity))
				{
					if(entityplayer.isSneaking())
					{
						dismantleBlock(world, x, y, z, false);
						
						return true;
					}
	
					if(MekanismUtils.isBCWrench(tool))
					{
						((IToolWrench)tool).wrenchUsed(entityplayer, x, y, z);
					}
	
					int change = ForgeDirection.ROTATION_MATRIX[ForgeDirection.UP.ordinal()][tileEntity.facing];
	
					tileEntity.setFacing((short)change);
					world.notifyBlocksOfNeighborChange(x, y, z, this);
				}
				else {
					SecurityUtils.displayNoAccess(entityplayer);
				}
				
				return true;
			}
		}
		
		if(metadata == GeneratorType.TURBINE_CASING.meta || metadata == GeneratorType.TURBINE_VALVE.meta || metadata == GeneratorType.TURBINE_VENT.meta)
		{
			return ((IMultiblock)world.getTileEntity(x, y, z)).onActivate(entityplayer);
		}
		
		if(metadata == GeneratorType.TURBINE_ROTOR.meta)
		{
			TileEntityTurbineRotor rod = (TileEntityTurbineRotor)tileEntity;
			
			if(!entityplayer.isSneaking())
			{
				if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == GeneratorsItems.TurbineBlade)
				{
					if(!world.isRemote && rod.editBlade(true))
					{
						if(!entityplayer.capabilities.isCreativeMode)
						{
							entityplayer.getCurrentEquippedItem().stackSize--;
							
							if(entityplayer.getCurrentEquippedItem().stackSize == 0)
							{
								entityplayer.setCurrentItemOrArmor(0, null);
							}
						}
					}
					
					return true;
				}
			}
			else {
				if(!world.isRemote)
				{
					if(entityplayer.getCurrentEquippedItem() == null)
					{
						if(rod.editBlade(false))
						{
							if(!entityplayer.capabilities.isCreativeMode)
							{
								entityplayer.setCurrentItemOrArmor(0, new ItemStack(GeneratorsItems.TurbineBlade));
								entityplayer.inventory.markDirty();
							}
						}
					}
					else if(entityplayer.getCurrentEquippedItem().getItem() == GeneratorsItems.TurbineBlade)
					{
						if(entityplayer.getCurrentEquippedItem().stackSize < entityplayer.getCurrentEquippedItem().getMaxStackSize())
						{
							if(rod.editBlade(false))
							{
								if(!entityplayer.capabilities.isCreativeMode)
								{
									entityplayer.getCurrentEquippedItem().stackSize++;
									entityplayer.inventory.markDirty();
								}
							}
						}
					}
				}
				
				return true;
			}
			
			return false;
		}
		
		int guiId = GeneratorType.getFromMetadata(metadata).guiId;

		if(guiId != -1 && tileEntity != null)
		{
			if(!entityplayer.isSneaking())
			{
				if(SecurityUtils.canAccess(entityplayer, tileEntity))
				{
					entityplayer.openGui(MekanismGenerators.instance, guiId, world, x, y, z);
				}
				else {
					SecurityUtils.displayNoAccess(entityplayer);
				}
				
				return true;
			}
		}

		return false;
	}

	@Override
	public int quantityDropped(Random random)
	{
		return 0;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		GeneratorType type = GeneratorType.getFromMetadata(metadata);

		if(type != null)
		{
			return type.create();
		}

		return null;
	}

	@Override
	public Item getItemDropped(int i, Random random, int j)
	{
		return null;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return Mekanism.proxy.CTM_RENDER_ID;
	}

	/*This method is not used, metadata manipulation is required to create a Tile Entity.*/
	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return null;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		int metadata = world.getBlockMetadata(x, y, z);

		if(metadata == GeneratorType.SOLAR_GENERATOR.meta)
		{
			setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.7F, 1.0F);
		}
		else if(metadata == GeneratorType.TURBINE_ROTOR.meta)
		{
			setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, 1.0F, 0.625F);
		}
		else {
			setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);

		if(!world.isRemote)
		{
			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onAdded();
			}
		}
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
	{
		if(!player.capabilities.isCreativeMode && !world.isRemote && willHarvest)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, getPickBlock(null, world, x, y, z, player));

			world.spawnEntityInWorld(entityItem);
		}

		return world.setBlockToAir(x, y, z);
	}

	@Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	{
		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getTileEntity(x, y, z);
		ItemStack itemStack = new ItemStack(GeneratorsBlocks.Generator, 1, world.getBlockMetadata(x, y, z));

		if(itemStack.stackTagCompound == null && !(tileEntity instanceof TileEntityMultiblock))
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}
		
		if(tileEntity == null)
		{
			return null;
		}
		
		if(tileEntity instanceof ISecurityTile)
		{
			ISecurityItem securityItem = (ISecurityItem)itemStack.getItem();
			
			if(securityItem.hasSecurity(itemStack))
			{
				securityItem.setOwner(itemStack, ((ISecurityTile)tileEntity).getSecurity().getOwner());
				securityItem.setSecurity(itemStack, ((ISecurityTile)tileEntity).getSecurity().getMode());
			}
		}

		if(tileEntity instanceof TileEntityElectricBlock)
		{
			IEnergizedItem electricItem = (IEnergizedItem)itemStack.getItem();
			electricItem.setEnergy(itemStack, ((TileEntityElectricBlock)tileEntity).electricityStored);
		}

		if(tileEntity instanceof TileEntityContainerBlock && ((TileEntityContainerBlock)tileEntity).handleInventory())
		{
			ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
			inventory.setInventory(((TileEntityContainerBlock)tileEntity).getInventory(), itemStack);
		}
		
		if(tileEntity instanceof ISustainedData)
		{
			((ISustainedData)tileEntity).writeSustainedData(itemStack);
		}

		if(((ISustainedTank)itemStack.getItem()).hasTank(itemStack))
		{
			if(tileEntity instanceof ISustainedTank)
			{
				if(((ISustainedTank)tileEntity).getFluidStack() != null)
				{
					((ISustainedTank)itemStack.getItem()).setFluidStack(((ISustainedTank)tileEntity).getFluidStack(), itemStack);
				}
			}
		}

		return itemStack;
	}

	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock)
	{
		ItemStack itemStack = getPickBlock(null, world, x, y, z, null);

		world.setBlockToAir(x, y, z);

		if(!returnBlock)
		{
			float motion = 0.7F;
			double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
			double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;

			EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, itemStack);

			world.spawnEntityInWorld(entityItem);
		}

		return itemStack;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		int metadata = world.getBlockMetadata(x, y, z);

		if(metadata != GeneratorType.SOLAR_GENERATOR.meta && 
				metadata != GeneratorType.ADVANCED_SOLAR_GENERATOR.meta && 
				metadata != GeneratorType.WIND_GENERATOR.meta &&
				metadata != GeneratorType.TURBINE_ROTOR.meta)
		{
			return true;
		}

		return false;
	}

	public static enum GeneratorType
	{
		HEAT_GENERATOR(0, "HeatGenerator", 0, 160000, TileEntityHeatGenerator.class, true),
		SOLAR_GENERATOR(1, "SolarGenerator", 1, 96000, TileEntitySolarGenerator.class, true),
		GAS_GENERATOR(3, "GasGenerator", 3, general.FROM_H2*100, TileEntityGasGenerator.class, true),
		BIO_GENERATOR(4, "BioGenerator", 4, 160000, TileEntityBioGenerator.class, true),
		ADVANCED_SOLAR_GENERATOR(5, "AdvancedSolarGenerator", 1, 200000, TileEntityAdvancedSolarGenerator.class, true),
		WIND_GENERATOR(6, "WindGenerator", 5, 200000, TileEntityWindGenerator.class, true),
		TURBINE_ROTOR(7, "TurbineRotor", -1, -1, TileEntityTurbineRotor.class, false),
		ROTATIONAL_COMPLEX(8, "RotationalComplex", -1, -1, TileEntityRotationalComplex.class, false),
		ELECTROMAGNETIC_COIL(9, "ElectromagneticCoil", -1, -1, TileEntityElectromagneticCoil.class, false),
		TURBINE_CASING(10, "TurbineCasing", -1, -1, TileEntityTurbineCasing.class, false),
		TURBINE_VALVE(11, "TurbineValve", -1, -1, TileEntityTurbineValve.class, false),
		TURBINE_VENT(12, "TurbineVent", -1, -1, TileEntityTurbineVent.class, false),
		SATURATING_CONDENSER(13, "SaturatingCondenser", -1, -1, TileEntitySaturatingCondenser.class, false);

		public int meta;
		public String name;
		public int guiId;
		public double maxEnergy;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean hasModel;

		private GeneratorType(int i, String s, int j, double k, Class<? extends TileEntity> tileClass, boolean model)
		{
			meta = i;
			name = s;
			guiId = j;
			maxEnergy = k;
			tileEntityClass = tileClass;
			hasModel = model;
		}

		public static GeneratorType getFromMetadata(int meta)
		{
			for(GeneratorType type : values())
			{
				if(type.meta == meta)
				{
					return type;
				}
			}
			
			return null;
		}

		public TileEntity create()
		{
			try {
				return tileEntityClass.newInstance();
			} catch(Exception e) {
				Mekanism.logger.error("Unable to indirectly create tile entity.");
				e.printStackTrace();
				return null;
			}
		}
		
		public String getDescription()
		{
			return LangUtils.localize("tooltip." + name);
		}
		
		public ItemStack getStack()
		{
			return new ItemStack(GeneratorsBlocks.Generator, 1, meta);
		}

		@Override
		public String toString()
		{
			return Integer.toString(meta);
		}
	}

	@Override
	public void setRenderBounds(Block block, int metadata)
	{
		if(metadata == GeneratorType.SOLAR_GENERATOR.meta)
		{
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.7F, 1.0F);
		}
		else if(metadata == GeneratorType.TURBINE_ROTOR.meta)
		{
			block.setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, 1.0F, 0.625F);
		}
		else {
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	@Override
	public boolean doDefaultBoundSetting(int metadata)
	{
		return true;
	}

	@Override
	public ForgeDirection[] getValidRotations(World world, int x, int y, int z)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		ForgeDirection[] valid = new ForgeDirection[6];
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			{
				if(basicTile.canSetFacing(dir.ordinal()))
				{
					valid[dir.ordinal()] = dir;
				}
			}
		}
		
		return valid;
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis)
	{
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if(tile instanceof TileEntityBasicBlock)
		{
			TileEntityBasicBlock basicTile = (TileEntityBasicBlock)tile;
			
			if(basicTile.canSetFacing(axis.ordinal()))
			{
				basicTile.setFacing((short)axis.ordinal());
				return true;
			}
		}
		
		return false;
	}
}
