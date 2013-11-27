package mekanism.common.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mekanism.api.Object3D;
import mekanism.client.ClientProxy;
import mekanism.common.ConnectedTextureRenderer;
import mekanism.common.IActiveState;
import mekanism.common.IBoundingBlock;
import mekanism.common.ItemAttacher;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.TankUpdateProtocol;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tileentity.TileEntityBasicBlock;
import mekanism.common.tileentity.TileEntityBin;
import mekanism.common.tileentity.TileEntityDynamicTank;
import mekanism.common.tileentity.TileEntityDynamicValve;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple metal block IDs.
 * 0: Osmium Block
 * 1: Bronze Block
 * 2: Refined Obsidian
 * 3: Coal Block
 * 4: Refined Glowstone
 * 5: Steel Block
 * 6: Bin
 * 7: Teleporter Frame
 * 8: Steel Casing
 * 9: Dynamic Tank
 * 10: Dynamic Glass
 * 11: Dynamic Valve
 * @author AidanBrady
 *
 */
public class BlockBasic extends Block
{
	public Icon[][] icons = new Icon[256][6];
	
	public ConnectedTextureRenderer glassRenderer = new ConnectedTextureRenderer("glass/DynamicGlass", blockID, 10);
	
	public BlockBasic(int id)
	{
		super(id, Material.iron);
		setHardness(5F);
		setResistance(10F);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id) 
	{
		if(!world.isRemote)
		{
			if(id == blockID && world.getBlockTileEntity(x, y, z) instanceof TileEntityDynamicTank)
			{
				TileEntityDynamicTank dynamicTank = (TileEntityDynamicTank)world.getBlockTileEntity(x, y, z);
				
				dynamicTank.update();
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register)
	{
		icons[0][0] = register.registerIcon("mekanism:OsmiumBlock");
		icons[1][0] = register.registerIcon("mekanism:BronzeBlock");
		icons[2][0] = register.registerIcon("mekanism:RefinedObsidian");
		icons[3][0] = register.registerIcon("mekanism:CoalBlock");
		icons[4][0] = register.registerIcon("mekanism:RefinedGlowstone");
		icons[5][0] = register.registerIcon("mekanism:SteelBlock");
		icons[6][0] = register.registerIcon("mekanism:BinSide");
		icons[6][1] = register.registerIcon("mekanism:BinTop");
		icons[6][2] = register.registerIcon("mekanism:BinFront");
		icons[6][3] = register.registerIcon("mekanism:BinTopOn");
		icons[6][4] = register.registerIcon("mekanism:BinFrontOn");
		icons[7][0] = register.registerIcon("mekanism:TeleporterFrame");
		icons[8][0] = register.registerIcon("mekanism:SteelCasing");
		icons[9][0] = register.registerIcon("mekanism:DynamicTank");
		icons[10][0] = register.registerIcon("mekanism:DynamicGlass");
		icons[11][0] = register.registerIcon("mekanism:DynamicValve");
		
		glassRenderer.registerIcons(register);
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(metadata == 6)
    	{
    		TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
    		
    		if(side == 0 || side == 1)
			{
				return MekanismUtils.isActive(world, x, y, z) ? icons[6][3] : icons[6][1];
			}
			else if(side == tileEntity.facing)
			{
				return MekanismUtils.isActive(world, x, y, z) ? icons[6][4] : icons[6][2];
			}
			else {
				return icons[6][0];
			}
    	}
    	else if(metadata == 10)
    	{
    		return glassRenderer.getIcon(world, x, y, z, side);
    	}
    	else {
     		return getIcon(side, metadata);
    	}
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		if(meta != 6)
		{
			return icons[meta][0];
		}
		else {
			if(side == 0 || side == 1)
			{
				return icons[6][1];
			}
			else if(side == 3)
			{
				return icons[6][2];
			}
			else {
				return icons[6][0];
			}
		}
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
		list.add(new ItemStack(i, 1, 0));
		list.add(new ItemStack(i, 1, 1));
		list.add(new ItemStack(i, 1, 2));
		list.add(new ItemStack(i, 1, 3));
		list.add(new ItemStack(i, 1, 4));
		list.add(new ItemStack(i, 1, 5));
		list.add(new ItemStack(i, 1, 6));
		list.add(new ItemStack(i, 1, 7));
		list.add(new ItemStack(i, 1, 8));
		list.add(new ItemStack(i, 1, 9));
		list.add(new ItemStack(i, 1, 10));
		list.add(new ItemStack(i, 1, 11));
	}
	
	@Override
    public boolean canCreatureSpawn(EnumCreatureType type, World world, int x, int y, int z)
    {
        int meta = world.getBlockMetadata(x, y, z);
        
        if(meta == 9 || meta == 10 || meta == 11)
        {
        	TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)world.getBlockTileEntity(x, y, z);
        	
        	if(tileEntity != null)
        	{
        		if(!world.isRemote)
        		{
        			if(tileEntity.structure != null)
        			{
        				return false;
        			}
        		}
        		else {
        			if(tileEntity.clientHasStructure)
        			{
        				return false;
        			}
        		}
        	}
        }
        
        return super.canCreatureSpawn(type, world, x, y, z);
    }
	
	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player)
	{
		int meta = world.getBlockMetadata(x, y, z);
		
		if(!world.isRemote && meta == 6)
		{			
			TileEntityBin bin = (TileEntityBin)world.getBlockTileEntity(x, y, z);
			MovingObjectPosition pos = MekanismUtils.rayTrace(world, player);
			
			if(pos != null && pos.sideHit == bin.facing)
			{
				if(bin.getStack() != null)
				{
					if(!player.isSneaking())
					{
						world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, bin.removeStack().copy()));
					}
					else {
						world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, bin.remove(1).copy()));
					}
				}
			}
		}
	}
	
	@Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
		if(ItemAttacher.canAttach(entityplayer.getCurrentEquippedItem()))
		{
			return false;
		}
		
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(metadata == 2)
    	{
    		if(entityplayer.isSneaking())
    		{
    			entityplayer.openGui(Mekanism.instance, 1, world, x, y, z);
    			return true;
    		}
    	}
    	
    	if(world.isRemote)
    	{
    		return true;
    	}
    	
    	if(metadata == 6)
    	{
    		TileEntityBin bin = (TileEntityBin)world.getBlockTileEntity(x, y, z);
    		
    		if(bin.itemCount < bin.MAX_STORAGE)
    		{
	    		if(bin.addTicks == 0)
	    		{
	    			if(entityplayer.getCurrentEquippedItem() != null)
	    			{
		    			ItemStack remain = bin.add(entityplayer.getCurrentEquippedItem());
		    			entityplayer.setCurrentItemOrArmor(0, remain);
		    			bin.addTicks = 5;
	    			}
	    		}
	    		else {
	    			ItemStack[] inv = entityplayer.inventory.mainInventory;
	    			
	    			for(int i = 0; i < inv.length; i++)
	    			{
	    				if(bin.itemCount == bin.MAX_STORAGE)
	    				{
	    					break;
	    				}
	    				
	    				if(inv[i] != null)
	    				{
	    					ItemStack remain = bin.add(inv[i]);
	    					inv[i] = remain;
	    				}
	    				
		    			((EntityPlayerMP)entityplayer).sendContainerAndContentsToPlayer(entityplayer.openContainer, entityplayer.openContainer.getInventory());
	    			}
	    		}
    		}
    		
    		return true;
    	}
    	else if(metadata == 9 || metadata == 10 || metadata == 11)
    	{
			if(!entityplayer.isSneaking() && ((TileEntityDynamicTank)world.getBlockTileEntity(x, y, z)).structure != null)
			{
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)world.getBlockTileEntity(x, y, z);
				
				if(!manageInventory(entityplayer, tileEntity))
				{
					PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity().setParams(Object3D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())));
					entityplayer.openGui(Mekanism.instance, 18, world, x, y, z);
				}
				else {
					entityplayer.inventory.onInventoryChanged();
					tileEntity.sendPacketToRenderer();
				}
				
				return true;
			}
    	}
    	
        return false;
    }
	
	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side)
	{
		return world.getBlockMetadata(x, y, z) != 10;
	}
	
	private boolean manageInventory(EntityPlayer player, TileEntityDynamicTank tileEntity)
	{
		ItemStack itemStack = player.getCurrentEquippedItem();
		
		if(itemStack != null && tileEntity.structure != null)
		{
			if(FluidContainerRegistry.isEmptyContainer(itemStack))
			{
				if(tileEntity.structure.fluidStored != null && tileEntity.structure.fluidStored.amount >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(tileEntity.structure.fluidStored, itemStack);
					
					if(filled != null)
					{
						if(player.capabilities.isCreativeMode)
						{
							tileEntity.structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;
							
							if(tileEntity.structure.fluidStored.amount == 0)
							{
								tileEntity.structure.fluidStored = null;
							}
							
							return true;
						}
						
						if(itemStack.stackSize > 1)
						{
							for(int i = 0; i < player.inventory.mainInventory.length; i++)
							{
								if(player.inventory.mainInventory[i] == null)
								{
									player.inventory.mainInventory[i] = filled;
									itemStack.stackSize--;
									
									tileEntity.structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;
									
									if(tileEntity.structure.fluidStored.amount == 0)
									{
										tileEntity.structure.fluidStored = null;
									}
									
									return true;
								}
								else if(player.inventory.mainInventory[i].isItemEqual(filled))
								{
									if(filled.getMaxStackSize() > player.inventory.mainInventory[i].stackSize)
									{
										player.inventory.mainInventory[i].stackSize++;
										itemStack.stackSize--;
										
										tileEntity.structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;
										
										if(tileEntity.structure.fluidStored.amount == 0)
										{
											tileEntity.structure.fluidStored = null;
										}
										
										return true;
									}
								}
							}
						}
						else if(itemStack.stackSize == 1)
						{
							player.setCurrentItemOrArmor(0, filled);
							
							tileEntity.structure.fluidStored.amount -= FluidContainerRegistry.getFluidForFilledItem(filled).amount;
							
							if(tileEntity.structure.fluidStored.amount == 0)
							{
								tileEntity.structure.fluidStored = null;
							}
							
							return true;
						}
					}
				}
			}
			else if(FluidContainerRegistry.isFilledContainer(itemStack))
			{
				FluidStack itemFluid = FluidContainerRegistry.getFluidForFilledItem(itemStack);
				int max = tileEntity.structure.volume*TankUpdateProtocol.FLUID_PER_TANK;
				
				if(tileEntity.structure.fluidStored == null || (tileEntity.structure.fluidStored.isFluidEqual(itemFluid) && (tileEntity.structure.fluidStored.amount+itemFluid.amount <= max)))
				{
					if(FluidContainerRegistry.isBucket(itemStack))
					{
						if(tileEntity.structure.fluidStored == null)
						{
							tileEntity.structure.fluidStored = itemFluid;
						}
						else {
							tileEntity.structure.fluidStored.amount += itemFluid.amount;
						}
						
						if(!player.capabilities.isCreativeMode)
						{
							player.setCurrentItemOrArmor(0, new ItemStack(Item.bucketEmpty));
						}
						
						return true;
					}
					else {
						if(!player.capabilities.isCreativeMode)
						{
							itemStack.stackSize--;
						}
						
						if(itemStack.stackSize == 0)
						{
							player.setCurrentItemOrArmor(0, null);
						}
						
						if(tileEntity.structure.fluidStored == null)
						{
							tileEntity.structure.fluidStored = itemFluid;
						}
						else {
							tileEntity.structure.fluidStored.amount += itemFluid.amount;
						}
						
						return true;
					}
				}
			}
		}
			
		return false;
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
		return ClientProxy.BASIC_RENDER_ID;
	}
    
	@Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) 
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
        int metadata = world.getBlockMetadata(x, y, z);
		
		if(tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive() && ((IActiveState)tileEntity).lightUpdate())
			{
				return 15;
			}
		}
        
        switch(metadata)
        {
        	case 2:
        		return 8;
        	case 4:
        		return 15;
        	case 7:
        		return 12;
        }
        
        return 0;
    }
	
	@Override
	public boolean hasTileEntity(int metadata)
	{
		return metadata == 6 || metadata == 9 || metadata == 10 || metadata == 11;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		switch(metadata)
		{
			case 6:
				return new TileEntityBin();
			case 9:
				return new TileEntityDynamicTank();
			case 10:
				return new TileEntityDynamicTank();
			case 11:
				return new TileEntityDynamicValve();
		}
		
		return null;
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack itemstack)
	{
		if(world.getBlockTileEntity(x, y, z) instanceof TileEntityBasicBlock)
		{
		   	TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)world.getBlockTileEntity(x, y, z);
	        int side = MathHelper.floor_double((entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
	        int height = Math.round(entityliving.rotationPitch);
	        int change = 3;
	        
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
	        
	        tileEntity.setFacing((short)change);
	        
	        if(tileEntity instanceof IBoundingBlock)
	        {
	        	((IBoundingBlock)tileEntity).onPlace();
	        }
		}
        
		world.markBlockForRenderUpdate(x, y, z);
		world.updateAllLightTypes(x, y, z);
		
		if(!world.isRemote && world.getBlockTileEntity(x, y, z) != null)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			
			if(tileEntity instanceof TileEntityDynamicTank)
			{
				((TileEntityDynamicTank)tileEntity).update();
			}
		}
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		ItemStack ret = new ItemStack(blockID, 1, world.getBlockMetadata(x, y, z));
		
		if(ret.getItemDamage() == 6)
		{
			TileEntityBin tileEntity = (TileEntityBin)world.getBlockTileEntity(x, y, z);
			InventoryBin inv = new InventoryBin(ret);
			
			inv.setItemCount(tileEntity.itemCount);
			
			if(tileEntity.itemCount > 0)
			{
				inv.setItemType(tileEntity.itemType);
			}
		}
		
		return ret;
	}
	
    @Override
    public int idDropped(int i, Random random, int j)
    {
    	return 0;
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
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
	{
		if(world.getBlockMetadata(x, y, z) == 10)
		{
			return glassRenderer.shouldRenderSide(world, x, y, z, side);
		}
		else {
			return super.shouldSideBeRendered(world, x, y, z, side);
		}
	}
}