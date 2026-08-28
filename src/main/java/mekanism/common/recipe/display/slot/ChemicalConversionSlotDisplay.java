package mekanism.common.recipe.display.slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.chemical.display.ForChemicalStacks;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismSlotDisplayTypes;
import net.minecraft.core.Holder;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record ChemicalConversionSlotDisplay(SlotDisplay chemicalSource) implements SlotDisplay {

    private static final ForChemicalStacks<Holder<Chemical>> CHEMICAL_TYPES = ChemicalStack::typeHolder;

    @Override
    public Type<ChemicalConversionSlotDisplay> type() {
        return MekanismSlotDisplayTypes.CHEMICAL_CONVERSION.get();
    }

    @Override
    public <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        if (factory instanceof DisplayContentsFactory.ForStacks<T>) {
            List<SlotDisplay> displays = new ArrayList<>();
            //TODO - 26.2: Can we simplify this
            Set<Holder<Chemical>> supportedTypes = chemicalSource.resolve(context, CHEMICAL_TYPES).collect(Collectors.toSet());
            for (RecipeHolder<? extends ItemStackToChemicalRecipe> recipeHolder : MekanismRecipeType.CHEMICAL_CONVERSION.getRecipes()) {
                ItemStackToChemicalRecipe recipe = recipeHolder.value();
                for (ChemicalStackTemplate output : recipe.getOutputDefinition(context)) {
                    if (supportedTypes.contains(output.typeHolder())) {
                        displays.add(recipe.getInput().display());
                        break;
                    }
                }
            }
            return new SlotDisplay.Composite(displays).resolve(context, factory);
        }
        return Stream.empty();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return this.chemicalSource.isEnabled(enabledFeatures);
    }
}