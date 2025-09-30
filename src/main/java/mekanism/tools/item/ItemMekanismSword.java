package mekanism.tools.item;

import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import java.util.List;

public class ItemMekanismSword extends ItemSword
{
	public ItemMekanismSword(ToolMaterial enumtoolmaterial)
	{
		super(enumtoolmaterial);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		itemIcon = register.registerIcon("mekanism:" + getUnlocalizedName().replace("item.", ""));
	}
}
