package mekanism.common.item.predicate;

import com.mojang.serialization.Codec;
import java.util.List;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.gear.ItemCanteen;
import mekanism.common.registries.MekanismFluids;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FullCanteenItemPredicate implements ItemSubPredicate {

    public static final FullCanteenItemPredicate INSTANCE = new FullCanteenItemPredicate();
    public static final Codec<FullCanteenItemPredicate> CODEC = Codec.unit(INSTANCE);
    public static final ItemSubPredicate.Type<FullCanteenItemPredicate> TYPE = new ItemSubPredicate.Type<>(CODEC);

    private FullCanteenItemPredicate() {
    }

    @Override
    public boolean matches(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof ItemCanteen) {
            List<IExtendedFluidTank> tanks = ContainerType.FLUID.getAttachmentContainersIfPresent(stack);
            return !tanks.isEmpty() && tanks.stream().allMatch(tank -> tank.getNeeded() == 0 && tank.getFluid().is(MekanismFluids.NUTRITIONAL_PASTE));
        }
        return false;
    }
}