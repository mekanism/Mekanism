package mekanism.api.recipes.display.slot;

import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;

/// Slot display for an amount of energy.
///
/// @param energy The energy to be displayed.
///
/// @since 10.8.0
public record EnergySlotDisplay(int energy) implements SlotDisplay {

    private static final DeferredHolder<Type<?>, Type<EnergySlotDisplay>> TYPE = DeferredHolder.create(Registries.SLOT_DISPLAY,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "energy"));

    @Override
    public Type<EnergySlotDisplay> type() {
        return TYPE.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return factory instanceof ForEnergyStacks<T> energyStacks ? Stream.of(energyStacks.forStack(energy)) : Stream.empty();
    }
}