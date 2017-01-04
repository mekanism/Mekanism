package mekanism.tools.item;

import java.util.List;

import mekanism.api.util.StackUtils;
import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.MekanismTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;

public class ItemMekanismAxe extends ItemAxe
{
	public ItemMekanismAxe(ToolMaterial tool)
	{
		super(tool, MekanismTools.AXE_DAMAGE.get(tool), MekanismTools.AXE_SPEED.get(tool));
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}

	@Override
	public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
	{
		return StackUtils.equalsWildcard(ItemMekanismTool.getRepairStack(toolMaterial), stack2) || super.getIsRepairable(stack1, stack2);
	}
}
