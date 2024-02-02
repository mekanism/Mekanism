package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.item.gear.ItemCanteen;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.registries.MekanismFluids;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
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
            if (stack.hasData(MekanismAttachmentTypes.FLUID_TANKS)) {
                IExtendedFluidTank tank = stack.getData(MekanismAttachmentTypes.FLUID_TANKS).getFluidTank(0, null);
                return tank != null && tank.getNeeded() == 0 && tank.getFluid().is(MekanismFluids.NUTRITIONAL_PASTE.getFluid());
            }
        }
        return false;
    }
}