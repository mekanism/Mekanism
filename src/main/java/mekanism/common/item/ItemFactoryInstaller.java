package mekanism.common.item;

import java.util.List;

import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.tile.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.TileEntityBasicBlock;
import mekanism.common.tile.TileEntityElectricMachine;
import mekanism.common.tile.TileEntityFactory;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemFactoryInstaller extends ItemMekanism
{
	public IIcon[] icons = new IIcon[256];
	
	public ItemFactoryInstaller()
	{
		super();
		setMaxStackSize(1);
		setHasSubtypes(true);
	}
	
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(world.isRemote) 
		{
			return false;
		}
		
		TileEntity tile = world.getTileEntity(x, y, z);
		FactoryTier tier = FactoryTier.values()[stack.getItemDamage()];
		
		if(tile instanceof TileEntityBasicBlock && ((TileEntityBasicBlock)tile).playersUsing.size() > 0)
		{
			return true;
		}
		
		if(tile instanceof TileEntityFactory && tier != FactoryTier.BASIC)
		{
			TileEntityFactory factory = (TileEntityFactory)tile;
			
			if(factory.tier.ordinal()+1 == tier.ordinal())
			{
				if(!world.isRemote)
				{
					factory.upgrade();
				}
				
				if(!player.capabilities.isCreativeMode)
				{
					stack.stackSize = 0;
				}
				
				return true;
			}
		}
		else if(tile != null && tier == FactoryTier.BASIC)
		{
			RecipeType type = null;
			
			for(RecipeType iterType : RecipeType.values())
			{
				ItemStack machineStack = iterType.getStack();
				
				if(Block.getBlockFromItem(machineStack.getItem()) == world.getBlock(x, y, z) && machineStack.getItemDamage() == world.getBlockMetadata(x, y, z))
				{
					type = iterType;
					break;
				}
			}
			
			if(type != null)
			{
				if(tile instanceof TileEntityElectricMachine)
				{
					((TileEntityElectricMachine)tile).upgrade(type);
					
					if(!player.capabilities.isCreativeMode)
					{
						stack.stackSize = 0;
					}
					
					return true;
				}
				else if(tile instanceof TileEntityAdvancedElectricMachine)
				{
					((TileEntityAdvancedElectricMachine)tile).upgrade(type);
					
					if(!player.capabilities.isCreativeMode)
					{
						stack.stackSize = 0;
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	private int getOutputSlot(FactoryTier tier, int operation)
	{
		return 5+tier.processes+operation;
	}
	
	@Override
	public void registerIcons(IIconRegister register)
	{
		for(FactoryTier tier : FactoryTier.values())
		{
			icons[tier.ordinal()] = register.registerIcon("mekanism:" + tier.getBaseTier().getName() + "FactoryInstaller");
		}
	}
	
	@Override
	public IIcon getIconFromDamage(int meta)
	{
		return icons[meta];
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List itemList)
	{
		for(FactoryTier tier : FactoryTier.values())
		{
			itemList.add(new ItemStack(item, 1, tier.ordinal()));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item)
	{
		return "item." + BaseTier.values()[item.getItemDamage()].getName().toLowerCase() + "FactoryInstaller";
	}
}
