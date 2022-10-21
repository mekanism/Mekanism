package mekanism.common.item.predicate;

import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemCanteen;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.StorageUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FullCanteenItemPredicate extends CustomItemPredicate {

    public static final ResourceLocation ID = Mekanism.rl("full_canteen");
    public static final FullCanteenItemPredicate INSTANCE = new FullCanteenItemPredicate();

    private FullCanteenItemPredicate() {
    }

    @Override
    protected ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean matches(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof ItemCanteen) {
            FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
            return fluidStack.isFluidStackIdentical(MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(MekanismConfig.gear.canteenMaxStorage.get()));
        }
        return false;
    }
}