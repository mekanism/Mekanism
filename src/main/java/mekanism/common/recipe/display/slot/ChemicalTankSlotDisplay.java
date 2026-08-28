package mekanism.common.recipe.display.slot;

import java.util.stream.Stream;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.chemical.display.ForChemicalStacks;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismSlotDisplayTypes;
import net.minecraft.core.Holder;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record ChemicalTankSlotDisplay(SlotDisplay chemicalSource) implements SlotDisplay {

    private static final ForChemicalStacks<Holder<Chemical>> CHEMICAL_TYPES = ChemicalStack::typeHolder;

    @Override
    public Type<ChemicalTankSlotDisplay> type() {
        return MekanismSlotDisplayTypes.CHEMICAL_TANK.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (factory instanceof DisplayContentsFactory.ForStacks<T> items) {
            return chemicalSource.resolve(context, CHEMICAL_TYPES)
                  .map(type -> items.forStack(
                        ContainerType.CHEMICAL.getFilledVariant(MekanismBlocks.BASIC_CHEMICAL_TANK.getItemHolder(), type, null)
                  ));
        }
        return Stream.empty();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.chemicalSource.isEnabled(enabledFeatures);
    }
}