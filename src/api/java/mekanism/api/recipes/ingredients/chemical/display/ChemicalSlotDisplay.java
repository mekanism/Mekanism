package mekanism.api.recipes.ingredients.chemical.display;

import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Slot display for a single chemical holder.
///
/// Note that information on amount and data of the displayed chemical stack depends on the provided factory!
///
/// @param chemical The chemical to be displayed.
///
/// @see net.minecraft.world.item.crafting.display.SlotDisplay.ItemSlotDisplay
/// @see net.neoforged.neoforge.fluids.crafting.display.FluidSlotDisplay
/// @since 10.8.0
public record ChemicalSlotDisplay(Holder<Chemical> chemical) implements SlotDisplay {

    private static final DeferredHolder<SlotDisplay.Type<?>, SlotDisplay.Type<ChemicalSlotDisplay>> TYPE = DeferredHolder.create(Registries.SLOT_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical"));

    @Override
    public SlotDisplay.Type<ChemicalSlotDisplay> type() {
        return TYPE.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return factory instanceof ForChemicalStacks<T> chemicals ? Stream.of(chemicals.forStack(chemical)) : Stream.empty();
    }
}
