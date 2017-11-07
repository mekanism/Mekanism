package mekanism.common.integration.wrenches;

import buildcraft.api.tools.IToolWrench;
import mekanism.api.IMekWrench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

/**
 * Translates to Buildcraft's IToolWrench
 */
public class BuildCraftProxy implements MekWrenchProxy, IMekWrench
{
	public static final String BUILDCRAFT_WRENCH_CLASS = "buildcraft.api.tools.IToolWrench";

	@Override
	public IMekWrench get(ItemStack stack)
	{
		return stack.getItem() instanceof IToolWrench ? this : null;
	}

	@Override
	public boolean canUseWrench(ItemStack stack, EntityPlayer player, BlockPos pos)
	{
		return false;//compat only, needs raytrace
	}

	@Override
	public boolean canUseWrench(EntityPlayer player, EnumHand hand, ItemStack stack, RayTraceResult rayTrace)
	{
		return stack.getItem() instanceof IToolWrench && ((IToolWrench)stack.getItem()).canWrench(player, hand, stack, rayTrace);
	}

	@Override
	public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack stack, RayTraceResult rayTrace)
	{
		if(stack.getItem() instanceof IToolWrench)
		{
			((IToolWrench)stack.getItem()).wrenchUsed(player, hand, stack, rayTrace);
		}
	}
}
