package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeItem;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemUpgrade extends ItemMekanism implements IUpgradeItem
{
	private Upgrade upgrade;
	
	public ItemUpgrade(Upgrade type)
	{
		super();
		upgrade = type;
		setMaxStackSize(type.getMax());
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + "shift" + EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails"));
		}
		else {
			list.addAll(MekanismUtils.splitTooltip(getUpgradeType(itemstack).getDescription(), itemstack));
		}
	}
	
	@Override
	public Upgrade getUpgradeType(ItemStack stack) 
	{
		return upgrade;
	}
	
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(world.isRemote) 
		{
			return false;
		}
		
		if(player.isSneaking())
		{
			TileEntity tile = world.getTileEntity(x, y, z);
			Upgrade type = getUpgradeType(stack);
			
			if(tile instanceof IUpgradeTile)
			{
				TileComponentUpgrade component = ((IUpgradeTile)tile).getComponent();
				
				if(component.supports(type))
				{
					if(component.getUpgrades(type) < type.getMax())
					{
						component.addUpgrade(type);
						stack.stackSize--;
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
}
