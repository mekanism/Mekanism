package mekanism.common.recipe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.IMekanismRecipe;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismGases;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Class used to handle machine recipes. This is used for both adding and fetching recipes.
 *
 * @author AidanBrady, unpairedbracket
 */
public final class RecipeHandler {

    /**
     * Add an Enrichment Chamber recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    public static void addEnrichmentChamberRecipe(ItemStackIngredient input, ItemStack output) {
        Recipe.ENRICHMENT_CHAMBER.put(new ItemStackToItemStackRecipe(input, output));
    }

    /**
     * Add an Osmium Compressor recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    public static void addOsmiumCompressorRecipe(ItemStackIngredient input, GasStackIngredient gasInput, ItemStack output) {
        Recipe.OSMIUM_COMPRESSOR.put(new ItemStackGasToItemStackRecipe(input, gasInput, output));
    }

    /**
     * Add a Combiner recipe.
     *
     * @param input  - input ItemStack
     * @param extra  - extra ItemStack
     * @param output - output ItemStack
     */
    public static void addCombinerRecipe(ItemStackIngredient input, ItemStackIngredient extra, ItemStack output) {
        Recipe.COMBINER.put(new CombinerRecipe(input, extra, output));
    }

    /**
     * Add a Crusher recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    public static void addCrusherRecipe(ItemStackIngredient input, ItemStack output) {
        Recipe.CRUSHER.put(new ItemStackToItemStackRecipe(input, output));
    }

    /**
     * Add a Purification Chamber recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    public static void addPurificationChamberRecipe(ItemStackIngredient input, ItemStack output) {
        Recipe.PURIFICATION_CHAMBER.put(new ItemStackGasToItemStackRecipe(input, GasStackIngredient.from(MekanismGases.OXYGEN, 1), output));
    }

    /**
     * Add a Metallurgic Infuser recipe.
     *
     * @param infusionIngredient - which Infuse to use
     * @param input              - input ItemStack
     * @param output             - output ItemStack
     */
    public static void addMetallurgicInfuserRecipe(InfusionIngredient infusionIngredient, ItemStackIngredient input, ItemStack output) {
        Recipe.METALLURGIC_INFUSER.put(new MetallurgicInfuserRecipe(input, infusionIngredient, output));
    }

    /**
     * Add a Chemical Infuser recipe.
     *
     * @param leftInput  - left GasStack to input
     * @param rightInput - right GasStack to input
     * @param output     - output GasStack
     */
    public static void addChemicalInfuserRecipe(GasStackIngredient leftInput, GasStackIngredient rightInput, GasStack output) {
        Recipe.CHEMICAL_INFUSER.put(new ChemicalInfuserRecipe(leftInput, rightInput, output));
    }

    /**
     * Add a Chemical Oxidizer recipe.
     *
     * @param input        - input ItemStack
     * @param outputGas    - output Gas
     * @param outputAmount - amount of gas output
     */
    public static void addChemicalOxidizerRecipe(ItemStackIngredient input, Gas outputGas, int outputAmount) {
        Recipe.CHEMICAL_OXIDIZER.put(new ItemStackToGasRecipe(input, outputGas, outputAmount));
    }

    public static void addChemicalOxidizerRecipe(ItemStackIngredient input, GasStack output) {
        Recipe.CHEMICAL_OXIDIZER.put(new ItemStackToGasRecipe(input, output));
    }

    /**
     * Add a Chemical Injection Chamber recipe.
     *
     * @param input  - input ItemStack
     * @param output - output ItemStack
     */
    public static void addChemicalInjectionChamberRecipe(ItemStackIngredient input, GasStackIngredient gas, ItemStack output) {
        Recipe.CHEMICAL_INJECTION_CHAMBER.put(new ItemStackGasToItemStackRecipe(input, gas, output));
    }

    /**
     * Add an Electrolytic Separator recipe.
     *
     * @param fluid       - FluidStack to electrolyze
     * @param leftOutput  - left gas to produce when the fluid is electrolyzed
     * @param rightOutput - right gas to produce when the fluid is electrolyzed
     */
    public static void addElectrolyticSeparatorRecipe(FluidStackIngredient fluid, double energy, GasStack leftOutput, GasStack rightOutput) {
        Recipe.ELECTROLYTIC_SEPARATOR.put(new ElectrolysisRecipe(fluid, energy, leftOutput, rightOutput));
    }

    /**
     * Add a Precision Sawmill recipe.
     *
     * @param input           - input ItemStack
     * @param primaryOutput   - guaranteed output
     * @param secondaryOutput - possible extra output
     * @param chance          - probability of obtaining extra output
     */
    public static void addPrecisionSawmillRecipe(ItemStackIngredient input, ItemStack primaryOutput, ItemStack secondaryOutput, double chance) {
        Recipe.PRECISION_SAWMILL.put(new SawmillRecipe(input, primaryOutput, secondaryOutput, chance));
    }

