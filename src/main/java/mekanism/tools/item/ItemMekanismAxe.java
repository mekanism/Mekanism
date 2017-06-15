package mekanism.tools.item;

import java.util.List;

import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import mekanism.common.util.StackUtils;
import mekanism.tools.common.MekanismTools;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemMekanismAxe extends ItemAxe
{
	public ItemMekanismAxe(ToolMaterial tool)
	{
		super(tool, MekanismTools.AXE_DAMAGE.get(tool), MekanismTools.AXE_SPEED.get(tool));
		setCreativeTab(Mekanism.tabMekanism);
	}

	@Override
	public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag)
	{
		list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair)
	{
		return StackUtils.equalsWildcard(ItemMekanismTool.getRepairStack(toolMaterial), repair) || super.getIsRepairable(toRepair, repair);
	}
}
