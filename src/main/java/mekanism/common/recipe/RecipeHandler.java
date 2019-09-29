package mekanism.common.recipe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.gas.GasStack;
import mekanism.api.inventory.IgnoredIInventory;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IGasProvider;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidGasToGasRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.world.World;

/**
 * Class used to handle machine recipes. This is used for both adding and fetching recipes.
 *
 * @author AidanBrady, unpairedbracket
 */
@ParametersAreNonnullByDefault
public final class RecipeHandler {

    /**
     * Add a Combiner recipe.
     *
     * @param input  - input ItemStack
     * @param extra  - extra ItemStack
     * @param output - output ItemStack
     */
    @Deprecated
    public static void addCombinerRecipe(ItemStackIngredient input, ItemStackIngredient extra, ItemStack output) {
        //TODO: API way of adding recipes
        //Recipe.COMBINER.put(new CombinerRecipe(input, extra, output));
    }

    /**
     * Add a Metallurgic Infuser recipe.
     *
     * @param infusionIngredient - which Infuse to use
     * @param input              - input ItemStack
     * @param output             - output ItemStack
     */
    @Deprecated
    public static void addMetallurgicInfuserRecipe(InfusionIngredient infusionIngredient, ItemStackIngredient input, ItemStack output) {
        //TODO: API way of adding recipes
        //Recipe.METALLURGIC_INFUSER.put(new MetallurgicInfuserRecipe(input, infusionIngredient, output));
    }

    /**
     * Add a Precision Sawmill recipe.
     *
     * @param input           - input ItemStack
     * @param primaryOutput   - guaranteed output
     * @param secondaryOutput - possible extra output
     * @param chance          - probability of obtaining extra output
     */
    @Deprecated
    public static void addPrecisionSawmillRecipe(ItemStackIngredient input, ItemStack primaryOutput, ItemStack secondaryOutput, double chance) {
        //TODO: API way of adding recipes
        //Recipe.PRECISION_SAWMILL.put(new SawmillRecipe(input, primaryOutput, secondaryOutput, chance));
    }

