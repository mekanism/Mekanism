package mekanism.api.recipes.display.slot;

import net.minecraft.world.item.crafting.display.DisplayContentsFactory;

/// @see ForStacks
/// @see net.neoforged.neoforge.fluids.crafting.display.ForFluidStacks
/// @since 10.8.0
@FunctionalInterface
public interface ForEnergyStacks<T> extends DisplayContentsFactory<T> {

    /// {@return display data for the given amount of energy}
    ///
    /// @param energy Amount of energy to display
    T forStack(int energy);
}
