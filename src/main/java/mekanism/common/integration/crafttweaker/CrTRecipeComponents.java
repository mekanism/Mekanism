package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents;
import com.blamejared.crafttweaker.api.recipe.component.DecomposedRecipeBuilder;
import com.blamejared.crafttweaker.api.recipe.component.IRecipeComponent;
import com.blamejared.crafttweaker.api.recipe.component.RecipeComponentEqualityCheckers;
import com.google.gson.reflect.TypeToken;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.GasStackIngredient;
import mekanism.api.recipes.ingredients.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.SlurryStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTInfusionStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTPigmentStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTSlurryStackIngredient;

public class CrTRecipeComponents {

    private CrTRecipeComponents() {
    }

    public static final IRecipeComponent<Double> CHANCE = IRecipeComponent.simple(
          Mekanism.rl("chance"),
          new TypeToken<>() {},
          RecipeComponentEqualityCheckers::areNumbersEqual
    );
    public static final IRecipeComponent<FloatingLong> ENERGY = IRecipeComponent.simple(
          Mekanism.rl("energy"),
          new TypeToken<>() {},
          FloatingLong::equals
    );

    public static final PairedRecipeComponent<IIngredientWithAmount, IItemStack> ITEM = new PairedRecipeComponent<>(
          BuiltinRecipeComponents.Input.INGREDIENTS_WITH_AMOUNTS,
          BuiltinRecipeComponents.Output.ITEMS
    );
    public static final PairedRecipeComponent<CTFluidIngredient, IFluidStack> FLUID = new PairedRecipeComponent<>(
          BuiltinRecipeComponents.Input.FLUID_INGREDIENTS,
          BuiltinRecipeComponents.Output.FLUIDS
    );

    //Compiler can't actually infer these
    @SuppressWarnings("Convert2Diamond")
    public static final ChemicalRecipeComponent<Gas, GasStack, GasStackIngredient, ICrTGasStack> GAS = new ChemicalRecipeComponent<>(
          ChemicalType.GAS,
          new TypeToken<GasStackIngredient>() {},
          new TypeToken<ICrTGasStack>() {},
          CrTGasStackIngredient::or
    );
    //Compiler can't actually infer these
    @SuppressWarnings("Convert2Diamond")
    public static final ChemicalRecipeComponent<InfuseType, InfusionStack, InfusionStackIngredient, ICrTInfusionStack> INFUSION = new ChemicalRecipeComponent<>(
          ChemicalType.INFUSION,
          new TypeToken<InfusionStackIngredient>() {},
          new TypeToken<ICrTInfusionStack>() {},
          CrTInfusionStackIngredient::or
    );
    //Compiler can't actually infer these
    @SuppressWarnings("Convert2Diamond")
    public static final ChemicalRecipeComponent<Pigment, PigmentStack, PigmentStackIngredient, ICrTPigmentStack> PIGMENT = new ChemicalRecipeComponent<>(
          ChemicalType.PIGMENT,
          new TypeToken<PigmentStackIngredient>() {},
          new TypeToken<ICrTPigmentStack>() {},
          CrTPigmentStackIngredient::or
    );
    //Compiler can't actually infer these
    @SuppressWarnings("Convert2Diamond")
    public static final ChemicalRecipeComponent<Slurry, SlurryStack, SlurryStackIngredient, ICrTSlurryStack> SLURRY = new ChemicalRecipeComponent<>(
          ChemicalType.SLURRY,
          new TypeToken<SlurryStackIngredient>() {},
          new TypeToken<ICrTSlurryStack>() {},
          CrTSlurryStackIngredient::or
    );

    public static final List<ChemicalRecipeComponent<?, ?, ?, ?>> CHEMICAL_COMPONENTS = List.of(
          GAS,
          INFUSION,
          PIGMENT,
          SLURRY
    );

    private static <TYPE, INGREDIENT extends InputIngredient<TYPE>> boolean ingredientsMatch(INGREDIENT a, INGREDIENT b) {
        return Objects.equals(a, b) || a.getRepresentations().stream().allMatch(b) && b.getRepresentations().stream().allMatch(a);
    }

    public record PairedRecipeComponent<INPUT, OUTPUT>(IRecipeComponent<INPUT> input, IRecipeComponent<OUTPUT> output) {
    }

    public record ChemicalRecipeComponent<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK, ?>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>>
          (ChemicalType chemicalType, IRecipeComponent<INGREDIENT> input, IRecipeComponent<CRT_STACK> output) {

        private ChemicalRecipeComponent(ChemicalType chemicalType, TypeToken<INGREDIENT> inputType, TypeToken<CRT_STACK> outputType,
              BinaryOperator<INGREDIENT> ingredientCombiner) {
            this(chemicalType, IRecipeComponent.composite(
                  Mekanism.rl("input/" + chemicalType.getSerializedName()),
                  inputType,
                  CrTRecipeComponents::ingredientsMatch,
                  Collections::singletonList,
                  ingredients -> ingredients.stream().reduce(ingredientCombiner).orElseThrow()
            ), IRecipeComponent.simple(
                  Mekanism.rl("output/" + chemicalType.getSerializedName()),
                  outputType,
                  ICrTChemicalStack::containsOther
            ));
        }

        public DecomposedRecipeBuilder withOutput(DecomposedRecipeBuilder builder, List<STACK> output) {
            return builder.with(output(), CrTUtils.convertChemical(output));
        }
    }
}