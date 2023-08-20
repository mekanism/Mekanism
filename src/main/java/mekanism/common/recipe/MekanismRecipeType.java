package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.SmeltingIRecipe;
import mekanism.common.recipe.lookup.cache.ChemicalCrystallizerInputRecipeCache;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.DoubleItem;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.EitherSideChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.FluidChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemFluidChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleChemical;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.recipe.lookup.cache.RotaryInputRecipeCache;
import mekanism.common.registration.impl.RecipeTypeDeferredRegister;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismRecipeType<RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> implements RecipeType<RECIPE>,
      IMekanismRecipeTypeProvider<RECIPE, INPUT_CACHE> {

    public static final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister(Mekanism.MODID);

    public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> CRUSHING =
          register("crushing", recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));
    public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> ENRICHING =
          register("enriching", recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));
    public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> SMELTING =
          register("smelting", recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));

    public static final RecipeTypeRegistryObject<ChemicalInfuserRecipe, EitherSideChemical<Gas, GasStack, ChemicalInfuserRecipe>> CHEMICAL_INFUSING =
          register("chemical_infusing", EitherSideChemical::new);

    public static final RecipeTypeRegistryObject<CombinerRecipe, DoubleItem<CombinerRecipe>> COMBINING =
          register("combining", recipeType -> new DoubleItem<>(recipeType, CombinerRecipe::getMainInput, CombinerRecipe::getExtraInput));

    public static final RecipeTypeRegistryObject<ElectrolysisRecipe, SingleFluid<ElectrolysisRecipe>> SEPARATING =
          register("separating", recipeType -> new SingleFluid<>(recipeType, ElectrolysisRecipe::getInput));

    public static final RecipeTypeRegistryObject<FluidSlurryToSlurryRecipe, FluidChemical<Slurry, SlurryStack, FluidSlurryToSlurryRecipe>> WASHING =
          register("washing", recipeType -> new FluidChemical<>(recipeType, FluidChemicalToChemicalRecipe::getFluidInput,
                FluidChemicalToChemicalRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> EVAPORATING =
          register("evaporating", recipeType -> new SingleFluid<>(recipeType, FluidToFluidRecipe::getInput));

    public static final RecipeTypeRegistryObject<GasToGasRecipe, SingleChemical<Gas, GasStack, GasToGasRecipe>> ACTIVATING =
          register("activating", recipeType -> new SingleChemical<>(recipeType, ChemicalToChemicalRecipe::getInput));
    public static final RecipeTypeRegistryObject<GasToGasRecipe, SingleChemical<Gas, GasStack, GasToGasRecipe>> CENTRIFUGING =
          register("centrifuging", recipeType -> new SingleChemical<>(recipeType, ChemicalToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<ChemicalCrystallizerRecipe, ChemicalCrystallizerInputRecipeCache> CRYSTALLIZING = register("crystallizing",
          ChemicalCrystallizerInputRecipeCache::new);

    public static final RecipeTypeRegistryObject<ChemicalDissolutionRecipe, ItemChemical<Gas, GasStack, ChemicalDissolutionRecipe>> DISSOLUTION =
          register("dissolution", recipeType -> new ItemChemical<>(recipeType, ChemicalDissolutionRecipe::getItemInput, ChemicalDissolutionRecipe::getGasInput));

    public static final RecipeTypeRegistryObject<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> COMPRESSING =
          register("compressing", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));
    public static final RecipeTypeRegistryObject<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> PURIFYING =
          register("purifying", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));
    public static final RecipeTypeRegistryObject<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> INJECTING =
          register("injecting", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<NucleosynthesizingRecipe, ItemChemical<Gas, GasStack, NucleosynthesizingRecipe>> NUCLEOSYNTHESIZING =
          register("nucleosynthesizing", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<ItemStackToEnergyRecipe, SingleItem<ItemStackToEnergyRecipe>> ENERGY_CONVERSION =
          register("energy_conversion", recipeType -> new SingleItem<>(recipeType, ItemStackToEnergyRecipe::getInput));

    public static final RecipeTypeRegistryObject<ItemStackToGasRecipe, SingleItem<ItemStackToGasRecipe>> GAS_CONVERSION =
          register("gas_conversion", recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));
    public static final RecipeTypeRegistryObject<ItemStackToGasRecipe, SingleItem<ItemStackToGasRecipe>> OXIDIZING =
          register("oxidizing", recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<ItemStackToInfuseTypeRecipe, SingleItem<ItemStackToInfuseTypeRecipe>> INFUSION_CONVERSION =
          register("infusion_conversion", recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<ItemStackToPigmentRecipe, SingleItem<ItemStackToPigmentRecipe>> PIGMENT_EXTRACTING =
          register("pigment_extracting", recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<PigmentMixingRecipe, EitherSideChemical<Pigment, PigmentStack, PigmentMixingRecipe>> PIGMENT_MIXING =
          register("pigment_mixing", EitherSideChemical::new);

    public static final RecipeTypeRegistryObject<MetallurgicInfuserRecipe, ItemChemical<InfuseType, InfusionStack, MetallurgicInfuserRecipe>> METALLURGIC_INFUSING =
          register("metallurgic_infusing", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<PaintingRecipe, ItemChemical<Pigment, PigmentStack, PaintingRecipe>> PAINTING =
          register("painting", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<PressurizedReactionRecipe, ItemFluidChemical<Gas, GasStack, PressurizedReactionRecipe>> REACTION =
          register("reaction", recipeType -> new ItemFluidChemical<>(recipeType, PressurizedReactionRecipe::getInputSolid,
                PressurizedReactionRecipe::getInputFluid, PressurizedReactionRecipe::getInputGas));

    public static final RecipeTypeRegistryObject<RotaryRecipe, RotaryInputRecipeCache> ROTARY = register("rotary", RotaryInputRecipeCache::new);

    public static final RecipeTypeRegistryObject<SawmillRecipe, SingleItem<SawmillRecipe>> SAWING =
          register("sawing", recipeType -> new SingleItem<>(recipeType, SawmillRecipe::getInput));

    public static <RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> RecipeTypeRegistryObject<RECIPE, INPUT_CACHE> register(String name,
          Function<MekanismRecipeType<RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        return RECIPE_TYPES.register(name, () -> new MekanismRecipeType<>(name, inputCacheCreator));
    }

    public static void clearCache() {
        for (IMekanismRecipeTypeProvider<?, ?> recipeTypeProvider : RECIPE_TYPES.getAllRecipeTypes()) {
            recipeTypeProvider.getRecipeType().clearCaches();
        }
    }

    private List<RECIPE> cachedRecipes = Collections.emptyList();
    private final ResourceLocation registryName;
    private final INPUT_CACHE inputCache;

    private MekanismRecipeType(String name, Function<MekanismRecipeType<RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        this.registryName = Mekanism.rl(name);
        this.inputCache = inputCacheCreator.apply(this);
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public MekanismRecipeType<RECIPE, INPUT_CACHE> getRecipeType() {
        return this;
    }

    private void clearCaches() {
        cachedRecipes = Collections.emptyList();
        inputCache.clear();
    }

    @Override
    public INPUT_CACHE getInputCache() {
        return inputCache;
    }

    @NotNull
    @Override
    public List<RECIPE> getRecipes(@Nullable Level world) {
        if (world == null) {
            //Try to get a fallback world if we are in a context that may not have one
            //If we are on the client get the client's world, if we are on the server get the current server's world
            if (FMLEnvironment.dist.isClient()) {
                world = MekanismClient.tryGetClientWorld();
            } else {
                world = ServerLifecycleHooks.getCurrentServer().overworld();
            }
            if (world == null) {
                //If we failed, then return no recipes
                return Collections.emptyList();
            }
        }
        if (cachedRecipes.isEmpty()) {
            RecipeManager recipeManager = world.getRecipeManager();
            //Note: This is a fresh mutable list that gets returned
            List<RECIPE> recipes = recipeManager.getAllRecipesFor(this);
            if (this == SMELTING.get()) {
                //Ensure the recipes can be modified
                recipes = new ArrayList<>(recipes);
                for (SmeltingRecipe smeltingRecipe : recipeManager.getAllRecipesFor(RecipeType.SMELTING)) {
                    ItemStack recipeOutput = smeltingRecipe.getResultItem(world.registryAccess());
                    if (!smeltingRecipe.isSpecial() && !smeltingRecipe.isIncomplete() && !recipeOutput.isEmpty()) {
                        //TODO: Can Smelting recipes even be "special", if so can we add some sort of checker to make getOutput return the correct result
                        NonNullList<Ingredient> ingredients = smeltingRecipe.getIngredients();
                        ItemStackIngredient input;
                        if (ingredients.isEmpty()) {
                            //Something went wrong
                            continue;
                        } else {
                            IItemStackIngredientCreator ingredientCreator = IngredientCreatorAccess.item();
                            input = ingredientCreator.from(ingredients.stream().map(ingredientCreator::from));
                        }
                        recipes.add((RECIPE) new SmeltingIRecipe(smeltingRecipe.getId(), input, recipeOutput));
                    }
                }
            }
            //Make the list of cached recipes immutable and filter out any incomplete recipes
            // as there is no reason to potentially look the partial complete piece up if
            // the other portion of the recipe is incomplete
            cachedRecipes = recipes.stream()
                  .filter(recipe -> !recipe.isIncomplete())
                  .toList();
        }
        return cachedRecipes;
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static <C extends Container, RECIPE_TYPE extends Recipe<C>> Optional<RECIPE_TYPE> getRecipeFor(RecipeType<RECIPE_TYPE> recipeType, C inventory, Level level) {
        //Only allow looking up complete recipes or special recipes as we only use this method for vanilla recipe types
        // and special recipes return that they are not complete
        return level.getRecipeManager().getRecipeFor(recipeType, inventory, level)
              .filter(recipe -> recipe.isSpecial() || !recipe.isIncomplete());
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static Optional<? extends Recipe<?>> byKey(Level level, ResourceLocation id) {
        //Only allow looking up complete recipes or special recipes as we only use this method for vanilla recipe types
        // and special recipes return that they are not complete
        return level.getRecipeManager().byKey(id)
              .filter(recipe -> recipe.isSpecial() || !recipe.isIncomplete());
    }
}
