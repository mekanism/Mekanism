package mekanism.common.integration.wrenches;

import buildcraft.api.tools.IToolWrench;
import mekanism.api.IMekWrench;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

/**
 * Translates to Buildcraft's IToolWrench
 */
public class BuildCraftProxy implements MekWrenchProxy, IMekWrench {

    public static final String BUILDCRAFT_WRENCH_CLASS = "buildcraft.api.tools.IToolWrench";

    @Override
    public IMekWrench get(ItemStack stack) {
        return stack.getItem() instanceof IToolWrench ? this : null;
    }

    @Override
    public boolean canUseWrench(ItemStack stack, PlayerEntity player, BlockPos pos) {
        return false;//compat only, needs raytrace
    }

    @Override
    public boolean canUseWrench(PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult rayTrace) {
        return stack.getItem() instanceof IToolWrench && ((IToolWrench) stack.getItem()).canWrench(player, hand, stack, rayTrace);
    }

    @Override
    public void wrenchUsed(PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult rayTrace) {
        if (stack.getItem() instanceof IToolWrench) {
            ((IToolWrench) stack.getItem()).wrenchUsed(player, hand, stack, rayTrace);
        }
    }
}