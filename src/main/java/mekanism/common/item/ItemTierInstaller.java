package mekanism.common.item;

import java.util.List;

import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.tile.TileEntityBasicBlock;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemTierInstaller extends ItemMekanism
{
	public IIcon[] icons = new IIcon[256];
	
	public ItemTierInstaller()
	{
		super();
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
		BaseTier tier = BaseTier.values()[stack.getItemDamage()];
		
		if(tile instanceof ITierUpgradeable)
		{
			if(tile instanceof TileEntityBasicBlock && ((TileEntityBasicBlock)tile).playersUsing.size() > 0)
			{
				return true;
			}
			
			if(((ITierUpgradeable)tile).upgrade(tier))
			{
				if(!player.capabilities.isCreativeMode)
				{
					stack.stackSize--;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void registerIcons(IIconRegister register)
	{
		for(FactoryTier tier : FactoryTier.values())
		{
			icons[tier.ordinal()] = register.registerIcon("mekanism:" + tier.getBaseTier().getName() + "TierInstaller");
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
		return "item." + BaseTier.values()[item.getItemDamage()].getName().toLowerCase() + "TierInstaller";
	}
}