    /**
     * Add a Precision Sawmill recipe with no chance output
     *
     * @param input         - input ItemStack
     * @param primaryOutput - guaranteed output
     */
    public static void addPrecisionSawmillRecipe(ItemStackIngredient input, ItemStack primaryOutput) {
        addPrecisionSawmillRecipe(input, primaryOutput, ItemStack.EMPTY, 0);
    }

    /**
     * Add a Chemical Dissolution Chamber recipe.
     *
     * @param input     - input ItemStack
     * @param outputGas - output GasStack
     */
    public static void addChemicalDissolutionChamberRecipe(ItemStackIngredient input, GasStackIngredient inputGas, Gas outputGas, int outputAmount) {
        Recipe.CHEMICAL_DISSOLUTION_CHAMBER.put(new ItemStackGasToGasRecipe(input, inputGas, outputGas, outputAmount));
    }

    public static void addChemicalDissolutionChamberRecipe(ItemStackIngredient input, GasStackIngredient inputGas, GasStack output) {
        Recipe.CHEMICAL_DISSOLUTION_CHAMBER.put(new ItemStackGasToGasRecipe(input, inputGas, output));
    }

    /**
     * Add a Chemical Washer recipe.
     *
     * @param input  - input GasStack
     * @param output - output GasStack
     */
    public static void addChemicalWasherRecipe(FluidStackIngredient fluidInput, GasStackIngredient input, GasStack output) {
        Recipe.CHEMICAL_WASHER.put(new FluidGasToGasRecipe(fluidInput, input, output));
    }

    /**
     * Add a Chemical Crystallizer recipe.
     *
     * @param input  - input GasStack
     * @param output - output ItemStack
     */
    public static void addChemicalCrystallizerRecipe(GasStackIngredient input, ItemStack output) {
        Recipe.CHEMICAL_CRYSTALLIZER.put(new ChemicalCrystallizerRecipe(input, output));
    }

    /**
     * Add a Pressurized Reaction Chamber recipe.
     *
     * @param inputSolid      - input ItemStack
     * @param inputFluid      - input FluidStack
     * @param inputGas        - input GasStack
     * @param outputSolid     - output ItemStack
     * @param outputGas       - output Gas
     * @param gasOutputAmount - amount of gas output
     * @param extraEnergy     - extra energy needed by the recipe
     * @param ticks           - amount of ticks it takes for this recipe to complete
     */
    public static void addPRCRecipe(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, ItemStack outputSolid, Gas outputGas,
          int gasOutputAmount, double extraEnergy, int ticks) {
        Recipe.PRESSURIZED_REACTION_CHAMBER.put(new PressurizedReactionRecipe(inputSolid, inputFluid, inputGas, outputGas, gasOutputAmount, extraEnergy, ticks, outputSolid));
    }

    public static void addPRCRecipe(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, ItemStack outputSolid, GasStack outputGas,
          double extraEnergy, int ticks) {
        Recipe.PRESSURIZED_REACTION_CHAMBER.put(new PressurizedReactionRecipe(inputSolid, inputFluid, inputGas, outputGas, extraEnergy, ticks, outputSolid));
    }

    public static void addThermalEvaporationRecipe(FluidStackIngredient inputFluid, FluidStack outputFluid) {
        Recipe.THERMAL_EVAPORATION_PLANT.put(new FluidToFluidRecipe(inputFluid, outputFluid));
    }

    public static void addSolarNeutronRecipe(GasStackIngredient inputGas, GasStack outputGas) {
        Recipe.SOLAR_NEUTRON_ACTIVATOR.put(new GasToGasRecipe(inputGas, outputGas));
    }

    public static class Recipe<RECIPE_TYPE extends IMekanismRecipe> {

        private static List<Recipe> values = new ArrayList<>();

