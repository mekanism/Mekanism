package mekanism.api.recipes.ingredients.chemical.display;

import java.util.function.Function;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Slot display that shows all chemicals in a given tag.
///
/// Note that information on amount and data of the displayed chemical stacks depends on the provided factory!
///
/// @param tag The tag to be displayed.
///
/// @see net.minecraft.world.item.crafting.display.SlotDisplay.TagSlotDisplay
/// @see net.neoforged.neoforge.fluids.crafting.display.FluidTagSlotDisplay
/// @since 10.8.0
public record ChemicalTagSlotDisplay(TagKey<Chemical> tag) implements SlotDisplay {

    private static final DeferredHolder<Type<?>, Type<ChemicalTagSlotDisplay>> TYPE = DeferredHolder.create(Registries.SLOT_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_tag"));

    @Override
    public Type<ChemicalTagSlotDisplay> type() {
        return TYPE.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (factory instanceof ForChemicalStacks<T> chemicals) {
            HolderLookup.Provider registries = context.getOptional(SlotDisplayContext.REGISTRIES);
            if (registries != null) {
                return registries.lookupOrThrow(MekanismRegistries.Keys.CHEMICAL)
                      .get(this.tag)
                      .map(tag -> tag.stream().map(chemicals::forStack))
                      .stream()
                      .flatMap(Function.identity());
            }
        }
        return Stream.empty();
    }
}
