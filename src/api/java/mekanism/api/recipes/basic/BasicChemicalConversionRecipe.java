package mekanism.api.recipes.basic;

import java.util.List;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.display.SimpleMachineRecipeDisplay;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class BasicChemicalConversionRecipe extends BasicItemStackToChemicalRecipe {

    public BasicChemicalConversionRecipe(ItemStackIngredient input, ChemicalStackTemplate output) {
        super(input, output, MekanismRecipeTypes.TYPE_CHEMICAL_CONVERSION.value());
    }

    @Override
    public RecipeSerializer<BasicChemicalConversionRecipe> getSerializer() {
        return MekanismRecipeSerializers.CHEMICAL_CONVERSION.value();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new SimpleMachineRecipeDisplay(
              getInput().display(),
              getOutputDisplay(),
              //TODO: What do we want to display as the work stations here
              SlotDisplay.Empty.INSTANCE
        ));
    }
}