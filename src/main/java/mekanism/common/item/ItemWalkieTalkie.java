package mekanism.common.item;

import java.util.List;

import mekanism.api.EnumColor;
import mekanism.common.util.LangUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemWalkieTalkie extends ItemMekanism {
	public ItemWalkieTalkie() {
		super();
		setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag) {
		super.addInformation(itemstack, entityplayer, list, flag);

		list.add(EnumColor.DARK_RED + LangUtils.localize("gui." + "off"));
	}
}