    /**
     * Add a Precision Sawmill recipe with no chance output
     *
     * @param input         - input ItemStack
     * @param primaryOutput - guaranteed output
     */
    @Deprecated
    public static void addPrecisionSawmillRecipe(ItemStackIngredient input, ItemStack primaryOutput) {
        addPrecisionSawmillRecipe(input, primaryOutput, ItemStack.EMPTY, 0);
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
    @Deprecated
    public static void addPRCRecipe(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, ItemStack outputSolid, IGasProvider outputGas,
          int gasOutputAmount, double extraEnergy, int ticks) {
        //TODO: API way of adding recipes
        //Recipe.PRESSURIZED_REACTION_CHAMBER.put(new PressurizedReactionRecipe(inputSolid, inputFluid, inputGas, outputGas.getGas(), gasOutputAmount, extraEnergy, ticks, outputSolid));
    }

    @Deprecated
    public static void addPRCRecipe(ItemStackIngredient inputSolid, FluidStackIngredient inputFluid, GasStackIngredient inputGas, ItemStack outputSolid, GasStack outputGas,
          double extraEnergy, int ticks) {
        //TODO: API way of adding recipes
        //Recipe.PRESSURIZED_REACTION_CHAMBER.put(new PressurizedReactionRecipe(inputSolid, inputFluid, inputGas, outputGas, extraEnergy, ticks, outputSolid));
    }

    public static class RecipeWrapper<RECIPE_TYPE extends MekanismRecipe> {

        public static final RecipeWrapper<ChemicalInfuserRecipe> CHEMICAL_INFUSING = new RecipeWrapper<>(MekanismRecipeType.CHEMICAL_INFUSING);
        public static final RecipeWrapper<CombinerRecipe> COMBINING = new RecipeWrapper<>(MekanismRecipeType.COMBINING);
        public static final RecipeWrapper<ElectrolysisRecipe> SEPARATING = new RecipeWrapper<>(MekanismRecipeType.SEPARATING);
        public static final RecipeWrapper<FluidGasToGasRecipe> WASHING = new RecipeWrapper<>(MekanismRecipeType.WASHING);
        public static final RecipeWrapper<FluidToFluidRecipe> EVAPORATING = new RecipeWrapper<>(MekanismRecipeType.EVAPORATING);
        public static final RecipeWrapper<GasToGasRecipe> ACTIVATING = new RecipeWrapper<>(MekanismRecipeType.ACTIVATING);
        public static final RecipeWrapper<GasToItemStackRecipe> CRYSTALLIZING = new RecipeWrapper<>(MekanismRecipeType.CRYSTALLIZING);
        public static final RecipeWrapper<ItemStackGasToGasRecipe> DISSOLUTION = new RecipeWrapper<>(MekanismRecipeType.DISSOLUTION);
        public static final RecipeWrapper<ItemStackGasToItemStackRecipe> INJECTING = new RecipeWrapper<>(MekanismRecipeType.INJECTING);
        public static final RecipeWrapper<ItemStackGasToItemStackRecipe> COMPRESSING = new RecipeWrapper<>(MekanismRecipeType.COMPRESSING);
        public static final RecipeWrapper<ItemStackGasToItemStackRecipe> PURIFYING = new RecipeWrapper<>(MekanismRecipeType.PURIFYING);
        public static final RecipeWrapper<ItemStackToGasRecipe> OXIDIZING = new RecipeWrapper<>(MekanismRecipeType.OXIDIZING);
        public static final RecipeWrapper<ItemStackToItemStackRecipe> CRUSHING = new RecipeWrapper<>(MekanismRecipeType.CRUSHING);
        public static final RecipeWrapper<ItemStackToItemStackRecipe> SMELTING = new RecipeWrapper<>(MekanismRecipeType.SMELTING);
        public static final RecipeWrapper<ItemStackToItemStackRecipe> ENRICHING = new RecipeWrapper<>(MekanismRecipeType.ENRICHING);
        public static final RecipeWrapper<MetallurgicInfuserRecipe> METALLURGIC_INFUSING = new RecipeWrapper<>(MekanismRecipeType.METALLURGIC_INFUSING);
        public static final RecipeWrapper<PressurizedReactionRecipe> REACTION = new RecipeWrapper<>(MekanismRecipeType.REACTION);
        public static final RecipeWrapper<SawmillRecipe> SAWING = new RecipeWrapper<>(MekanismRecipeType.SAWING);

        private IRecipeType<RECIPE_TYPE> recipeType;

        private RecipeWrapper(IRecipeType<RECIPE_TYPE> recipeType) {
            this.recipeType = recipeType;
        }

        @Nonnull
        public List<RECIPE_TYPE> getRecipes(@Nullable World world) {
            if (world == null) {
                return Collections.emptyList();
            }
            //TODO: Cache this stuff by dimension. Update it when /reload is run or things
            return world.getRecipeManager().getRecipes(recipeType, IgnoredIInventory.INSTANCE, world);
        }

        public Stream<RECIPE_TYPE> stream(@Nullable World world) {
            return getRecipes(world).stream();
        }

        @Nullable
        public RECIPE_TYPE findFirst(@Nullable World world, Predicate<RECIPE_TYPE> matchCriteria) {
            return stream(world).filter(matchCriteria).findFirst().orElse(null);
        }

        public boolean contains(@Nullable World world, Predicate<RECIPE_TYPE> matchCriteria) {
            return stream(world).anyMatch(matchCriteria);
        }
    }

    //TODO: Move this into its own class if this stuff doesn't just go away becoming a RecipeType
    //TODO: Replace this stuff with RecipeWrapper
    @Deprecated
    public static class Recipe<RECIPE_TYPE extends MekanismRecipe> {

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
        public static final Recipe<GasToItemStackRecipe> CHEMICAL_CRYSTALLIZER = new Recipe<>(MekanismBlock.CHEMICAL_CRYSTALLIZER, GasToItemStackRecipe.class);
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
            return stream().filter(matchCriteria).findFirst().orElse(null);
        }

        public boolean contains(Predicate<RECIPE_TYPE> matchCriteria) {
            return stream().anyMatch(matchCriteria);
        }
    }
}