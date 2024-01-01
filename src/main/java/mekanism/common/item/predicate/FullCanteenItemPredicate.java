package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemCanteen;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FullCanteenItemPredicate implements ICustomItemPredicate {

    public static final FullCanteenItemPredicate INSTANCE = new FullCanteenItemPredicate();

    private FullCanteenItemPredicate() {
    }

    @Override
    public Codec<? extends ICustomItemPredicate> codec() {
        return MekanismItemPredicates.FULL_CANTEEN.get();
    }

    @Override
    public boolean test(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof ItemCanteen) {
            FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
            return fluidStack.isFluidStackIdentical(MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(MekanismConfig.gear.canteenMaxStorage.get()));
        }
        return false;
    }
}