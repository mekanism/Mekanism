package mekanism.api.recipes.ingredients.chemical.display;

import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/// Base chemical stack contents factory: directly returns the stacks.
///
/// Chemical equivalent of [SlotDisplay.ItemStackContentsFactory] and [net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory].
///
/// @since 10.8.0
public final class ChemicalStackContentsFactory implements ForChemicalStacks<ChemicalStack> {

    public static final ChemicalStackContentsFactory INSTANCE = new ChemicalStackContentsFactory();

    private ChemicalStackContentsFactory() {
    }

    @Override
    public ChemicalStack forStack(ChemicalStack chemical) {
        return chemical;
    }
}
