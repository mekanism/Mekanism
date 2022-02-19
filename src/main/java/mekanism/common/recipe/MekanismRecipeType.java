package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.inventory.IgnoredIInventory;
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
import mekanism.api.recipes.inputs.ItemStackIngredient;
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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.IForgeRegistry;

public class MekanismRecipeType<RECIPE_TYPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> implements IRecipeType<RECIPE_TYPE> {

    private static final List<MekanismRecipeType<?, ?>> types = new ArrayList<>();

    public static final MekanismRecipeType<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> CRUSHING =
          create("crushing", recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));
    public static final MekanismRecipeType<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> ENRICHING =
          create("enriching", recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));
    public static final MekanismRecipeType<ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> SMELTING =
          create("smelting", recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));

    public static final MekanismRecipeType<ChemicalInfuserRecipe, EitherSideChemical<Gas, GasStack, ChemicalInfuserRecipe>> CHEMICAL_INFUSING =
          create("chemical_infusing", EitherSideChemical::new);

    public static final MekanismRecipeType<CombinerRecipe, DoubleItem<CombinerRecipe>> COMBINING =
          create("combining", recipeType -> new DoubleItem<>(recipeType, CombinerRecipe::getMainInput, CombinerRecipe::getExtraInput));

    public static final MekanismRecipeType<ElectrolysisRecipe, SingleFluid<ElectrolysisRecipe>> SEPARATING =
          create("separating", recipeType -> new SingleFluid<>(recipeType, ElectrolysisRecipe::getInput));

    public static final MekanismRecipeType<FluidSlurryToSlurryRecipe, FluidChemical<Slurry, SlurryStack, FluidSlurryToSlurryRecipe>> WASHING =
          create("washing", recipeType -> new FluidChemical<>(recipeType, FluidChemicalToChemicalRecipe::getFluidInput,
                FluidChemicalToChemicalRecipe::getChemicalInput));

    public static final MekanismRecipeType<FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> EVAPORATING =
          create("evaporating", recipeType -> new SingleFluid<>(recipeType, FluidToFluidRecipe::getInput));

    public static final MekanismRecipeType<GasToGasRecipe, SingleChemical<Gas, GasStack, GasToGasRecipe>> ACTIVATING =
          create("activating", recipeType -> new SingleChemical<>(recipeType, ChemicalToChemicalRecipe::getInput));
    public static final MekanismRecipeType<GasToGasRecipe, SingleChemical<Gas, GasStack, GasToGasRecipe>> CENTRIFUGING =
          create("centrifuging", recipeType -> new SingleChemical<>(recipeType, ChemicalToChemicalRecipe::getInput));

    public static final MekanismRecipeType<ChemicalCrystallizerRecipe, ChemicalCrystallizerInputRecipeCache> CRYSTALLIZING = create("crystallizing",
          ChemicalCrystallizerInputRecipeCache::new);

    public static final MekanismRecipeType<ChemicalDissolutionRecipe, ItemChemical<Gas, GasStack, ChemicalDissolutionRecipe>> DISSOLUTION =
          create("dissolution", recipeType -> new ItemChemical<>(recipeType, ChemicalDissolutionRecipe::getItemInput, ChemicalDissolutionRecipe::getGasInput));

    public static final MekanismRecipeType<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> COMPRESSING =
          create("compressing", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));
    public static final MekanismRecipeType<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> PURIFYING =
          create("purifying", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));
    public static final MekanismRecipeType<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> INJECTING =
          create("injecting", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final MekanismRecipeType<NucleosynthesizingRecipe, ItemChemical<Gas, GasStack, NucleosynthesizingRecipe>> NUCLEOSYNTHESIZING =
          create("nucleosynthesizing", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final MekanismRecipeType<ItemStackToEnergyRecipe, SingleItem<ItemStackToEnergyRecipe>> ENERGY_CONVERSION =
          create("energy_conversion", recipeType -> new SingleItem<>(recipeType, ItemStackToEnergyRecipe::getInput));

    public static final MekanismRecipeType<ItemStackToGasRecipe, SingleItem<ItemStackToGasRecipe>> GAS_CONVERSION =
          create("gas_conversion", recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));
    public static final MekanismRecipeType<ItemStackToGasRecipe, SingleItem<ItemStackToGasRecipe>> OXIDIZING =
          create("oxidizing", recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final MekanismRecipeType<ItemStackToInfuseTypeRecipe, SingleItem<ItemStackToInfuseTypeRecipe>> INFUSION_CONVERSION =
          create("infusion_conversion", recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final MekanismRecipeType<ItemStackToPigmentRecipe, SingleItem<ItemStackToPigmentRecipe>> PIGMENT_EXTRACTING =
          create("pigment_extracting", recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final MekanismRecipeType<PigmentMixingRecipe, EitherSideChemical<Pigment, PigmentStack, PigmentMixingRecipe>> PIGMENT_MIXING =
          create("pigment_mixing", EitherSideChemical::new);

    public static final MekanismRecipeType<MetallurgicInfuserRecipe, ItemChemical<InfuseType, InfusionStack, MetallurgicInfuserRecipe>> METALLURGIC_INFUSING =
          create("metallurgic_infusing", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final MekanismRecipeType<PaintingRecipe, ItemChemical<Pigment, PigmentStack, PaintingRecipe>> PAINTING =
          create("painting", recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput,
                ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final MekanismRecipeType<PressurizedReactionRecipe, ItemFluidChemical<Gas, GasStack, PressurizedReactionRecipe>> REACTION =
          create("reaction", recipeType -> new ItemFluidChemical<>(recipeType, PressurizedReactionRecipe::getInputSolid,
                PressurizedReactionRecipe::getInputFluid, PressurizedReactionRecipe::getInputGas));

    public static final MekanismRecipeType<RotaryRecipe, RotaryInputRecipeCache> ROTARY = create("rotary", RotaryInputRecipeCache::new);

    public static final MekanismRecipeType<SawmillRecipe, SingleItem<SawmillRecipe>> SAWING =
          create("sawing", recipeType -> new SingleItem<>(recipeType, SawmillRecipe::getInput));

    private static <RECIPE_TYPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> MekanismRecipeType<RECIPE_TYPE, INPUT_CACHE> create(String name,
          Function<MekanismRecipeType<RECIPE_TYPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        MekanismRecipeType<RECIPE_TYPE, INPUT_CACHE> type = new MekanismRecipeType<>(name, inputCacheCreator);
        types.add(type);
        return type;
    }

    //TODO: Convert this to using the proper forge registry once we stop needing to directly use the vanilla registry as a work around
    public static void registerRecipeTypes(IForgeRegistry<IRecipeSerializer<?>> registry) {
        types.forEach(type -> Registry.register(Registry.RECIPE_TYPE, type.registryName, type));
    }

    public static void clearCache() {
        types.forEach(MekanismRecipeType::clearCaches);
    }

    private List<RECIPE_TYPE> cachedRecipes = Collections.emptyList();
    private final ResourceLocation registryName;
    private final INPUT_CACHE inputCache;

    private MekanismRecipeType(String name, Function<MekanismRecipeType<RECIPE_TYPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        this.registryName = Mekanism.rl(name);
        this.inputCache = inputCacheCreator.apply(this);
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    public ResourceLocation getRegistryName() {
        return registryName;
    }

    private void clearCaches() {
        cachedRecipes.clear();
        inputCache.clear();
    }

    public INPUT_CACHE getInputCache() {
        return inputCache;
    }

    @Nonnull
    public List<RECIPE_TYPE> getRecipes(@Nullable World world) {
        if (world == null) {
            //Try to get a fallback world if we are in a context that may not have one
            //If we are on the client get the client's world, if we are on the server get the current server's world
            world = DistExecutor.safeRunForDist(() -> MekanismClient::tryGetClientWorld, () -> () -> ServerLifecycleHooks.getCurrentServer().overworld());
            if (world == null) {
                //If we failed, then return no recipes
                return Collections.emptyList();
            }
        }
        if (cachedRecipes.isEmpty()) {
            RecipeManager recipeManager = world.getRecipeManager();
            //TODO: Should we use the byType(RecipeType) that we ATd so that our recipes don't have to always return true for matching?
            List<RECIPE_TYPE> recipes = recipeManager.getRecipesFor(this, IgnoredIInventory.INSTANCE, world);
            if (this == SMELTING) {
                Map<ResourceLocation, IRecipe<IInventory>> smeltingRecipes = recipeManager.byType(IRecipeType.SMELTING);
                //Copy recipes our recipes to make sure it is mutable
                recipes = new ArrayList<>(recipes);
                for (Entry<ResourceLocation, IRecipe<IInventory>> entry : smeltingRecipes.entrySet()) {
                    IRecipe<IInventory> smeltingRecipe = entry.getValue();
                    ItemStack recipeOutput = smeltingRecipe.getResultItem();
                    if (!smeltingRecipe.isSpecial() && !recipeOutput.isEmpty()) {
                        //TODO: Can Smelting recipes even be "special", if so can we add some sort of checker to make getOutput return the correct result
                        NonNullList<Ingredient> ingredients = smeltingRecipe.getIngredients();
                        int ingredientCount = ingredients.size();
                        ItemStackIngredient input;
                        if (ingredientCount == 0) {
                            //Something went wrong
                            continue;
                        } else if (ingredientCount == 1) {
                            input = ItemStackIngredient.from(ingredients.get(0));
                        } else {
                            ItemStackIngredient[] itemIngredients = new ItemStackIngredient[ingredientCount];
                            for (int i = 0; i < ingredientCount; i++) {
                                itemIngredients[i] = ItemStackIngredient.from(ingredients.get(i));
                            }
                            input = ItemStackIngredient.createMulti(itemIngredients);
                        }
                        recipes.add((RECIPE_TYPE) new SmeltingIRecipe(entry.getKey(), input, recipeOutput));
                    }
                }
            }
            cachedRecipes = recipes;
        }
        return cachedRecipes;
    }

    public Stream<RECIPE_TYPE> stream(@Nullable World world) {
        return getRecipes(world).stream();
    }

    /**
     * Finds the first recipe that matches the given criteria, or null if no matching recipe is found. Prefer using the find recipe methods in {@link #getInputCache()}.
     */
    @Nullable
    public RECIPE_TYPE findFirst(@Nullable World world, Predicate<RECIPE_TYPE> matchCriteria) {
        return stream(world).filter(matchCriteria).findFirst().orElse(null);
    }

    /**
     * Checks if this recipe type contains a recipe that matches the given criteria. Prefer using the contains recipe methods in {@link #getInputCache()}.
     */
    public boolean contains(@Nullable World world, Predicate<RECIPE_TYPE> matchCriteria) {
        return stream(world).anyMatch(matchCriteria);
    }
}