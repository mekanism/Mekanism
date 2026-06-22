package mekanism.common.recipe.lookup.cache.type;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import org.jspecify.annotations.Nullable;

public class ChemicalInputCache<RECIPE extends MekanismRecipe<?>> extends BaseInputCache<Chemical, ChemicalStack, ChemicalStackIngredient, RECIPE> {

    @Override
    public boolean mapInputs(RECIPE recipe, ChemicalStackIngredient inputIngredient) {
        for (Holder<Chemical> chemicalHolder : inputIngredient.ingredient().chemicals()) {
            if (!chemicalHolder.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                //Ignore empty stacks as some mods have ingredients that some stacks are empty
                addInputCache(chemicalHolder, recipe);
            }
        }
        return false;
    }

    @Override
    public boolean isEmpty(@Nullable TypedInstance<Chemical> input) {
        return switch (input) {
            case ChemicalStack stack -> stack.isEmpty();
            case ChemicalResource resource -> resource.isEmpty();
            case null -> true;
            default -> input.typeHolder().is(MekanismAPI.EMPTY_CHEMICAL_KEY);
        };
    }
}