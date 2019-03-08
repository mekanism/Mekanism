package mekanism.common.integration.wrenches;

import cofh.api.item.IToolHammer;
import mekanism.api.IMekWrench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

/**
 * Translates to COFH's IToolHammer
 */
public class CofhProxy implements MekWrenchProxy, IMekWrench
{
	public static final String COFH_HAMMER_CLASS = "cofh.api.item.IToolHammer";

	@Override
	public IMekWrench get(ItemStack stack)
	{
		return stack.getItem() instanceof IToolHammer ? this : null;
	}

	@Override
	public boolean canUseWrench(ItemStack stack, EntityPlayer player, BlockPos pos)
	{
		return stack.getItem() instanceof IToolHammer && ((IToolHammer)stack.getItem()).isUsable(stack, player, pos);
	}

	// uses default method
	//@Override
	//public boolean canUseWrench(EntityPlayer player, EnumHand hand, ItemStack stack, RayTraceResult rayTrace) {
	//    return false;
	//}

	@Override
	public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace)
	{
		if(wrench.getItem() instanceof IToolHammer)
		{
			((IToolHammer)wrench.getItem()).toolUsed(wrench, player, rayTrace.getBlockPos());
		}
	}
}
