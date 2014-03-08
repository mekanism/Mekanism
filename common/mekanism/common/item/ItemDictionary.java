package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDictionary extends ItemMekanism
{
	public ItemDictionary(int id)
	{
		super(id);
		setMaxStackSize(1);
	}

	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(!player.isSneaking())
		{
			Block block = Block.blocksList[world.getBlockId(x, y, z)];

			if(block != null)
			{
				if(world.isRemote)
				{
					ItemStack testStack = new ItemStack(block, 1, world.getBlockMetadata(x, y, z));
					List<String> names = MekanismUtils.getOreDictName(testStack);

					if(!names.isEmpty())
					{
						player.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Key(s) found:");

						for(String name : names)
						{
							player.addChatMessage(EnumColor.DARK_GREEN + " - " + name);
						}
					}
					else {
						player.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " No key.");
					}
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		if(entityplayer.isSneaking())
		{
			entityplayer.openGui(Mekanism.instance, 0, world, 0, 0, 0);
		}

		return itemstack;
	}
}
