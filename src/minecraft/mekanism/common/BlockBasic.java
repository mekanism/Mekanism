package mekanism.common;

import java.util.List;

import mekanism.client.ClientProxy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
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
 * 6: Control Panel
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
	public Icon[] icons = new Icon[256];
	
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
			if(world.getBlockTileEntity(x, y, z) instanceof TileEntityDynamicTank)
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
		icons[0] = register.registerIcon("mekanism:OsmiumBlock");
		icons[1] = register.registerIcon("mekanism:BronzeBlock");
		icons[2] = register.registerIcon("mekanism:RefinedObsidian");
		icons[3] = register.registerIcon("mekanism:CoalBlock");
		icons[4] = register.registerIcon("mekanism:RefinedGlowstone");
		icons[5] = register.registerIcon("mekanism:SteelBlock");
		icons[6] = register.registerIcon("mekanism:ControlPanel");
		icons[7] = register.registerIcon("mekanism:TeleporterFrame");
		icons[8] = register.registerIcon("mekanism:SteelCasing");
		icons[9] = register.registerIcon("mekanism:DynamicTank");
		icons[10] = register.registerIcon("mekanism:DynamicGlass");
		icons[11] = register.registerIcon("mekanism:DynamicValve");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta)
	{
		return icons[meta];
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
		//list.add(new ItemStack(i, 1, 6));
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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
		if(world.isRemote)
		{
			return true;
		}
		
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(metadata == 2)
    	{
    		if(entityplayer.isSneaking())
    		{
    			entityplayer.openGui(Mekanism.instance, 19, world, x, y, z);
    			return true;
    		}
    	}
    	else if(metadata == 6)
    	{
    		if(!entityplayer.isSneaking())
    		{
    			entityplayer.openGui(Mekanism.instance, 9, world, x, y, z);
    			return true;
    		}
    	}
    	else if(metadata == 9 || metadata == 10 || metadata == 11)
    	{
			if(!entityplayer.isSneaking() && ((TileEntityDynamicTank)world.getBlockTileEntity(x, y, z)).structure != null)
			{
				TileEntityDynamicTank tileEntity = (TileEntityDynamicTank)world.getBlockTileEntity(x, y, z);
				
				if(!manageInventory(entityplayer, tileEntity))
				{
					entityplayer.openGui(Mekanism.instance, 18, world, x, y, z);
				}
				else {
					tileEntity.sendPacketToRenderer();
				}
				
				return true;
			}
    	}
    	
        return false;
    }
	
	public boolean manageInventory(EntityPlayer player, TileEntityDynamicTank tileEntity)
	{
		ItemStack itemStack = player.getCurrentEquippedItem();
		
		if(itemStack != null && tileEntity.structure != null)
		{
			if(LiquidContainerRegistry.isEmptyContainer(itemStack))
			{
				if(tileEntity.structure.liquidStored != null && tileEntity.structure.liquidStored.amount >= LiquidContainerRegistry.BUCKET_VOLUME)
				{
					ItemStack filled = LiquidContainerRegistry.fillLiquidContainer(tileEntity.structure.liquidStored, itemStack);
					
					if(filled != null)
					{
						if(itemStack.stackSize > 1)
						{
							boolean didMove = false;
							
							for(int i = 0; i < player.inventory.mainInventory.length; i++)
							{
								if(player.inventory.mainInventory[i] == null)
								{
									player.inventory.mainInventory[i] = filled;
									itemStack.stackSize--;
									
									tileEntity.structure.liquidStored.amount -= LiquidContainerRegistry.getLiquidForFilledItem(filled).amount;
									
									if(tileEntity.structure.liquidStored.amount == 0)
									{
										tileEntity.structure.liquidStored = null;
									}
									
									return true;
								}
								else if(player.inventory.mainInventory[i].isItemEqual(filled))
								{
									if(filled.getMaxStackSize() > player.inventory.mainInventory[i].stackSize)
									{
										player.inventory.mainInventory[i].stackSize++;
										itemStack.stackSize--;
										
										tileEntity.structure.liquidStored.amount -= LiquidContainerRegistry.getLiquidForFilledItem(filled).amount;
										
										if(tileEntity.structure.liquidStored.amount == 0)
										{
											tileEntity.structure.liquidStored = null;
										}
										
										return true;
									}
								}
							}
						}
						else if(itemStack.stackSize == 1)
						{
							player.setCurrentItemOrArmor(0, filled);
							
							tileEntity.structure.liquidStored.amount -= LiquidContainerRegistry.getLiquidForFilledItem(filled).amount;
							
							if(tileEntity.structure.liquidStored.amount == 0)
							{
								tileEntity.structure.liquidStored = null;
							}
							
							return true;
						}
					}
				}
			}
			else if(LiquidContainerRegistry.isFilledContainer(itemStack))
			{
				LiquidStack itemLiquid = LiquidContainerRegistry.getLiquidForFilledItem(itemStack);
				int max = tileEntity.structure.volume*16000;
				
				if(tileEntity.structure.liquidStored == null || (tileEntity.structure.liquidStored.amount+itemLiquid.amount <= max))
				{
					if(LiquidContainerRegistry.isBucket(itemStack))
					{
						if(tileEntity.structure.liquidStored == null)
						{
							tileEntity.structure.liquidStored = itemLiquid;
						}
						else {
							tileEntity.structure.liquidStored.amount += itemLiquid.amount;
						}
						
						player.setCurrentItemOrArmor(0, new ItemStack(Item.bucketEmpty));
						return true;
					}
					else {
						itemStack.stackSize--;
						
						if(itemStack.stackSize == 0)
						{
							player.setCurrentItemOrArmor(0, null);
						}
						
						if(tileEntity.structure.liquidStored == null)
						{
							tileEntity.structure.liquidStored = itemLiquid;
						}
						else {
							tileEntity.structure.liquidStored.amount += itemLiquid.amount;
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
        int metadata = world.getBlockMetadata(x, y, z);
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
		    	 return new TileEntityControlPanel();
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
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving, ItemStack itemstack)
	{
		world.markBlockForRenderUpdate(x, y, z);
		world.updateAllLightTypes(x, y, z);
		
		if(world.getBlockTileEntity(x, y, z) != null && !world.isRemote)
		{
			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
			
			if(tileEntity instanceof TileEntityDynamicTank)
			{
				((TileEntityDynamicTank)tileEntity).update();
			}
		}
	}
}