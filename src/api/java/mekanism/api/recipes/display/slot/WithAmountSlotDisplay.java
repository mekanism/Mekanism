package mekanism.api.recipes.display.slot;

import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackContentsFactory;
import mekanism.api.recipes.ingredients.chemical.display.ForChemicalStacks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory;
import net.neoforged.neoforge.fluids.crafting.display.ForFluidStacks;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Wrapping slot display that increases the size of the resolved stacks to the given amount.
///
/// @param source Base slot display.
/// @param amount Size that stacks should resolve to.
///
/// @since 10.8.0
public record WithAmountSlotDisplay(SlotDisplay source, int amount) implements SlotDisplay {

    private static final DeferredHolder<Type<?>, Type<WithAmountSlotDisplay>> TYPE = DeferredHolder.create(Registries.SLOT_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "with_amount"));

    @Override
    public Type<WithAmountSlotDisplay> type() {
        return TYPE.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return switch (factory) {
            case DisplayContentsFactory.ForStacks<T> items -> source.resolve(context, ItemStackContentsFactory.INSTANCE)
                  .map(stack -> items.forStack(stack.copyWithCount(amount)));
            case ForFluidStacks<T> fluids -> source.resolve(context, FluidStackContentsFactory.INSTANCE)
                  .map(stack -> fluids.forStack(stack.copyWithAmount(amount)));
            case ForChemicalStacks<T> chemicals -> source.resolve(context, ChemicalStackContentsFactory.INSTANCE)
                  .map(stack -> chemicals.forStack(stack.copyWithAmount(amount)));
            //TODO - 26.2: Should we be doing a best effort thing like this, or should we return that nothing could be resolved?
            default -> source.resolve(context, factory);
        };
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.source.isEnabled(enabledFeatures);
    }

    /// Scales the amount for this slot display by the given scale.
    ///
    /// @param scale Scale to multiply the amount by. Must be positive.
    public WithAmountSlotDisplay scale(int scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        } else if (scale == 1) {
            return this;
        }
        return new WithAmountSlotDisplay(source, amount * scale);
    }
}