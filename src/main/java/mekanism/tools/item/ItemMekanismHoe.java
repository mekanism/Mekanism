package mekanism.tools.item;

import java.util.List;

import mekanism.api.util.StackUtils;
import mekanism.common.item.ItemMekanism;
import mekanism.common.util.MekanismUtils;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMekanismHoe extends ItemMekanism
{
	protected ToolMaterial toolMaterial;

	public ItemMekanismHoe(ToolMaterial enumtoolmaterial)
	{
		super();
		toolMaterial = enumtoolmaterial;
		maxStackSize = 1;
		setMaxDamage(enumtoolmaterial.getMaxUses());
		setCreativeTab(CreativeTabs.tabTools);
	}
	
	@Override
    public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
    {
        return StackUtils.equalsWildcard(ItemMekanismTool.getRepairStack(toolMaterial), stack2) ? true : super.getIsRepairable(stack1, stack2);
    }

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float entityX, float entityY, float entityZ)
	{
		if(!entityplayer.canPlayerEdit(x, y, z, side, itemstack))
		{
			return false;
		}
		else {
			UseHoeEvent event = new UseHoeEvent(entityplayer, itemstack, world, x, y, z);

			if(MinecraftForge.EVENT_BUS.post(event))
			{
				return false;
			}

			if(event.getResult() == Result.ALLOW)
			{
				itemstack.damageItem(1, entityplayer);
				return true;
			}

			Block blockID = world.getBlock(x, y, z);
			Block aboveBlock = world.getBlock(x, y + 1, z);

			if((side == 0 || !aboveBlock.isAir(world, x, y, z+1) || blockID != Blocks.grass) && blockID != Blocks.dirt)
			{
				return false;
			}
			else {
				Block block = Blocks.farmland;
				world.playSoundEffect(x + 0.5F, y + 0.5F, z + 0.5F, block.stepSound.getStepResourcePath(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);

				if(world.isRemote)
				{
					return true;
				}
				else {
					world.setBlock(x, y, z, block);
					itemstack.damageItem(1, entityplayer);
					return true;
				}
			}
		}
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add(MekanismUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D()
	{
		return true;
	}
}
