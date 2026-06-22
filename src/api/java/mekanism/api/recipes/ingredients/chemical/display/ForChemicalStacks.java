package mekanism.api.recipes.ingredients.chemical.display;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.core.Holder;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.neoforged.neoforge.fluids.FluidType;

/// @see net.minecraft.world.item.crafting.display.DisplayContentsFactory.ForStacks
/// @see net.neoforged.neoforge.fluids.crafting.display.ForFluidStacks
/// @since 10.8.0
@FunctionalInterface
public interface ForChemicalStacks<T> extends DisplayContentsFactory<T> {

    /// {@return display data for the given chemical holder}
    ///
    /// @param chemical Chemical holder to display.
    default T forStack(Holder<Chemical> chemical) {
        return this.forStack(new ChemicalStack(chemical, FluidType.BUCKET_VOLUME));
    }

    /// {@return display data for the given chemical stack}
    ///
    /// @param chemical Chemical stack to display
    T forStack(ChemicalStack chemical);
}
