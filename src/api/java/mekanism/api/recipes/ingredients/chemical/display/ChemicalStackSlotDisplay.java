package mekanism.api.recipes.ingredients.chemical.display;

import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStackTemplate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Slot display for a given chemical stack, including chemical amount.
///
/// @param stack The chemical stack to be displayed.
///
/// @see net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackSlotDisplay
/// @see net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay
/// @since 10.8.0
public record ChemicalStackSlotDisplay(ChemicalStackTemplate stack) implements SlotDisplay {

    private static final DeferredHolder<Type<?>, Type<ChemicalStackSlotDisplay>> TYPE = DeferredHolder.create(Registries.SLOT_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_stack"));

    @Override
    public Type<ChemicalStackSlotDisplay> type() {
        return TYPE.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return switch (factory) {
            case ForChemicalStacks<T> chemicals -> Stream.of(chemicals.forStack(stack.create()));
            default -> Stream.empty();
        };
    }
}
