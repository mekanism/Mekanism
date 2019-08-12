//TODO: COFH IToolHammer
/*import cofh.api.item.IToolHammer;
import mekanism.api.IMekWrench;
import mekanism.common.integration.wrenches.MekWrenchProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

/package mekanism.common.integration.wrenches;

//Translates to COFH's IToolHammer
public class CofhProxy implements MekWrenchProxy, IMekWrench {

    public static final String COFH_HAMMER_CLASS = "cofh.api.item.IToolHammer";

    @Override
    public IMekWrench get(ItemStack stack) {
        return stack.getItem() instanceof IToolHammer ? this : null;
    }

    @Override
    public boolean canUseWrench(ItemStack stack, PlayerEntity player, BlockPos pos) {
        return stack.getItem() instanceof IToolHammer && ((IToolHammer) stack.getItem()).isUsable(stack, player, pos);
    }

    @Override
    public void wrenchUsed(PlayerEntity player, Hand hand, ItemStack wrench, BlockRayTraceResult rayTrace) {
        if (wrench.getItem() instanceof IToolHammer) {
            ((IToolHammer) wrench.getItem()).toolUsed(wrench, player, rayTrace.getPos());
        }
    }
}*/