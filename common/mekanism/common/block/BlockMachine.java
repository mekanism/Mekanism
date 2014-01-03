package mekanism.common.block;

import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergizedItem;
import mekanism.client.ClientProxy;
import mekanism.common.*;
import mekanism.common.IFactory.RecipeType;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.miner.MinerFilter;
import mekanism.common.network.PacketElectricChest;
import mekanism.common.network.PacketElectricChest.ElectricChestPacketType;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketLogisticalSorterGui.SorterGuiPacket;
import mekanism.common.tileentity.*;
import mekanism.common.transporter.TransporterFilter;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import java.util.List;
import java.util.Random;

/**
 * Block class for handling multiple machine block IDs.
 * 0:0: Enrichment Chamber
 * 0:1: Osmium Compressor
 * 0:2: Combiner
 * 0:3: Crusher
 * 0:4: Digital Miner
 * 0:5: Basic Factory
 * 0:6: Advanced Factory
 * 0:7: Elite Factory
 * 0:8: Metallurgic Infuser
 * 0:9: Purification Chamber
 * 0:10: Energized Smelter
 * 0:11: Teleporter
 * 0:12: Electric Pump
 * 0:13: Electric Chest
 * 0:14: Chargepad
 * 0:15: Logistical Sorter
 * 1:0: Rotary Condensentrator
 * 1:1: Chemical Oxidizer
 * 1:2: Chemical Infuser
 * 1:3: Chemical Injection Chamber
 * @author AidanBrady
 *
 */
public class BlockMachine extends BlockContainer implements ISpecialBounds
{
	public Icon[][] icons = new Icon[16][16];
	public Random machineRand = new Random();
	
