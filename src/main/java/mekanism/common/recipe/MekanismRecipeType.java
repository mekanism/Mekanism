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
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.basic.BasicSmeltingRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.MekanismClient;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.common.Mekanism;
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
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismRecipeType<RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> implements RecipeType<RECIPE>,
      IMekanismRecipeTypeProvider<RECIPE, INPUT_CACHE> {

    public static final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister(Mekanism.MODID);

    public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> CRUSHING = register(MekanismRecipeTypes.NAME_CRUSHING, recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));
    public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> ENRICHING = register(MekanismRecipeTypes.NAME_ENRICHING, recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));
    public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> SMELTING = register(MekanismRecipeTypes.NAME_SMELTING, recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));

    public static final RecipeTypeRegistryObject<ChemicalInfuserRecipe, EitherSideChemical<Gas, GasStack, ChemicalInfuserRecipe>> CHEMICAL_INFUSING = register(MekanismRecipeTypes.NAME_CHEMICAL_INFUSING, EitherSideChemical::new);

    public static final RecipeTypeRegistryObject<CombinerRecipe, DoubleItem<CombinerRecipe>> COMBINING = register(MekanismRecipeTypes.NAME_COMBINING, recipeType -> new DoubleItem<>(recipeType, CombinerRecipe::getMainInput, CombinerRecipe::getExtraInput));

    public static final RecipeTypeRegistryObject<ElectrolysisRecipe, SingleFluid<ElectrolysisRecipe>> SEPARATING = register(MekanismRecipeTypes.NAME_SEPARATING, recipeType -> new SingleFluid<>(recipeType, ElectrolysisRecipe::getInput));

    public static final RecipeTypeRegistryObject<FluidSlurryToSlurryRecipe, FluidChemical<Slurry, SlurryStack, FluidSlurryToSlurryRecipe>> WASHING = register(MekanismRecipeTypes.NAME_WASHING, recipeType -> new FluidChemical<>(recipeType, FluidSlurryToSlurryRecipe::getFluidInput, FluidSlurryToSlurryRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> EVAPORATING = register(MekanismRecipeTypes.NAME_EVAPORATING, recipeType -> new SingleFluid<>(recipeType, FluidToFluidRecipe::getInput));

    public static final RecipeTypeRegistryObject<GasToGasRecipe, SingleChemical<Gas, GasStack, GasToGasRecipe>> ACTIVATING = register(MekanismRecipeTypes.NAME_ACTIVATING, recipeType -> new SingleChemical<>(recipeType, GasToGasRecipe::getInput));
    public static final RecipeTypeRegistryObject<GasToGasRecipe, SingleChemical<Gas, GasStack, GasToGasRecipe>> CENTRIFUGING = register(MekanismRecipeTypes.NAME_CENTRIFUGING, recipeType -> new SingleChemical<>(recipeType, GasToGasRecipe::getInput));

    public static final RecipeTypeRegistryObject<ChemicalCrystallizerRecipe, ChemicalCrystallizerInputRecipeCache> CRYSTALLIZING = register(MekanismRecipeTypes.NAME_CRYSTALLIZING, ChemicalCrystallizerInputRecipeCache::new);

    public static final RecipeTypeRegistryObject<ChemicalDissolutionRecipe, ItemChemical<Gas, GasStack, ChemicalDissolutionRecipe>> DISSOLUTION = register(MekanismRecipeTypes.NAME_DISSOLUTION, recipeType -> new ItemChemical<>(recipeType, ChemicalDissolutionRecipe::getItemInput, ChemicalDissolutionRecipe::getGasInput));

    public static final RecipeTypeRegistryObject<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> COMPRESSING = register(MekanismRecipeTypes.NAME_COMPRESSING, recipeType -> new ItemChemical<>(recipeType, ItemStackGasToItemStackRecipe::getItemInput, ItemStackGasToItemStackRecipe::getChemicalInput));
    public static final RecipeTypeRegistryObject<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> PURIFYING = register(MekanismRecipeTypes.NAME_PURIFYING, recipeType -> new ItemChemical<>(recipeType, ItemStackGasToItemStackRecipe::getItemInput, ItemStackGasToItemStackRecipe::getChemicalInput));
    public static final RecipeTypeRegistryObject<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> INJECTING = register(MekanismRecipeTypes.NAME_INJECTING, recipeType -> new ItemChemical<>(recipeType, ItemStackGasToItemStackRecipe::getItemInput, ItemStackGasToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<NucleosynthesizingRecipe, ItemChemical<Gas, GasStack, NucleosynthesizingRecipe>> NUCLEOSYNTHESIZING = register(MekanismRecipeTypes.NAME_NUCLEOSYNTHESIZING, recipeType -> new ItemChemical<>(recipeType, NucleosynthesizingRecipe::getItemInput, NucleosynthesizingRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<ItemStackToEnergyRecipe, SingleItem<ItemStackToEnergyRecipe>> ENERGY_CONVERSION = register(MekanismRecipeTypes.NAME_ENERGY_CONVERSION, recipeType -> new SingleItem<>(recipeType, ItemStackToEnergyRecipe::getInput));

    public static final RecipeTypeRegistryObject<ItemStackToGasRecipe, SingleItem<ItemStackToGasRecipe>> GAS_CONVERSION = register(MekanismRecipeTypes.NAME_GAS_CONVERSION, recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));
    public static final RecipeTypeRegistryObject<ItemStackToGasRecipe, SingleItem<ItemStackToGasRecipe>> OXIDIZING = register(MekanismRecipeTypes.NAME_OXIDIZING, recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<ItemStackToInfuseTypeRecipe, SingleItem<ItemStackToInfuseTypeRecipe>> INFUSION_CONVERSION = register(MekanismRecipeTypes.NAME_INFUSION_CONVERSION, recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<ItemStackToPigmentRecipe, SingleItem<ItemStackToPigmentRecipe>> PIGMENT_EXTRACTING = register(MekanismRecipeTypes.NAME_PIGMENT_EXTRACTING, recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<PigmentMixingRecipe, EitherSideChemical<Pigment, PigmentStack, PigmentMixingRecipe>> PIGMENT_MIXING = register(MekanismRecipeTypes.NAME_PIGMENT_MIXING, EitherSideChemical::new);

    public static final RecipeTypeRegistryObject<MetallurgicInfuserRecipe, ItemChemical<InfuseType, InfusionStack, MetallurgicInfuserRecipe>> METALLURGIC_INFUSING = register(MekanismRecipeTypes.NAME_METALLURGIC_INFUSING, recipeType -> new ItemChemical<>(recipeType, MetallurgicInfuserRecipe::getItemInput, MetallurgicInfuserRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<PaintingRecipe, ItemChemical<Pigment, PigmentStack, PaintingRecipe>> PAINTING = register(MekanismRecipeTypes.NAME_PAINTING, recipeType -> new ItemChemical<>(recipeType, PaintingRecipe::getItemInput, PaintingRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<PressurizedReactionRecipe, ItemFluidChemical<Gas, GasStack, PressurizedReactionRecipe>> REACTION = register(MekanismRecipeTypes.NAME_REACTION, recipeType -> new ItemFluidChemical<>(recipeType, PressurizedReactionRecipe::getInputSolid, PressurizedReactionRecipe::getInputFluid, PressurizedReactionRecipe::getInputGas));

    public static final RecipeTypeRegistryObject<RotaryRecipe, RotaryInputRecipeCache> ROTARY = register(MekanismRecipeTypes.NAME_ROTARY, RotaryInputRecipeCache::new);

    public static final RecipeTypeRegistryObject<SawmillRecipe, SingleItem<SawmillRecipe>> SAWING = register(MekanismRecipeTypes.NAME_SAWING, recipeType -> new SingleItem<>(recipeType, SawmillRecipe::getInput));

    private static <RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> RecipeTypeRegistryObject<RECIPE, INPUT_CACHE> register(ResourceLocation name,
          Function<MekanismRecipeType<RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        if (!Mekanism.MODID.equals(name.getNamespace())) {
            throw new IllegalStateException("Name must be in " + Mekanism.MODID + " namespace");
        }
        return RECIPE_TYPES.registerMek(name.getPath(), registryName -> new MekanismRecipeType<>(registryName, inputCacheCreator));
    }

    public static void clearCache() {
        for (Holder<RecipeType<?>> entry : RECIPE_TYPES.getEntries()) {
            //Note: We expect all entries to be a MekanismRecipeType, but we validate it just to be sure
            if (entry.value() instanceof MekanismRecipeType<?, ?> recipeType) {
                recipeType.clearCaches();
            }
        }
    }

    private List<RecipeHolder<RECIPE>> cachedRecipes = Collections.emptyList();
    private final ResourceLocation registryName;
    private final INPUT_CACHE inputCache;

    private MekanismRecipeType(ResourceLocation name, Function<MekanismRecipeType<RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        this.registryName = name;
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

    @Nullable
    private Level getLevel(@Nullable Level level) {
        if (level == null) {
            //Try to get a fallback world if we are in a context that may not have one
            //If we are on the client get the client's world, if we are on the server get the current server's world
            if (FMLEnvironment.dist.isClient()) {
                return MekanismClient.tryGetClientWorld();
            }
            return ServerLifecycleHooks.getCurrentServer().overworld();
        }
        return level;
    }

    @NotNull
    @Override
    public List<RecipeHolder<RECIPE>> getRecipes(@Nullable Level world) {
        world = getLevel(world);
        if (world == null) {
            //If we failed, then return no recipes
            return Collections.emptyList();
        }
        return getRecipes(world.getRecipeManager(), world);
    }

    @NotNull
    @Override
    public List<RecipeHolder<RECIPE>> getRecipes(RecipeManager recipeManager, @Nullable Level world) {
        if (cachedRecipes.isEmpty()) {
            //Note: This is a fresh immutable list that gets returned
            List<RecipeHolder<RECIPE>> recipes = recipeManager.getAllRecipesFor(this);
            if (this == SMELTING.get()) {
                world = getLevel(world);
                if (world == null) {
                    //If we failed, then only return the recipes that are for the base type
                    return recipes.stream()
                          .filter(recipe -> !recipe.value().isIncomplete())
                          .toList();
                }
                //Ensure the recipes can be modified
                recipes = new ArrayList<>(recipes);
                for (RecipeHolder<SmeltingRecipe> smeltingRecipe : recipeManager.getAllRecipesFor(RecipeType.SMELTING)) {
                    ItemStack recipeOutput = smeltingRecipe.value().getResultItem(world.registryAccess());
                    if (!smeltingRecipe.value().isSpecial() && !smeltingRecipe.value().isIncomplete() && !recipeOutput.isEmpty()) {
                        //TODO: Can Smelting recipes even be "special", if so can we add some sort of checker to make getOutput return the correct result
                        NonNullList<Ingredient> ingredients = smeltingRecipe.value().getIngredients();
                        if (ingredients.isEmpty()) {
                            //Something went wrong
                            continue;
                        }
                        ItemStackIngredient input = IngredientCreatorAccess.item().from(CompoundIngredient.of(ingredients.toArray(Ingredient[]::new)));
                        recipes.add(new RecipeHolder<>(RecipeViewerUtils.synthetic(smeltingRecipe.id(), "mekanism_generated"),
                              castRecipe(new BasicSmeltingRecipe(input, recipeOutput))));
                    }
                }
            }
            //Make the list of cached recipes immutable and filter out any incomplete recipes
            // as there is no reason to potentially look the partial complete piece up if
            // the other portion of the recipe is incomplete
            cachedRecipes = recipes.stream()
                  .filter(recipe -> !recipe.value().isIncomplete())
                  .toList();
        }
        return cachedRecipes;
    }

    @SuppressWarnings("unchecked")
    private RECIPE castRecipe(MekanismRecipe o) {
        if (o.getType() != this) {
            throw new IllegalArgumentException("Wrong recipe type");
        }
        return (RECIPE) o;
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static <I extends RecipeInput, RECIPE_TYPE extends Recipe<I>> Optional<RecipeHolder<RECIPE_TYPE>> getRecipeFor(RecipeType<RECIPE_TYPE> recipeType, I input,
          Level level) {
        //Only allow looking up complete recipes or special recipes as we only use this method for vanilla recipe types
        // and special recipes return that they are not complete
        return level.getRecipeManager().getRecipeFor(recipeType, input, level)
              .filter(recipe -> recipe.value().isSpecial() || !recipe.value().isIncomplete());
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static Optional<RecipeHolder<?>> byKey(Level level, ResourceLocation id) {
        //Only allow looking up complete recipes or special recipes as we only use this method for vanilla recipe types
        // and special recipes return that they are not complete
        return level.getRecipeManager().byKey(id)
              .filter(recipe -> recipe.value().isSpecial() || !recipe.value().isIncomplete());
    }
}