        public static final Recipe<ItemStackToItemStackRecipe> ENERGIZED_SMELTER = new Recipe<>(MekanismBlock.ENERGIZED_SMELTER, ItemStackToItemStackRecipe.class);
        public static final Recipe<ItemStackToItemStackRecipe> ENRICHMENT_CHAMBER = new Recipe<>(MekanismBlock.ENRICHMENT_CHAMBER, ItemStackToItemStackRecipe.class);
        public static final Recipe<ItemStackGasToItemStackRecipe> OSMIUM_COMPRESSOR = new Recipe<>(MekanismBlock.OSMIUM_COMPRESSOR, ItemStackGasToItemStackRecipe.class);
        public static final Recipe<CombinerRecipe> COMBINER = new Recipe<>(MekanismBlock.COMBINER, CombinerRecipe.class);
        public static final Recipe<ItemStackToItemStackRecipe> CRUSHER = new Recipe<>(MekanismBlock.CRUSHER, ItemStackToItemStackRecipe.class);
        public static final Recipe<ItemStackGasToItemStackRecipe> PURIFICATION_CHAMBER = new Recipe<>(MekanismBlock.PURIFICATION_CHAMBER, ItemStackGasToItemStackRecipe.class);
        public static final Recipe<MetallurgicInfuserRecipe> METALLURGIC_INFUSER = new Recipe<>(MekanismBlock.METALLURGIC_INFUSER, MetallurgicInfuserRecipe.class);
        public static final Recipe<ChemicalInfuserRecipe> CHEMICAL_INFUSER = new Recipe<>(MekanismBlock.CHEMICAL_INFUSER, ChemicalInfuserRecipe.class);
        public static final Recipe<ItemStackToGasRecipe> CHEMICAL_OXIDIZER = new Recipe<>(MekanismBlock.CHEMICAL_OXIDIZER, ItemStackToGasRecipe.class);
        public static final Recipe<ItemStackGasToItemStackRecipe> CHEMICAL_INJECTION_CHAMBER = new Recipe<>(MekanismBlock.CHEMICAL_INJECTION_CHAMBER, ItemStackGasToItemStackRecipe.class);
        public static final Recipe<ElectrolysisRecipe> ELECTROLYTIC_SEPARATOR = new Recipe<>(MekanismBlock.ELECTROLYTIC_SEPARATOR, ElectrolysisRecipe.class);
        public static final Recipe<SawmillRecipe> PRECISION_SAWMILL = new Recipe<>(MekanismBlock.PRECISION_SAWMILL, SawmillRecipe.class);
        public static final Recipe<ItemStackGasToGasRecipe> CHEMICAL_DISSOLUTION_CHAMBER = new Recipe<>(MekanismBlock.CHEMICAL_DISSOLUTION_CHAMBER, ItemStackGasToGasRecipe.class);
        public static final Recipe<FluidGasToGasRecipe> CHEMICAL_WASHER = new Recipe<>(MekanismBlock.CHEMICAL_WASHER, FluidGasToGasRecipe.class);
        public static final Recipe<ChemicalCrystallizerRecipe> CHEMICAL_CRYSTALLIZER = new Recipe<>(MekanismBlock.CHEMICAL_CRYSTALLIZER, ChemicalCrystallizerRecipe.class);
        public static final Recipe<PressurizedReactionRecipe> PRESSURIZED_REACTION_CHAMBER = new Recipe<>(MekanismBlock.PRESSURIZED_REACTION_CHAMBER, PressurizedReactionRecipe.class);
        public static final Recipe<FluidToFluidRecipe> THERMAL_EVAPORATION_PLANT = new Recipe<>(MekanismBlock.THERMAL_EVAPORATION_CONTROLLER, FluidToFluidRecipe.class);
        public static final Recipe<GasToGasRecipe> SOLAR_NEUTRON_ACTIVATOR = new Recipe<>(MekanismBlock.SOLAR_NEUTRON_ACTIVATOR, GasToGasRecipe.class);

        static {
            values = ImmutableList.copyOf(values);
        }

        public static Iterable<Recipe> values() {
            return values;
        }

        private final List<RECIPE_TYPE> recipes = new ArrayList<>();
        private final String recipeName;
        private Class<RECIPE_TYPE> recipeClass;

        private Recipe(IBlockProvider type, Class<RECIPE_TYPE> recipe) {
            recipeName = type.getName();
            recipeClass = recipe;
            values.add(this);
        }

        public boolean put(@Nonnull RECIPE_TYPE recipe) {
            Preconditions.checkArgument(recipeClass.isInstance(recipe));
            return recipes.add(recipe);
        }

        public boolean remove(@Nonnull Predicate<RECIPE_TYPE> removeIfTrue) {
            return recipes.removeIf(removeIfTrue);
        }

        public String getRecipeName() {
            return recipeName;
        }

        public Class<RECIPE_TYPE> getRecipeClass() {
            return recipeClass;
        }

        @Nonnull
        public List<RECIPE_TYPE> get() {
            return recipes;
        }

        public Stream<RECIPE_TYPE> stream() {
            return recipes.stream();
        }

        @Nullable
        public RECIPE_TYPE findFirst(Predicate<RECIPE_TYPE> matchCriteria) {
            return recipes.stream().filter(matchCriteria).findFirst().orElse(null);
        }

        public boolean contains(Predicate<RECIPE_TYPE> matchCriteria) {
            return recipes.stream().anyMatch(matchCriteria);
        }
    }
}