	public BlockMachine(int id)
	{
		super(id, Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		if(blockID == Mekanism.machineBlockID)
		{
			icons[0][0] = register.registerIcon("mekanism:EnrichmentChamberFrontOff");
			icons[0][1] = register.registerIcon("mekanism:EnrichmentChamberFrontOn");
			icons[0][2] = register.registerIcon("mekanism:SteelCasing");
			icons[1][0] = register.registerIcon("mekanism:OsmiumCompressorFrontOff");
			icons[1][1] = register.registerIcon("mekanism:OsmiumCompressorFrontOn");
			icons[1][2] = register.registerIcon("mekanism:SteelCasing");
			icons[2][0] = register.registerIcon("mekanism:CombinerFrontOff");
			icons[2][1] = register.registerIcon("mekanism:CombinerFrontOn");
			icons[2][2] = register.registerIcon("mekanism:SteelCasing");
			icons[3][0] = register.registerIcon("mekanism:CrusherFrontOff");
			icons[3][1] = register.registerIcon("mekanism:CrusherFrontOn");
			icons[3][2] = register.registerIcon("mekanism:SteelCasing");
			icons[5][0] = register.registerIcon("mekanism:BasicFactoryFront");
			icons[5][1] = register.registerIcon("mekanism:BasicFactorySide");
			icons[5][2] = register.registerIcon("mekanism:BasicFactoryTop");
			icons[6][0] = register.registerIcon("mekanism:AdvancedFactoryFront");
			icons[6][1] = register.registerIcon("mekanism:AdvancedFactorySide");
			icons[6][2] = register.registerIcon("mekanism:AdvancedFactoryTop");
			icons[7][0] = register.registerIcon("mekanism:EliteFactoryFront");
			icons[7][1] = register.registerIcon("mekanism:EliteFactorySide");
			icons[7][2] = register.registerIcon("mekanism:EliteFactoryTop");
			icons[9][0] = register.registerIcon("mekanism:PurificationChamberFrontOff");
			icons[9][1] = register.registerIcon("mekanism:PurificationChamberFrontOn");
			icons[9][2] = register.registerIcon("mekanism:SteelCasing");
			icons[10][0] = register.registerIcon("mekanism:EnergizedSmelterFrontOff");
			icons[10][1] = register.registerIcon("mekanism:EnergizedSmelterFrontOn");
			icons[10][2] = register.registerIcon("mekanism:SteelCasing");
			icons[11][0] = register.registerIcon("mekanism:Teleporter");
		}
		else if(blockID == Mekanism.machineBlock2ID)
		{
			icons[2][0] = register.registerIcon("mekanism:ChemicalInjectionChamberFrontOff");
			icons[2][1] = register.registerIcon("mekanism:ChemicalInjectionChamberFrontOn");
			icons[2][2] = register.registerIcon("mekanism:SteelCasing");
			icons[3][0] = register.registerIcon("mekanism:ChemicalInjectionChamberFrontOff");
			icons[3][1] = register.registerIcon("mekanism:ChemicalInjectionChamberFrontOn");
			icons[3][2] = register.registerIcon("mekanism:SteelCasing");
			icons[4][0] = register.registerIcon("mekanism:ChemicalInjectionChamberFrontOff");
			icons[4][1] = register.registerIcon("mekanism:ChemicalInjectionChamberFrontOn");
			icons[4][2] = register.registerIcon("mekanism:SteelCasing");
		}
	}
	
	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
    {
    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
        int side = MathHelper.floor_double((entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int height = Math.round(entityliving.rotationPitch);
        int change = 3;
        
        if(tileEntity == null)
        {
        	return;
        }
        
        if(tileEntity.canSetFacing(0) && tileEntity.canSetFacing(1))
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
        
        if(tileEntity instanceof TileEntityLogisticalSorter)
        {
        	TileEntityLogisticalSorter transporter = (TileEntityLogisticalSorter)tileEntity;
        	
        	if(!transporter.hasInventory())
    		{
    			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
    			{
    				TileEntity tile = Coord4D.get(transporter).getFromSide(dir).getTileEntity(world);

    				if(tile instanceof IInventory)
    				{
    					change = dir.getOpposite().ordinal();
    					break;
    				}
    			}
    		}
        }
        
        tileEntity.setFacing((short)change);
        tileEntity.redstone = world.isBlockIndirectlyGettingPowered(x, y, z);
        
        if(tileEntity instanceof IBoundingBlock)
        {
        	((IBoundingBlock)tileEntity).onPlace();
        }
    }
	
	@Override
	public void breakBlock(World world, int x, int y, int z, int i1, int i2)
	{
		TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);

		if(tileEntity instanceof IBoundingBlock)
		{
			((IBoundingBlock)tileEntity).onBreak();
		}

		super.breakBlock(world, x, y, z, i1, i2);
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random)
    {
    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
    	
        if(MekanismUtils.isActive(world, x, y, z) && ((IActiveState)tileEntity).renderUpdate())
        {
            float xRandom = (float)x + 0.5F;
            float yRandom = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float)z + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            
            int side = tileEntity.facing;
            
            if(tileEntity instanceof TileEntityMetallurgicInfuser)
            {
            	side = ForgeDirection.getOrientation(side).getOpposite().ordinal();
            }

            if(side == 4)
            {
                world.spawnParticle("smoke", (xRandom - iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (xRandom - iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
            }
            else if(side == 5)
            {
                world.spawnParticle("smoke", (xRandom + iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (xRandom + iRandom), yRandom, (zRandom + jRandom), 0.0D, 0.0D, 0.0D);
            }
            else if(side == 2)
            {
                world.spawnParticle("smoke", (xRandom + jRandom), yRandom, (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (xRandom + jRandom), yRandom, (zRandom - iRandom), 0.0D, 0.0D, 0.0D);
            }
            else if(side == 3)
            {
                world.spawnParticle("smoke", (xRandom + jRandom), yRandom, (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (xRandom + jRandom), yRandom, (zRandom + iRandom), 0.0D, 0.0D, 0.0D);
            }
        }
    }
	
	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		if(tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
			{
				return 15;
			}
		}
		
		return 0;
	}
    
	@Override
	@SideOnly(Side.CLIENT)
    public Icon getIcon(int side, int meta)
    {
		if(blockID == Mekanism.machineBlockID)
		{
	    	if(meta == 0)
	    	{
	        	if(side == 3)
	        	{
	        		return icons[0][0];
	        	}
	        	else {
	        		return icons[0][2];
	        	}
	    	}
	    	else if(meta == 1)
	    	{
	        	if(side == 3)
	        	{
	        		return icons[1][0];
	        	}
	        	else {
	        		return icons[1][2];
	        	}
	    	}
	    	else if(meta == 2)
	    	{
	        	if(side == 3)
	        	{
	        		return icons[2][0];
	        	}
	        	else {
	        		return icons[2][2];
	        	}
	    	}
	    	else if(meta == 3)
	    	{
	        	if(side == 3)
	        	{
	        		return icons[3][0];
	        	}
	        	else {
	        		return icons[3][2];
	        	}
	    	}
	    	else if(meta == 5)
	    	{
	    		if(side == 3)
	    		{
	    			return icons[5][0];
	    		}
	     		else if(side == 0 || side == 1)
	    		{
	    			return icons[5][2];
	    		}
	    		else {
	    			return icons[5][1];
	    		}
	    	}
	    	else if(meta == 6)
	    	{
	    		if(side == 3)
	    		{
	    			return icons[6][0];
	    		}
	     		else if(side == 0 || side == 1)
	    		{
	    			return icons[6][2];
	    		}
	    		else {
	    			return icons[6][1];
	    		}
	    	}
	    	else if(meta == 7)
	    	{
	    		if(side == 3)
	    		{
	    			return icons[7][0];
	    		}
	     		else if(side == 0 || side == 1)
	    		{
	    			return icons[7][2];
	    		}
	    		else {
	    			return icons[7][1];
	    		}
	    	}
	    	else if(meta == 9)
	    	{
	    		if(side == 3)
	    		{
	    			return icons[9][0];
	    		}
	    		else {
	    			return icons[9][2];
	    		}
	    	}
	    	else if(meta == 10)
	    	{
	    		if(side == 3)
	    		{
	    			return icons[10][0];
	    		}
	    		else {
	    			return icons[10][2];
	    		}
	    	}
	    	else if(meta == 11)
	    	{
	    		return icons[11][0];
	    	}
		}
		else if(blockID == Mekanism.machineBlock2ID)
		{
			if(meta == 2)
			{
				if(side == 3)
				{
					return icons[2][0];
				}
				else {
					return icons[2][2];
				}
			}
			else if(meta == 3)
			{
				if(side == 3)
				{
					return icons[3][0];
				}
				else {
					return icons[3][2];
				}
			}
			else if(meta == 4)
			{
				if(side == 3)
				{
					return icons[4][0];
				}
				else {
					return icons[4][2];
				}
			}
		}
    	
    	return null;
    }
    
	@Override
    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
        
    	if(blockID == Mekanism.machineBlockID)
    	{
	    	if(metadata == 0)
	    	{
		        if(side == tileEntity.facing)
		        {
		        	return MekanismUtils.isActive(world, x, y, z) ? icons[0][1] : icons[0][0];
		        }
		        else {
		        	return icons[0][2];
		        }
	    	}
	    	else if(metadata == 1)
	    	{
	            if(side == tileEntity.facing)
	            {
	            	return MekanismUtils.isActive(world, x, y, z) ? icons[1][1] : icons[1][0];
	            }
	            else {
	            	return icons[1][2];
	            }
	    	}
	    	else if(metadata == 2)
	    	{
	            if(side == tileEntity.facing)
	            {
	            	return MekanismUtils.isActive(world, x, y, z) ? icons[2][1] : icons[2][0];
	            }
	            else {
	            	return icons[2][2];
	            }
	    	}
	    	else if(metadata == 3)
	    	{
	            if(side == tileEntity.facing)
	            {
	            	return MekanismUtils.isActive(world, x, y, z) ? icons[3][1] : icons[3][0];
	            }
	            else {
	            	return icons[3][2];
	            }
	    	}
	    	else if(metadata == 5)
	    	{
	    		if(side == tileEntity.facing)
	    		{
	    			return icons[5][0];
	    		}
	    		else if(side == 0 || side == 1)
	    		{
	    			return icons[5][2];
	    		}
	    		else {
	    			return icons[5][1];
	    		}
	    	}
	    	else if(metadata == 6)
	    	{
	    		if(side == tileEntity.facing)
	    		{
	    			return icons[6][0];
	    		}
	       		else if(side == 0 || side == 1)
	    		{
	    			return icons[6][2];
	    		}
	    		else {
	    			return icons[6][1];
	    		}
	    	}
	    	else if(metadata == 7)
	    	{
	    		if(side == tileEntity.facing)
	    		{
	    			return icons[7][0];
	    		}
	       		else if(side == 0 || side == 1)
	    		{
	    			return icons[7][2];
	    		}
	    		else {
	    			return icons[7][1];
	    		}
	    	}
	    	else if(metadata == 9)
	    	{
	    		if(side == tileEntity.facing)
	    		{
	    			return MekanismUtils.isActive(world, x, y, z) ? icons[9][1] : icons[9][0];
	    		}
	    		else {
	    			return icons[9][2];
	    		}
	    	}
	    	else if(metadata == 10)
	    	{
	    		if(side == tileEntity.facing)
	    		{
	    			return MekanismUtils.isActive(world, x, y, z) ? icons[10][1] : icons[10][0];
	    		}
	    		else {
	    			return icons[10][2];
	    		}
	    	}
	    	else if(metadata == 11)
	    	{
	    		return icons[11][0];
	    	}
    	}
    	else if(blockID == Mekanism.machineBlock2ID)
    	{
    		if(metadata == 2 || metadata == 3 || metadata == 4)
	    	{
	    		if(side == tileEntity.facing)
	    		{
	    			return MekanismUtils.isActive(world, x, y, z) ? icons[metadata][1] : icons[metadata][0];
	    		}
	    		else {
	    			return icons[metadata][2];
	    		}
	    	}
    	}
    	
    	return null;
    }
    
	@Override
    public int damageDropped(int i)
    {
    	return i;
    }
    
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		for(MachineType type : MachineType.values())
		{
			if(type == MachineType.CHEMICAL_OXIDIZER || type == MachineType.CHEMICAL_INFUSER /*TODO*/)
			{
				//continue;
			}
			
			if(type.typeId == blockID)
			{
				if(type == MachineType.BASIC_FACTORY || type == MachineType.ADVANCED_FACTORY || type == MachineType.ELITE_FACTORY)
				{
					for(RecipeType recipe : RecipeType.values())
					{
						ItemStack stack = new ItemStack(i, 1, type.meta);
						((IFactory)stack.getItem()).setRecipeType(recipe.ordinal(), stack);
						list.add(stack);
					}
				}
				else {
					list.add(new ItemStack(i, 1, type.meta));
				}
			}
		}
	}
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int facing, float posX, float posY, float posZ)
    {
		if(ItemAttacher.canAttach(entityplayer.getCurrentEquippedItem()))
		{
			return false;
		}
		
    	if(world.isRemote)
    	{
    		return true;
    	}
    	
    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(entityplayer.getCurrentEquippedItem() != null)
    	{
    		Item tool = entityplayer.getCurrentEquippedItem().getItem();
    		
	    	if(tool instanceof IToolWrench && !tool.getUnlocalizedName().contains("omniwrench"))
	    	{
	    		if(((IToolWrench)tool).canWrench(entityplayer, x, y, z))
	    		{
		    		if(entityplayer.isSneaking() && metadata != 13)
		    		{
		    			dismantleBlock(world, x, y, z, false);
		    			return true;
		    		}
		    		
		    		((IToolWrench)tool).wrenchUsed(entityplayer, x, y, z);
		    		
		    		int change = 0;
		    		
		    		switch(tileEntity.facing)
		    		{
		    			case 3:
		    				change = 5;
		    				break;
		    			case 5:
		    				change = 2;
		    				break;
		    			case 2:
		    				change = 4;
		    				break;
		    			case 4:
		    				change = 3;
		    				break;
		    		}
		    		
			        if(tileEntity instanceof TileEntityLogisticalSorter)
			        {
			        	if(!((TileEntityLogisticalSorter)tileEntity).hasInventory())
			        	{
				        	for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
							{
								TileEntity tile = Coord4D.get(tileEntity).getFromSide(dir).getTileEntity(world);
			
								if(tileEntity instanceof IInventory)
								{
									change = dir.getOpposite().ordinal();
									break;
								}
							}
			        	}
			        }
		    		
		    		tileEntity.setFacing((short)change);
		    		world.notifyBlocksOfNeighborChange(x, y, z, blockID);
		    		return true;
	    		}
	    	}
    	}
    	
        if(tileEntity != null)
        {
        	if(metadata == MachineType.ELECTRIC_CHEST.meta)
        	{
        		TileEntityElectricChest electricChest = (TileEntityElectricChest)tileEntity;
        		
        	 	if(!entityplayer.isSneaking())
	        	{
        	 		if(electricChest.canAccess())
        	 		{
        	 			MekanismUtils.openElectricChestGui((EntityPlayerMP)entityplayer, electricChest, null, true);
        	 		}
        	 		else if(!electricChest.authenticated)
        	 		{
        	 			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketElectricChest().setParams(ElectricChestPacketType.CLIENT_OPEN, 2, 0, true, Coord4D.get(electricChest)), entityplayer);
        	 		}
        	 		else {
        	 			PacketHandler.sendPacket(Transmission.SINGLE_CLIENT, new PacketElectricChest().setParams(ElectricChestPacketType.CLIENT_OPEN, 1, 0, true, Coord4D.get(electricChest)), entityplayer);
        	 		}
        	 		
	        		return true;
	        	}
        	}
        	else if(metadata == MachineType.LOGISTICAL_SORTER.meta)
        	{
        		TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)tileEntity;
        		PacketLogisticalSorterGui.openServerGui(SorterGuiPacket.SERVER, 0, world, (EntityPlayerMP)entityplayer, Coord4D.get(tileEntity), -1);
        		return true;
        	}
        	else {
            	if(!entityplayer.isSneaking() && MachineType.get(blockID, metadata).guiId != -1)
	        	{
	        		entityplayer.openGui(Mekanism.instance, MachineType.get(blockID, metadata).guiId, world, x, y, z);
	        		return true;
	        	}
        	}
        }
    	return false;
    }
    
    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
    	if(MachineType.get(blockID, metadata) == null)
    	{
    		return null;
    	}
    	
    	return MachineType.get(blockID, metadata).create();
    }
	
