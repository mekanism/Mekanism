package mekanism.common.item.predicate;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.container.LargeResourceStack;
import mekanism.common.attachments.containers.AttachedResources;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;

public class FullCanteenItemPredicate implements DataComponentPredicate {

    public static ItemPredicate build(HolderLookup.Provider registry) {
        return ItemPredicate.Builder.item()
              .of(registry.lookupOrThrow(Registries.ITEM), MekanismItems.CANTEEN)
              .withComponents(
                    DataComponentMatchers.Builder.components()
                          .partial(TYPE, INSTANCE)
                          .build()
              )
              .build();
    }

    public static final FullCanteenItemPredicate INSTANCE = new FullCanteenItemPredicate();
    public static DataComponentPredicate.Type<FullCanteenItemPredicate> TYPE = new ConcreteType<>(MapCodec.unitCodec(INSTANCE));

    private FullCanteenItemPredicate() {
    }

    @Override
    public boolean matches(@NotNull DataComponentGetter stack) {
        AttachedResources<FluidResource> attachedFluids = ContainerType.FLUID.getOrEmpty(stack);
        List<LargeResourceStack<FluidResource>> tanks = attachedFluids.containers();
        return !tanks.isEmpty() && tanks.stream().allMatch(tank ->
              tank.amount() == MekanismConfig.gear.canteenMaxStorage.get() && tank.resource().is(MekanismFluids.NUTRITIONAL_PASTE));
    }
}