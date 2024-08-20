package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents;
import com.blamejared.crafttweaker.api.recipe.component.IRecipeComponent;
import com.blamejared.crafttweaker.api.recipe.component.RecipeComponentEqualityCheckers;
import com.google.gson.reflect.TypeToken;
import java.util.Collections;
import java.util.Objects;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.ingredient.CrTChemicalStackIngredient;

public class CrTRecipeComponents {

    private CrTRecipeComponents() {
    }

    public static final IRecipeComponent<Double> CHANCE = IRecipeComponent.simple(
          Mekanism.rl("chance"),
          new TypeToken<>() {},
          RecipeComponentEqualityCheckers::areNumbersEqual
    );
    public static final IRecipeComponent<Long> ENERGY = IRecipeComponent.simple(
          Mekanism.rl("energy"),
          new TypeToken<>() {},
          Long::equals
    );
    public static final IRecipeComponent<Boolean> PER_TICK_USAGE = IRecipeComponent.simple(
          Mekanism.rl("per_tick_usage"),
          new TypeToken<>() {},
          Boolean::equals
    );

    public static final PairedRecipeComponent<IIngredientWithAmount, IItemStack> ITEM = new PairedRecipeComponent<>(
          BuiltinRecipeComponents.Input.INGREDIENTS_WITH_AMOUNTS,
          BuiltinRecipeComponents.Output.ITEMS
    );
    public static final PairedRecipeComponent<CTFluidIngredient, IFluidStack> FLUID = new PairedRecipeComponent<>(
          BuiltinRecipeComponents.Input.FLUID_INGREDIENTS,
          BuiltinRecipeComponents.Output.FLUIDS
    );

    public static final PairedRecipeComponent<ChemicalStackIngredient, ICrTChemicalStack> CHEMICAL = new PairedRecipeComponent<>(
          IRecipeComponent.composite(
                Mekanism.rl("input/chemical"),
                new TypeToken<>() {},
                CrTRecipeComponents::ingredientsMatch,
                Collections::singletonList,
                ingredients -> ingredients.stream().reduce(CrTChemicalStackIngredient::or).orElseThrow()
          ),
          IRecipeComponent.simple(
                Mekanism.rl("output/chemical"),
                new TypeToken<>() {},
                ICrTChemicalStack::containsOther
          )
    );

    private static <TYPE, INGREDIENT extends InputIngredient<TYPE>> boolean ingredientsMatch(INGREDIENT a, INGREDIENT b) {
        return Objects.equals(a, b) || a.getRepresentations().stream().allMatch(b) && b.getRepresentations().stream().allMatch(a);
    }

    public record PairedRecipeComponent<INPUT, OUTPUT>(IRecipeComponent<INPUT> input, IRecipeComponent<OUTPUT> output) {
    }
}