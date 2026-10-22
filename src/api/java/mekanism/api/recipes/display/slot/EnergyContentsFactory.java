package mekanism.api.recipes.display.slot;

import net.minecraft.world.item.crafting.display.SlotDisplay;

/// Base energy contents factory: directly returns the energy.
///
/// Energy equivalent of [SlotDisplay.ItemStackContentsFactory] and [net.neoforged.neoforge.fluids.crafting.display.FluidStackContentsFactory].
///
/// @since 10.8.0
public final class EnergyContentsFactory implements ForEnergyStacks<Integer> {

    public static final EnergyContentsFactory INSTANCE = new EnergyContentsFactory();

    private EnergyContentsFactory() {
    }

    @Override
    public Integer forStack(int energy) {
        return energy;
    }
}
