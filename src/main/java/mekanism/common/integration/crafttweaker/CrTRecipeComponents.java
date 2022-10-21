package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents;
import com.blamejared.crafttweaker.api.recipe.component.DecomposedRecipeBuilder;
import com.blamejared.crafttweaker.api.recipe.component.IRecipeComponent;
import com.blamejared.crafttweaker.api.recipe.component.RecipeComponentEqualityCheckers;
import com.google.gson.reflect.TypeToken;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.SlurryStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator.SingleItemStackIngredient;

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

    public static final PairedRecipeComponent<ItemStackIngredient, IItemStack> ITEM = new PairedRecipeComponent<>(IRecipeComponent.composite(
          Mekanism.rl("input/item"),
          new TypeToken<>() {},
          (a, b) -> {
              if (Objects.equals(a, b)) {
                  return true;
              } else if (a instanceof SingleItemStackIngredient as && b instanceof SingleItemStackIngredient bs && as.getAmountRaw() == bs.getAmountRaw()) {
                  return RecipeComponentEqualityCheckers.areIngredientsEqual(IIngredient.fromIngredient(as.getInputRaw()), IIngredient.fromIngredient(bs.getInputRaw()));
              }
              return false;
          },
          CrTRecipeComponents::unwrapIngredient,
          ingredients -> IngredientCreatorAccess.item().from(ingredients.stream())
    ), BuiltinRecipeComponents.Output.ITEMS);
    public static final PairedRecipeComponent<FluidStackIngredient, IFluidStack> FLUID = new PairedRecipeComponent<>(IRecipeComponent.composite(
          Mekanism.rl("input/fluid"),
          new TypeToken<>() {},
          Objects::equals,
          CrTRecipeComponents::unwrapIngredient,
          ingredients -> IngredientCreatorAccess.fluid().from(ingredients.stream())
    ), IRecipeComponent.simple(
          Mekanism.rl("output/fluid"),
          new TypeToken<>() {},
          (a, b) -> a.getInternal().isFluidStackIdentical(b.getInternal())
    ));

    //Compiler can't actually infer these
    @SuppressWarnings("Convert2Diamond")
    public static final ChemicalRecipeComponent<Gas, GasStack, GasStackIngredient, ICrTGasStack> GAS = new ChemicalRecipeComponent<>(
          ChemicalType.GAS,
          new TypeToken<GasStackIngredient>() {},
          new TypeToken<ICrTGasStack>() {}
    );
    //Compiler can't actually infer these
    @SuppressWarnings("Convert2Diamond")
    public static final ChemicalRecipeComponent<InfuseType, InfusionStack, InfusionStackIngredient, ICrTInfusionStack> INFUSION = new ChemicalRecipeComponent<>(
          ChemicalType.INFUSION,
          new TypeToken<InfusionStackIngredient>() {},
          new TypeToken<ICrTInfusionStack>() {}
    );
    //Compiler can't actually infer these
    @SuppressWarnings("Convert2Diamond")
    public static final ChemicalRecipeComponent<Pigment, PigmentStack, PigmentStackIngredient, ICrTPigmentStack> PIGMENT = new ChemicalRecipeComponent<>(
          ChemicalType.PIGMENT,
          new TypeToken<PigmentStackIngredient>() {},
          new TypeToken<ICrTPigmentStack>() {}
    );
    //Compiler can't actually infer these
    @SuppressWarnings("Convert2Diamond")
    public static final ChemicalRecipeComponent<Slurry, SlurryStack, SlurryStackIngredient, ICrTSlurryStack> SLURRY = new ChemicalRecipeComponent<>(
          ChemicalType.SLURRY,
          new TypeToken<SlurryStackIngredient>() {},
          new TypeToken<ICrTSlurryStack>() {}
    );

    public static final List<ChemicalRecipeComponent<?, ?, ?, ?>> CHEMICAL_COMPONENTS = List.of(
          GAS,
          INFUSION,
          PIGMENT,
          SLURRY
    );

    private static <TYPE, INGREDIENT extends InputIngredient<TYPE>> Collection<INGREDIENT> unwrapIngredient(INGREDIENT ingredient) {
        if (ingredient instanceof IMultiIngredient) {
            return ((IMultiIngredient<TYPE, INGREDIENT>) ingredient).getIngredients();
        }
        return Collections.singletonList(ingredient);
    }

    public record PairedRecipeComponent<INPUT, OUTPUT>(IRecipeComponent<INPUT> input, IRecipeComponent<OUTPUT> output) {
    }

    public record ChemicalRecipeComponent<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
          INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>>
          (ChemicalType chemicalType, IRecipeComponent<INGREDIENT> input, IRecipeComponent<CRT_STACK> output) {

        @SuppressWarnings("unchecked")
        ChemicalRecipeComponent(ChemicalType chemicalType, TypeToken<INGREDIENT> inputType, TypeToken<CRT_STACK> outputType) {
            this(chemicalType, inputType, outputType, (IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT>) IngredientCreatorAccess.getCreatorForType(chemicalType));
        }

        private ChemicalRecipeComponent(ChemicalType chemicalType, TypeToken<INGREDIENT> inputType, TypeToken<CRT_STACK> outputType,
              IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> ingredientCreator) {
            this(chemicalType, IRecipeComponent.composite(
                  Mekanism.rl("input/" + chemicalType.getSerializedName()),
                  inputType,
                  Objects::equals,
                  CrTRecipeComponents::unwrapIngredient,
                  ingredients -> ingredientCreator.from(ingredients.stream())
            ), IRecipeComponent.simple(
                  Mekanism.rl("output/" + chemicalType.getSerializedName()),
                  outputType,
                  (a, b) -> a.isTypeEqual(b) && a.getAmount() == b.getAmount()
            ));
        }

        public DecomposedRecipeBuilder withOutput(DecomposedRecipeBuilder builder, List<STACK> output) {
            return builder.with(output(), CrTUtils.convertChemical(output));
        }
    }
}