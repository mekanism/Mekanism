package mekanism.common.recipe.display.slot;

import java.util.stream.Stream;
import mekanism.api.datamaps.chemical.ChemicalSolidTag;
import mekanism.api.recipes.ingredients.chemical.display.ChemicalStackContentsFactory;
import mekanism.common.registries.MekanismSlotDisplayTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

public record ChemicalSolidTagSlotDisplay(SlotDisplay chemicalSource) implements SlotDisplay {

    @Override
    public Type<ChemicalSolidTagSlotDisplay> type() {
        return MekanismSlotDisplayTypes.CHEMICAL_SOLID_TAG.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (factory instanceof DisplayContentsFactory.ForStacks<T> items) {
            HolderLookup.Provider registries = context.getOptional(SlotDisplayContext.REGISTRIES);
            if (registries != null) {
                return chemicalSource.resolve(context, ChemicalStackContentsFactory.INSTANCE)
                      .flatMap(chemical -> {
                          //Note: We pass null, as we assume the chemical has a proper holder instead of being a direct holder
                          // and falling back for a direct holder requires access to a full registry access
                          ChemicalSolidTag solidTag = chemical.getSolidTag(null);
                          if (solidTag == null) {
                              return Stream.empty();
                          }
                          return solidTag.lookupTag(registries).stream();
                      })
                      .flatMap(HolderSet::stream)
                      .map(items::forStack);
            }
        }
        return Stream.empty();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.chemicalSource.isEnabled(enabledFeatures);
    }
}