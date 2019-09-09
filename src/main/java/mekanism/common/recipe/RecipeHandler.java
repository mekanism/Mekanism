package mekanism.common.recipe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfusionContainer;
import mekanism.api.recipes.AmbientAccumulatorRecipe;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
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
import mekanism.api.recipes.inputs.GasIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismFluids;
import mekanism.common.block.states.BlockStateMachine.MachineType;
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
    public static void addOsmiumCompressorRecipe(ItemStackIngredient input, GasIngredient gasInput, ItemStack output) {
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
        Recipe.PURIFICATION_CHAMBER.put(new ItemStackGasToItemStackRecipe(input, GasIngredient.fromInstance(MekanismFluids.Oxygen), output));
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
    public static void addChemicalInjectionChamberRecipe(ItemStackIngredient input, GasIngredient gas, ItemStack output) {
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
    public static void addChemicalDissolutionChamberRecipe(ItemStackIngredient input, GasIngredient inputGas, Gas outputGas, int outputAmount) {
        Recipe.CHEMICAL_DISSOLUTION_CHAMBER.put(new ItemStackGasToGasRecipe(input, inputGas, outputGas, outputAmount));
    }

    public static void addChemicalDissolutionChamberRecipe(ItemStackIngredient input, GasIngredient inputGas, GasStack output) {
        Recipe.CHEMICAL_DISSOLUTION_CHAMBER.put(new ItemStackGasToGasRecipe(input, inputGas, output));
    }

    /**
     * Add a Chemical Washer recipe.
     *
     * @param input  - input GasStack
     * @param output - output GasStack
     */
    public static void addChemicalWasherRecipe(GasStackIngredient input, GasStack output) {
        Recipe.CHEMICAL_WASHER.put(new GasToGasRecipe(input, output));
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

    public static void addAmbientGas(int dimensionID, GasStack ambient) {
        Recipe.AMBIENT_ACCUMULATOR.put(new AmbientAccumulatorRecipe(dimensionID, 20, ambient));
    }

    /**
     * Gets the Metallurgic Infuser Recipe for the InfusionInput in the parameters.
     *
     * @param item   - input item
     * @param infuse - input Infusion
     *
     * @return MetallurgicInfuserRecipe
     */
    @Nullable
    public static MetallurgicInfuserRecipe getMetallurgicInfuserRecipe(@Nonnull ItemStack item, InfusionContainer infuse) {
        return Recipe.METALLURGIC_INFUSER.findFirst(recipe -> recipe.test(infuse, item));
    }

    /**
     * Gets the Chemical Infuser Recipe of the ChemicalPairInput in the parameters.
     *
     * @param input1 - Left input
     * @param input2 - right input
     *
     * @return ChemicalInfuserRecipe
     */
    @Nullable
    public static ChemicalInfuserRecipe getChemicalInfuserRecipe(@Nonnull GasStack input1, @Nonnull GasStack input2) {
        return Recipe.CHEMICAL_INFUSER.findFirst(recipe -> recipe.test(input1, input2));
    }

    /**
     * Gets the Chemical Crystallizer Recipe for the defined Gas input.
     *
     * @param input - GasInput
     *
     * @return CrystallizerRecipe
     */
    @Nullable
    public static ChemicalCrystallizerRecipe getChemicalCrystallizerRecipe(@Nonnull GasStack input) {
        return Recipe.CHEMICAL_CRYSTALLIZER.findFirst(recipe -> recipe.test(input));
    }

    /**
     * Gets the Chemical Washer Recipe for the defined Gas input.
     *
     * @param input - GasInput
     *
     * @return WasherRecipe
     */
    @Nullable
    public static GasToGasRecipe getChemicalWasherRecipe(@Nonnull GasStack input) {
        return Recipe.CHEMICAL_WASHER.findFirst(recipe -> recipe.test(input));
    }

    /**
     * Gets the Chemical Oxidizer Recipe for the ItemStack in the parameters.
     *
     * @param input - ItemStack
     *
     * @return OxidationRecipe
     */
    @Nullable
    public static ItemStackToGasRecipe getOxidizerRecipe(@Nonnull ItemStack input) {
        return Recipe.CHEMICAL_OXIDIZER.findFirst(recipe -> recipe.test(input));
    }

    @Nullable
    public static SawmillRecipe getSawmillRecipe(@Nonnull ItemStack input) {
        return Recipe.PRECISION_SAWMILL.findFirst(recipe -> recipe.test(input));
    }

    /**
     * Get the Electrolytic Separator Recipe corresponding to electrolysing a given fluid.
     *
     * @param input - the FluidStack to electrolyse fluid from
     *
     * @return SeparatorRecipe
     */
    @Nullable
    public static ElectrolysisRecipe getElectrolyticSeparatorRecipe(@Nonnull FluidStack input) {
        return Recipe.ELECTROLYTIC_SEPARATOR.findFirst(recipe -> recipe.test(input));
    }

    @Nullable
    public static FluidToFluidRecipe getThermalEvaporationRecipe(@Nonnull FluidStack input) {
        return Recipe.THERMAL_EVAPORATION_PLANT.findFirst(recipe -> recipe.test(input));
    }

    @Nullable
    public static GasToGasRecipe getSolarNeutronRecipe(@Nonnull GasStack input) {
        return Recipe.SOLAR_NEUTRON_ACTIVATOR.findFirst(recipe -> recipe.test(input));
    }

    @Nullable
    public static PressurizedReactionRecipe getPRCRecipe(ItemStack solid, FluidStack fluid, GasStack gas) {
        return Recipe.PRESSURIZED_REACTION_CHAMBER.findFirst(recipe -> recipe.test(solid, fluid, gas));
    }

    @Nullable
    public static AmbientAccumulatorRecipe getDimensionGas(int dimension) {
        return Recipe.AMBIENT_ACCUMULATOR.findFirst(recipe -> recipe.test(dimension));
    }

    public static boolean isInPressurizedRecipe(@Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            return Recipe.PRESSURIZED_REACTION_CHAMBER.contains(recipe -> recipe.getInputSolid().testType(stack));
        }
        return false;
    }

    public static class Recipe<RECIPE_TYPE extends IMekanismRecipe> {

        private static List<Recipe> values = new ArrayList<>();

        public static final Recipe<ItemStackToItemStackRecipe> ENERGIZED_SMELTER = new Recipe<>(MachineType.ENERGIZED_SMELTER, ItemStackToItemStackRecipe.class);

        public static final Recipe<ItemStackToItemStackRecipe> ENRICHMENT_CHAMBER = new Recipe<>(MachineType.ENRICHMENT_CHAMBER, ItemStackToItemStackRecipe.class);

        public static final Recipe<ItemStackGasToItemStackRecipe> OSMIUM_COMPRESSOR = new Recipe<>(MachineType.OSMIUM_COMPRESSOR, ItemStackGasToItemStackRecipe.class);

        public static final Recipe<CombinerRecipe> COMBINER = new Recipe<>(MachineType.COMBINER, CombinerRecipe.class);

        public static final Recipe<ItemStackToItemStackRecipe> CRUSHER = new Recipe<>(MachineType.CRUSHER, ItemStackToItemStackRecipe.class);

        public static final Recipe<ItemStackGasToItemStackRecipe> PURIFICATION_CHAMBER = new Recipe<>(MachineType.PURIFICATION_CHAMBER, ItemStackGasToItemStackRecipe.class);

        public static final Recipe<MetallurgicInfuserRecipe> METALLURGIC_INFUSER = new Recipe<>(MachineType.METALLURGIC_INFUSER, MetallurgicInfuserRecipe.class);

        public static final Recipe<ChemicalInfuserRecipe> CHEMICAL_INFUSER = new Recipe<>(MachineType.CHEMICAL_INFUSER, ChemicalInfuserRecipe.class);

        public static final Recipe<ItemStackToGasRecipe> CHEMICAL_OXIDIZER = new Recipe<>(MachineType.CHEMICAL_OXIDIZER, ItemStackToGasRecipe.class);

        public static final Recipe<ItemStackGasToItemStackRecipe> CHEMICAL_INJECTION_CHAMBER = new Recipe<>(MachineType.CHEMICAL_INJECTION_CHAMBER, ItemStackGasToItemStackRecipe.class);

        public static final Recipe<ElectrolysisRecipe> ELECTROLYTIC_SEPARATOR = new Recipe<>(MachineType.ELECTROLYTIC_SEPARATOR, ElectrolysisRecipe.class);

        public static final Recipe<SawmillRecipe> PRECISION_SAWMILL = new Recipe<>(MachineType.PRECISION_SAWMILL, SawmillRecipe.class);

        public static final Recipe<ItemStackGasToGasRecipe> CHEMICAL_DISSOLUTION_CHAMBER = new Recipe<>(MachineType.CHEMICAL_DISSOLUTION_CHAMBER, ItemStackGasToGasRecipe.class);

        public static final Recipe<GasToGasRecipe> CHEMICAL_WASHER = new Recipe<>(MachineType.CHEMICAL_WASHER, GasToGasRecipe.class);

        public static final Recipe<ChemicalCrystallizerRecipe> CHEMICAL_CRYSTALLIZER = new Recipe<>(MachineType.CHEMICAL_CRYSTALLIZER, ChemicalCrystallizerRecipe.class);

        public static final Recipe<PressurizedReactionRecipe> PRESSURIZED_REACTION_CHAMBER = new Recipe<>(MachineType.PRESSURIZED_REACTION_CHAMBER, PressurizedReactionRecipe.class);

        public static final Recipe<AmbientAccumulatorRecipe> AMBIENT_ACCUMULATOR = new Recipe<>(MachineType.AMBIENT_ACCUMULATOR, AmbientAccumulatorRecipe.class);

        public static final Recipe<FluidToFluidRecipe> THERMAL_EVAPORATION_PLANT = new Recipe<>("ThermalEvaporationPlant", FluidToFluidRecipe.class);

        public static final Recipe<GasToGasRecipe> SOLAR_NEUTRON_ACTIVATOR = new Recipe<>(MachineType.SOLAR_NEUTRON_ACTIVATOR, GasToGasRecipe.class);

        static {
            values = ImmutableList.copyOf(values);
        }

        public static Iterable<Recipe> values() {
            return values;
        }

        private final List<RECIPE_TYPE> recipes = new ArrayList<>();
        private final String recipeName;
        @Nonnull
        private final String jeiCategory;

        private Class<RECIPE_TYPE> recipeClass;

        private Recipe(MachineType type, Class<RECIPE_TYPE> recipe) {
            this(type.getBlockName(), recipe);
        }

        private Recipe(String name, Class<RECIPE_TYPE> recipe) {
            recipeName = name;
            jeiCategory = "mekanism." + recipeName.toLowerCase(Locale.ROOT);

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

        @Nonnull
        public String getJEICategory() {
            return jeiCategory;
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