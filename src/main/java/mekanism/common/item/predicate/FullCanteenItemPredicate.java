package mekanism.common.item.predicate;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismFluids;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FullCanteenItemPredicate implements DataComponentPredicate {

    public static final FullCanteenItemPredicate INSTANCE = new FullCanteenItemPredicate();
    public static DataComponentPredicate.Type<FullCanteenItemPredicate> TYPE = new ConcreteType<>(MapCodec.unitCodec(INSTANCE));

    private FullCanteenItemPredicate() {
    }

    @Override
    public boolean matches(DataComponentGetter stack) {
        List<LargeResourceStack<FluidResource>> tanks = ContainerType.FLUID.getAttachedContents(stack);
        return !tanks.isEmpty() && tanks.stream().allMatch(tank ->
              tank.amount() == MekanismConfig.gear.canteenMaxStorage.get() && tank.resource().is(MekanismFluids.NUTRITIONAL_PASTE));
    }
}