    @Override
	public TileEntity createNewTileEntity(World world)
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
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return ClientProxy.MACHINE_RENDER_ID;
	}
	
	@Override
    public float getBlockHardness(World world, int x, int y, int z)
    {
		if(world.getBlockMetadata(x, y, z) != 13)
		{
			return blockHardness;
		}
		else {
			TileEntityElectricChest tileEntity = (TileEntityElectricChest)world.getBlockTileEntity(x, y, z);
			return tileEntity.canAccess() ? 3.5F : -1;
		}
    }
	
    @Override
    public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
    	if(!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z)))
    	{
	    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
	    	
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            
            EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, getPickBlock(null, world, x, y, z));
	        
	        world.spawnEntityInWorld(entityItem);
    	}
    	
        return world.setBlockToAir(x, y, z);
    }
    
    @Override
    public int idDropped(int i, Random random, int j)
    {
    	return 0;
    }
    
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id) 
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			
			if(tileEntity instanceof TileEntityBasicBlock)
			{
				((TileEntityBasicBlock)tileEntity).onNeighborChange(x, y, z, id);
			}
			
			if(tileEntity instanceof TileEntityLogisticalSorter)
			{
				TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)tileEntity;
				
        		if(!sorter.hasInventory())
        		{
		        	for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
					{
						TileEntity tile = Coord4D.get(tileEntity).getFromSide(dir).getTileEntity(world);
		
						if(tile instanceof IInventory)
						{
							sorter.setFacing((short)dir.getOpposite().ordinal());
							return;
						}
					}
	        	}
	        }
		}
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
    	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
    	ItemStack itemStack = new ItemStack(blockID, 1, world.getBlockMetadata(x, y, z));
    	
    	if(itemStack.stackTagCompound == null)
    	{
    		itemStack.setTagCompound(new NBTTagCompound());
    	}
    	
    	if(((IUpgradeManagement)itemStack.getItem()).supportsUpgrades(itemStack))
    	{
	        IUpgradeManagement upgrade = (IUpgradeManagement)itemStack.getItem();
	        
	        upgrade.setEnergyMultiplier(((IUpgradeManagement)tileEntity).getEnergyMultiplier(), itemStack);
	        upgrade.setSpeedMultiplier(((IUpgradeManagement)tileEntity).getSpeedMultiplier(), itemStack);
    	}
    	
    	if(tileEntity instanceof IInvConfiguration)
    	{
    		IInvConfiguration config = (IInvConfiguration)tileEntity;
    		
    		itemStack.stackTagCompound.setBoolean("hasSideData", true);
    		
    		itemStack.stackTagCompound.setBoolean("ejecting", config.getEjector().isEjecting());
    		
            for(int i = 0; i < 6; i++)
            {
            	itemStack.stackTagCompound.setByte("config"+i, config.getConfiguration()[i]);
            }
    	}
    	
    	if(tileEntity instanceof TileEntityDigitalMiner)
    	{
    		TileEntityDigitalMiner miner = (TileEntityDigitalMiner)tileEntity;
    		
    		itemStack.stackTagCompound.setBoolean("hasMinerConfig", true);
    		
            itemStack.stackTagCompound.setInteger("radius", miner.radius);
            itemStack.stackTagCompound.setInteger("minY", miner.minY);
            itemStack.stackTagCompound.setInteger("maxY", miner.maxY);
            itemStack.stackTagCompound.setBoolean("doEject", miner.doEject);
            itemStack.stackTagCompound.setBoolean("doPull", miner.doPull);
            itemStack.stackTagCompound.setBoolean("silkTouch", miner.silkTouch);
            
            if(miner.replaceStack != null)
            {
            	itemStack.stackTagCompound.setCompoundTag("replaceStack", miner.replaceStack.writeToNBT(new NBTTagCompound()));
            }
            
            NBTTagList filterTags = new NBTTagList();
            
            for(MinerFilter filter : miner.filters)
            {
            	filterTags.appendTag(filter.write(new NBTTagCompound()));
            }
            
            if(filterTags.tagCount() != 0)
            {
            	itemStack.stackTagCompound.setTag("filters", filterTags);
            }
    	}
    	
    	if(tileEntity instanceof TileEntityLogisticalSorter)
    	{
    		TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter)tileEntity;
    		
    		itemStack.stackTagCompound.setBoolean("hasSorterConfig", true);
            
            if(sorter.color != null)
            {
            	itemStack.stackTagCompound.setInteger("color", TransporterUtils.colors.indexOf(sorter.color));
            }
            
            itemStack.stackTagCompound.setBoolean("autoEject", sorter.autoEject);
            itemStack.stackTagCompound.setBoolean("roundRobin", sorter.roundRobin);
            
            NBTTagList filterTags = new NBTTagList();
            
            for(TransporterFilter filter : sorter.filters)
            {
            	NBTTagCompound tagCompound = new NBTTagCompound();
            	filter.write(tagCompound);
            	filterTags.appendTag(tagCompound);
            }
            
            if(filterTags.tagCount() != 0)
            {
            	itemStack.stackTagCompound.setTag("filters", filterTags);
            }
    	}
    	
    	if(tileEntity instanceof IRedstoneControl)
    	{
    		IRedstoneControl control = (IRedstoneControl)tileEntity;
    		itemStack.stackTagCompound.setInteger("controlType", control.getControlType().ordinal());
    	}
        
    	if(tileEntity instanceof TileEntityElectricBlock)
    	{
	        IEnergizedItem energizedItem = (IEnergizedItem)itemStack.getItem();
	        energizedItem.setEnergy(itemStack, ((TileEntityElectricBlock)tileEntity).electricityStored);
    	}
        
    	if(tileEntity instanceof TileEntityContainerBlock && ((TileEntityContainerBlock)tileEntity).inventory.length > 0)
    	{
	        ISustainedInventory inventory = (ISustainedInventory)itemStack.getItem();
	        inventory.setInventory(((ISustainedInventory)tileEntity).getInventory(), itemStack);
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
        
        if(tileEntity instanceof TileEntityElectricChest)
        {
        	IElectricChest electricChest = (IElectricChest)itemStack.getItem();
        	electricChest.setAuthenticated(itemStack, ((TileEntityElectricChest)tileEntity).authenticated);
        	electricChest.setLocked(itemStack, ((TileEntityElectricChest)tileEntity).locked);
        	electricChest.setPassword(itemStack, ((TileEntityElectricChest)tileEntity).password);
        }
        
        if(tileEntity instanceof TileEntityFactory)
        {
        	IFactory factoryItem = (IFactory)itemStack.getItem();
        	factoryItem.setRecipeType(((TileEntityFactory)tileEntity).recipeType, itemStack);
        }
        
        if(tileEntity instanceof TileEntityRotaryCondensentrator)
        {
        	TileEntityRotaryCondensentrator condensentrator = (TileEntityRotaryCondensentrator)tileEntity;
        	
        	if(condensentrator.gasTank.getGas() != null)
        	{
        		itemStack.stackTagCompound.setCompoundTag("gasStack", condensentrator.gasTank.getGas().write(new NBTTagCompound()));
        	}
        }
        
        if(tileEntity instanceof TileEntityChemicalOxidizer)
        {
        	TileEntityChemicalOxidizer formulator = (TileEntityChemicalOxidizer)tileEntity;
        	
        	if(formulator.gasTank.getGas() != null)
        	{
        		itemStack.stackTagCompound.setCompoundTag("gasTank", formulator.gasTank.getGas().write(new NBTTagCompound()));
        	}
        }
        
        if(tileEntity instanceof TileEntityChemicalInfuser)
        {
        	TileEntityChemicalInfuser infuser = (TileEntityChemicalInfuser)tileEntity;
        	
        	if(infuser.leftTank.getGas() != null)
        	{
        		itemStack.stackTagCompound.setCompoundTag("leftTank", infuser.leftTank.getGas().write(new NBTTagCompound()));
        	}
        	
        	if(infuser.rightTank.getGas() != null)
        	{
        		itemStack.stackTagCompound.setCompoundTag("rightTank", infuser.rightTank.getGas().write(new NBTTagCompound()));
        	}
        	
        	if(infuser.centerTank.getGas() != null)
        	{
        		itemStack.stackTagCompound.setCompoundTag("leftTank", infuser.centerTank.getGas().write(new NBTTagCompound()));
        	}
        }
        
        return itemStack;
	}
	
	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock) 
	{
		ItemStack itemStack = getPickBlock(null, world, x, y, z);
        
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
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) 
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(metadata == MachineType.CHARGEPAD.meta)
    	{
    		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.06F, 1.0F);
    	}
    	else {
    		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    	}
    }
    
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
    	if(world.getBlockTileEntity(x, y, z) instanceof TileEntityChargepad)
    	{
    		return null;
    	}
    	
    	return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }
    
    @Override
    public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(metadata != MachineType.CHARGEPAD.meta)
    	{
    		return true;
    	}
    	
    	return false;
    }
    
	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if(!world.isRemote)
		{
			((TileEntityElectricBlock)tileEntity).register();
		}
	}
	
	public static enum MachineType
	{
		ENRICHMENT_CHAMBER(Mekanism.machineBlockID, 0, "EnrichmentChamber", 3, Mekanism.enrichmentChamberUsage*400, TileEntityEnrichmentChamber.class, false, true),
		OSMIUM_COMPRESSOR(Mekanism.machineBlockID, 1, "OsmiumCompressor", 4, Mekanism.osmiumCompressorUsage*400, TileEntityOsmiumCompressor.class, false, true),
		COMBINER(Mekanism.machineBlockID, 2, "Combiner", 5, Mekanism.combinerUsage*400, TileEntityCombiner.class, false, true),
		CRUSHER(Mekanism.machineBlockID, 3, "Crusher", 6, Mekanism.crusherUsage*400, TileEntityCrusher.class, false, true),
		DIGITAL_MINER(Mekanism.machineBlockID, 4, "DigitalMiner", 2, 100000, TileEntityDigitalMiner.class, true, true),
		BASIC_FACTORY(Mekanism.machineBlockID, 5, "BasicFactory", 11, Mekanism.factoryUsage*3*400, TileEntityFactory.class, false, true),
		ADVANCED_FACTORY(Mekanism.machineBlockID, 6, "AdvancedFactory", 11, Mekanism.factoryUsage*5*400, TileEntityAdvancedFactory.class, false, true),
		ELITE_FACTORY(Mekanism.machineBlockID, 7, "EliteFactory", 11, Mekanism.factoryUsage*7*400, TileEntityEliteFactory.class, false, true),
		METALLURGIC_INFUSER(Mekanism.machineBlockID, 8, "MetallurgicInfuser", 12, Mekanism.metallurgicInfuserUsage*400, TileEntityMetallurgicInfuser.class, true, true),
		PURIFICATION_CHAMBER(Mekanism.machineBlockID, 9, "PurificationChamber", 15, Mekanism.purificationChamberUsage*400, TileEntityPurificationChamber.class, false, true),
		ENERGIZED_SMELTER(Mekanism.machineBlockID, 10, "EnergizedSmelter", 16, Mekanism.energizedSmelterUsage*400, TileEntityEnergizedSmelter.class, false, true),
		TELEPORTER(Mekanism.machineBlockID, 11, "Teleporter", 13, 5000000, TileEntityTeleporter.class, false, false),
		ELECTRIC_PUMP(Mekanism.machineBlockID, 12, "ElectricPump", 17, 10000, TileEntityElectricPump.class, true, false),
		ELECTRIC_CHEST(Mekanism.machineBlockID, 13, "ElectricChest", -1, 12000, TileEntityElectricChest.class, true, false),
		CHARGEPAD(Mekanism.machineBlockID, 14, "Chargepad", -1, 10000, TileEntityChargepad.class, true, false),
		LOGISTICAL_SORTER(Mekanism.machineBlockID, 15, "LogisticalSorter", -1, 0, TileEntityLogisticalSorter.class, true, false),
		ROTARY_CONDENSENTRATOR(Mekanism.machineBlock2ID, 0, "RotaryCondensentrator", 7, 20000, TileEntityRotaryCondensentrator.class, true, false),
		CHEMICAL_OXIDIZER(Mekanism.machineBlock2ID, 1, "ChemicalOxidizer", 29, 20000, TileEntityChemicalOxidizer.class, true, false),
		CHEMICAL_INFUSER(Mekanism.machineBlock2ID, 2, "ChemicalInfuser", 30, 20000, TileEntityChemicalInfuser.class, true, false),
		CHEMICAL_INJECTION_CHAMBER(Mekanism.machineBlock2ID, 3, "ChemicalInjectionChamber", 31, Mekanism.chemicalInjectionChamberUsage*400, TileEntityChemicalInjectionChamber.class, false, true),
		ELECTROLYTIC_SEPARATOR(Mekanism.machineBlock2ID, 5, "ElectrolyticSeparator", 32, 20000, TileEntityElectrolyticSeparator.class, true, false);


		public int typeId;
		public int meta;
		public String name;
		public int guiId;
		public double baseEnergy;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean hasModel;
		public boolean supportsUpgrades;
		
		private MachineType(int id, int i, String s, int j, double k, Class<? extends TileEntity> tileClass, boolean model, boolean upgrades)
		{
			typeId = id;
			meta = i;
			name = s;
			guiId = j;
			baseEnergy = k;
			tileEntityClass = tileClass;
			hasModel = model;
			supportsUpgrades = upgrades;
		}
		
		public static MachineType get(int id, int meta)
		{
			for(MachineType type : values())
			{
				if(type.meta == meta && type.typeId == id)
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
				System.err.println("[Mekanism] Unable to indirectly create tile entity.");
				e.printStackTrace();
				return null;
			}
		}
		
		public String getDescription()
		{
			return MekanismUtils.localize("tooltip." + name);
		}
		
		public ItemStack getStack()
		{
			return new ItemStack(typeId, 1, meta);
		}
		
		public static MachineType get(ItemStack stack)
		{
			return get(stack.itemID, stack.getItemDamage());
		}
		
		@Override
		public String toString()
		{
			return Integer.toString(meta);
		}
	}

	@Override
	public void setRenderBounds(Block block, int metadata) {}

	@Override
	public boolean doDefaultBoundSetting(int metadata) 
	{
		return false;
	}
}
