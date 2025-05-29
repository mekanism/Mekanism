package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.basic.BasicSmeltingRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.recipes.vanilla_input.BiChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.ReactionRecipeInput;
import mekanism.api.recipes.vanilla_input.RotaryRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleFluidChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.client.MekanismClient;
import mekanism.client.recipe_viewer.RecipeViewerUtils;
import mekanism.common.Mekanism;
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
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismRecipeType<VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache>
      implements RecipeType<RECIPE>, IMekanismRecipeTypeProvider<VANILLA_INPUT, RECIPE, INPUT_CACHE> {

    public static final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister(Mekanism.MODID);

    public static final RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> CRUSHING = register(MekanismRecipeTypes.NAME_CRUSHING, recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> ENRICHING = register(MekanismRecipeTypes.NAME_ENRICHING, recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToItemStackRecipe, SingleItem<ItemStackToItemStackRecipe>> SMELTING = register(MekanismRecipeTypes.NAME_SMELTING, recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));

    public static final RecipeTypeRegistryObject<BiChemicalRecipeInput, ChemicalChemicalToChemicalRecipe, EitherSideChemical<ChemicalChemicalToChemicalRecipe>> CHEMICAL_INFUSING = register(MekanismRecipeTypes.NAME_CHEMICAL_INFUSING, EitherSideChemical::new);

    public static final RecipeTypeRegistryObject<RecipeInput, CombinerRecipe, DoubleItem<CombinerRecipe>> COMBINING = register(MekanismRecipeTypes.NAME_COMBINING, recipeType -> new DoubleItem<>(recipeType, CombinerRecipe::getMainInput, CombinerRecipe::getExtraInput));

    public static final RecipeTypeRegistryObject<SingleFluidRecipeInput, ElectrolysisRecipe, SingleFluid<ElectrolysisRecipe>> SEPARATING = register(MekanismRecipeTypes.NAME_SEPARATING, recipeType -> new SingleFluid<>(recipeType, ElectrolysisRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleFluidChemicalRecipeInput, FluidChemicalToChemicalRecipe, FluidChemical<FluidChemicalToChemicalRecipe>> WASHING = register(MekanismRecipeTypes.NAME_WASHING, recipeType -> new FluidChemical<>(recipeType, FluidChemicalToChemicalRecipe::getFluidInput, FluidChemicalToChemicalRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleFluidRecipeInput, FluidToFluidRecipe, SingleFluid<FluidToFluidRecipe>> EVAPORATING = register(MekanismRecipeTypes.NAME_EVAPORATING, recipeType -> new SingleFluid<>(recipeType, FluidToFluidRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, SingleChemical<ChemicalToChemicalRecipe>> ACTIVATING = register(MekanismRecipeTypes.NAME_ACTIVATING, recipeType -> new SingleChemical<>(recipeType, ChemicalToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalToChemicalRecipe, SingleChemical<ChemicalToChemicalRecipe>> CENTRIFUGING = register(MekanismRecipeTypes.NAME_CENTRIFUGING, recipeType -> new SingleChemical<>(recipeType, ChemicalToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleChemicalRecipeInput, ChemicalCrystallizerRecipe, SingleChemical<ChemicalCrystallizerRecipe>> CRYSTALLIZING = register(MekanismRecipeTypes.NAME_CRYSTALLIZING, recipeType -> new SingleChemical<>(recipeType, ChemicalCrystallizerRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ChemicalDissolutionRecipe, ItemChemical<ChemicalDissolutionRecipe>> DISSOLUTION = register(MekanismRecipeTypes.NAME_DISSOLUTION, recipeType -> new ItemChemical<>(recipeType, ChemicalDissolutionRecipe::getItemInput, ChemicalDissolutionRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> COMPRESSING = register(MekanismRecipeTypes.NAME_COMPRESSING, recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> PURIFYING = register(MekanismRecipeTypes.NAME_PURIFYING, recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> INJECTING = register(MekanismRecipeTypes.NAME_INJECTING, recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, NucleosynthesizingRecipe, ItemChemical<NucleosynthesizingRecipe>> NUCLEOSYNTHESIZING = register(MekanismRecipeTypes.NAME_NUCLEOSYNTHESIZING, recipeType -> new ItemChemical<>(recipeType, NucleosynthesizingRecipe::getItemInput, NucleosynthesizingRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToEnergyRecipe, SingleItem<ItemStackToEnergyRecipe>> ENERGY_CONVERSION = register(MekanismRecipeTypes.NAME_ENERGY_CONVERSION, recipeType -> new SingleItem<>(recipeType, ItemStackToEnergyRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToChemicalRecipe, SingleItem<ItemStackToChemicalRecipe>> CHEMICAL_CONVERSION = register(MekanismRecipeTypes.NAME_CHEMICAL_CONVERSION, recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToChemicalRecipe, SingleItem<ItemStackToChemicalRecipe>> OXIDIZING = register(MekanismRecipeTypes.NAME_OXIDIZING, recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, ItemStackToChemicalRecipe, SingleItem<ItemStackToChemicalRecipe>> PIGMENT_EXTRACTING = register(MekanismRecipeTypes.NAME_PIGMENT_EXTRACTING, recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<BiChemicalRecipeInput, ChemicalChemicalToChemicalRecipe, EitherSideChemical<ChemicalChemicalToChemicalRecipe>> PIGMENT_MIXING = register(MekanismRecipeTypes.NAME_PIGMENT_MIXING, EitherSideChemical::new);

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> METALLURGIC_INFUSING = register(MekanismRecipeTypes.NAME_METALLURGIC_INFUSING, recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, ItemStackChemicalToItemStackRecipe, ItemChemical<ItemStackChemicalToItemStackRecipe>> PAINTING = register(MekanismRecipeTypes.NAME_PAINTING, recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<ReactionRecipeInput, PressurizedReactionRecipe, ItemFluidChemical<PressurizedReactionRecipe>> REACTION = register(MekanismRecipeTypes.NAME_REACTION, recipeType -> new ItemFluidChemical<>(recipeType, PressurizedReactionRecipe::getInputSolid, PressurizedReactionRecipe::getInputFluid, PressurizedReactionRecipe::getInputChemical));

    public static final RecipeTypeRegistryObject<RotaryRecipeInput, RotaryRecipe, RotaryInputRecipeCache> ROTARY = register(MekanismRecipeTypes.NAME_ROTARY, RotaryInputRecipeCache::new);

    public static final RecipeTypeRegistryObject<SingleRecipeInput, SawmillRecipe, SingleItem<SawmillRecipe>> SAWING = register(MekanismRecipeTypes.NAME_SAWING, recipeType -> new SingleItem<>(recipeType, SawmillRecipe::getInput));

    private static <VANILLA_INPUT extends RecipeInput, RECIPE extends MekanismRecipe<VANILLA_INPUT>, INPUT_CACHE extends IInputRecipeCache>
    RecipeTypeRegistryObject<VANILLA_INPUT, RECIPE, INPUT_CACHE> register(
          ResourceLocation name,
          Function<MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator
    ) {
        if (!Mekanism.MODID.equals(name.getNamespace())) {
            throw new IllegalStateException("Name must be in " + Mekanism.MODID + " namespace");
        }
        return RECIPE_TYPES.registerMek(name.getPath(), registryName -> new MekanismRecipeType<>(registryName, inputCacheCreator));
    }

    public static void clearCache() {
        for (Holder<RecipeType<?>> entry : RECIPE_TYPES.getEntries()) {
            //Note: We expect all entries to be a MekanismRecipeType, but we validate it just to be sure
            if (entry.value() instanceof MekanismRecipeType<?, ?, ?> recipeType) {
                recipeType.clearCaches();
            }
        }
    }

    public static boolean checkIncompleteRecipes(MinecraftServer server) {
        return checkIncompleteRecipes(server.getRecipeManager(), server.registryAccess());
    }

    public static boolean checkIncompleteRecipes(RecipeManager recipeManager, RegistryAccess registryAccess) {
        boolean foundIncompleteRecipes = false;
        for (DeferredHolder<RecipeType<?>, ? extends RecipeType<?>> holder : RECIPE_TYPES.getEntries()) {
            MekanismRecipeType<?, ?, ?> recipeType = (MekanismRecipeType<?, ?, ?>) holder.value();
            foundIncompleteRecipes |= recipeType.checkMyIncompleteRecipes(recipeManager, registryAccess);
        }
        return foundIncompleteRecipes;
    }

    private List<RecipeHolder<RECIPE>> cachedRecipes = Collections.emptyList();
    private final ResourceLocation registryName;
    private final INPUT_CACHE inputCache;

    private MekanismRecipeType(ResourceLocation name, Function<MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
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
    public MekanismRecipeType<VANILLA_INPUT, RECIPE, INPUT_CACHE> getRecipeType() {
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
    private static RegistryAccess tryGetRegistryAccess() {
        //Try to get a fallback world if we are in a context that may not have one
        //If we are on the client get the client's world, if we are on the server get the current server's world
        if (FMLEnvironment.dist.isClient()) {
            Level clientWorld = MekanismClient.tryGetClientWorld();
            return clientWorld != null ? clientWorld.registryAccess() : null;
        }
        return Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(), "Server not running?").registryAccess();
    }

    @NotNull
    @Override
    public List<RecipeHolder<RECIPE>> getRecipes(@Nullable Level world) {
        RecipeManager recipeManager = null;
        RegistryAccess registryAccess = null;
        if (world == null) {
            //Try to get a fallback world if we are in a context that may not have one
            //If we are on the client get the client's world, if we are on the server get the current server's world
            if (FMLEnvironment.dist.isClient()) {
                Level clientWorld = MekanismClient.tryGetClientWorld();
                if (clientWorld != null) {
                    recipeManager = clientWorld.getRecipeManager();
                    registryAccess = clientWorld.registryAccess();
                }
            } else {
                MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
                if (currentServer != null) {
                    recipeManager = currentServer.getRecipeManager();
                    registryAccess = currentServer.registryAccess();
                }
            }
        } else {
            recipeManager = world.getRecipeManager();
            registryAccess = world.registryAccess();
        }
        if (recipeManager == null) {
            //If we failed, then return no recipes
            return Collections.emptyList();
        }
        return getRecipes(recipeManager, registryAccess);
    }

    @Override
    public @NotNull List<RecipeHolder<RECIPE>> getRecipes(RecipeManager recipeManager) {
        return getRecipes(recipeManager, tryGetRegistryAccess());
    }

    @NotNull
    @Override
    public List<RecipeHolder<RECIPE>> getRecipes(@NotNull RecipeManager recipeManager, @Nullable RegistryAccess registryAccess) {
        if (cachedRecipes.isEmpty()) {
            //Note: This is a fresh immutable list that gets returned
            List<RecipeHolder<RECIPE>> recipes = getRecipesUncached(recipeManager, registryAccess);
            //Make the list of cached recipes immutable and filter out any incomplete recipes
            // as there is no reason to potentially look the partial complete piece up if
            // the other portion of the recipe is incomplete
            cachedRecipes = recipes.stream()
                  .filter(recipe -> !recipe.value().isIncomplete())
                  .toList();
        }
        return cachedRecipes;
    }

    /**
     * Get a list of recipes directly from the manager
     *
     * @param recipeManager  The manager to query
     * @param registryAccess Registry access to use (for Smelting only). Call {@link #tryGetRegistryAccess()} if none directly available
     */
    @NotNull
    private List<RecipeHolder<RECIPE>> getRecipesUncached(@NotNull RecipeManager recipeManager, @Nullable RegistryAccess registryAccess) {
        //Note: This is a fresh mutable list that gets returned
        List<RecipeHolder<RECIPE>> recipes = recipeManager.getAllRecipesFor(this);
        if (this == SMELTING.get()) {
            if (registryAccess == null) {
                //If we failed, then only return the recipes that are for the base type
                return recipes;
            }
            //Ensure the recipes can be modified
            recipes = new ArrayList<>(recipes);
            for (RecipeHolder<SmeltingRecipe> smeltingRecipe : recipeManager.getAllRecipesFor(RecipeType.SMELTING)) {
                ItemStack recipeOutput = smeltingRecipe.value().getResultItem(registryAccess);
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
        return recipes;
    }

    @SuppressWarnings("unchecked")
    private RECIPE castRecipe(MekanismRecipe<?> o) {
        if (o.getType() != this) {
            throw new IllegalArgumentException("Wrong recipe type");
        }
        return (RECIPE) o;
    }

    private boolean checkMyIncompleteRecipes(RecipeManager recipeManager, RegistryAccess registryAccess) {
        boolean incomplete = false;
        for (RecipeHolder<RECIPE> holder : getRecipesUncached(recipeManager, registryAccess)) {
            if (!holder.value().isIncomplete()) {
                continue;
            }
            Mekanism.logger.error("Incomplete recipe detected: {}", holder.id());
            incomplete = true;
            holder.value().logMissingTags();
        }
        return incomplete;
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