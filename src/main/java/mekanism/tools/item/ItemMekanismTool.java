package mekanism.tools.item;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;

public class ItemMekanismTool extends ItemTool
{
	public ItemMekanismTool(int mobBoost, ToolMaterial toolMaterial, Block[] effectiveBlocks)
	{
		super(mobBoost, toolMaterial, new HashSet<Block>(Arrays.asList(effectiveBlocks)));
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add("HP: " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		itemIcon = register.registerIcon("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}
}
