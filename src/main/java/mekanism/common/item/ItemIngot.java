package mekanism.common.item;

import java.util.List;

import mekanism.common.Mekanism;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemIngot extends ItemMekanism {
	
	//The indices for the types of ingots defined here.
	public static final int R_OBSIDIAN = 0;
	public static final int R_GLOWSTONE = 1;

	public IIcon[] icons = new IIcon[256];

	public static String[] en_USNames = { "Obsidian", "Glowstone" };

	public ItemIngot() {
		super();
		setHasSubtypes(true);
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void registerIcons(IIconRegister register) {
		for (int i = 0; i < en_USNames.length; i++) {
			icons[i] = register.registerIcon("mekanism:" + en_USNames[i] + "Ingot");
		}

	}

	@Override
	public IIcon getIconFromDamage(int meta) {
		return icons[meta];
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tabs, List itemList) {
		for (int counter = 0; counter < en_USNames.length; ++counter) {
			itemList.add(new ItemStack(item, 1, counter));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {
		return "item." + en_USNames[item.getItemDamage()].toLowerCase() + "Ingot";
	}
}
