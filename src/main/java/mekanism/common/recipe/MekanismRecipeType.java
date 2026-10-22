package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import mekanism.api.recipes.ItemStackToFluidOptionalItemRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.book.ActivatingBookCategory;
import mekanism.api.recipes.book.ActivatingBookCategory.ActivatingBookInfo;
import mekanism.api.recipes.book.CentrifugingBookCategory;
import mekanism.api.recipes.book.CentrifugingBookCategory.CentrifugingBookInfo;
import mekanism.api.recipes.book.ChemicalConversionBookCategory;
import mekanism.api.recipes.book.ChemicalConversionBookCategory.ChemicalConversionBookInfo;
import mekanism.api.recipes.book.ChemicalInfusingBookCategory;
import mekanism.api.recipes.book.ChemicalInfusingBookCategory.ChemicalInfusingBookInfo;
import mekanism.api.recipes.book.CombiningBookCategory;
import mekanism.api.recipes.book.CombiningBookCategory.CombiningBookInfo;
import mekanism.api.recipes.book.CompressingBookCategory;
import mekanism.api.recipes.book.CompressingBookCategory.CompressingBookInfo;
import mekanism.api.recipes.book.CrushingBookCategory;
import mekanism.api.recipes.book.CrushingBookCategory.CrushingBookInfo;
import mekanism.api.recipes.book.CrystallizingBookCategory;
import mekanism.api.recipes.book.CrystallizingBookCategory.CrystallizingBookInfo;
import mekanism.api.recipes.book.DissolutionBookCategory;
import mekanism.api.recipes.book.DissolutionBookCategory.DissolutionBookInfo;
import mekanism.api.recipes.book.ElectrolysisBookCategory;
import mekanism.api.recipes.book.ElectrolysisBookCategory.ElectrolysisBookInfo;
import mekanism.api.recipes.book.EnergyConversionBookCategory;
import mekanism.api.recipes.book.EnergyConversionBookCategory.EnergyConversionBookInfo;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.book.EnrichingBookCategory.EnrichingBookInfo;
import mekanism.api.recipes.book.EvaporatingBookCategory;
import mekanism.api.recipes.book.EvaporatingBookCategory.EvaporatingBookInfo;
import mekanism.api.recipes.book.InjectingBookCategory;
import mekanism.api.recipes.book.InjectingBookCategory.InjectingBookInfo;
import mekanism.api.recipes.book.LiquificationBookCategory;
import mekanism.api.recipes.book.LiquificationBookCategory.LiquificationBookInfo;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory;
import mekanism.api.recipes.book.MetallurgicInfusingBookCategory.MetallurgicInfusingBookInfo;
import mekanism.api.recipes.book.NucleosynthesizingBookCategory;
import mekanism.api.recipes.book.NucleosynthesizingBookCategory.NucleosynthesizingBookInfo;
import mekanism.api.recipes.book.OxidizingBookCategory;
import mekanism.api.recipes.book.OxidizingBookCategory.OxidizingBookInfo;
import mekanism.api.recipes.book.PaintingBookCategory;
import mekanism.api.recipes.book.PaintingBookCategory.PaintingBookInfo;
import mekanism.api.recipes.book.PigmentExtractingBookCategory;
import mekanism.api.recipes.book.PigmentExtractingBookCategory.PigmentExtractingBookInfo;
import mekanism.api.recipes.book.PigmentMixingBookCategory;
import mekanism.api.recipes.book.PigmentMixingBookCategory.PigmentMixingBookInfo;
import mekanism.api.recipes.book.PurifyingBookCategory;
import mekanism.api.recipes.book.PurifyingBookCategory.PurifyingBookInfo;
import mekanism.api.recipes.book.ReactingBookCategory;
import mekanism.api.recipes.book.ReactingBookCategory.ReactingBookInfo;
import mekanism.api.recipes.book.RotaryBookCategory;
import mekanism.api.recipes.book.RotaryBookCategory.RotaryBookInfo;
import mekanism.api.recipes.book.SawingBookCategory;
import mekanism.api.recipes.book.SawingBookCategory.SawingBookInfo;
import mekanism.api.recipes.book.SmeltingBookCategory;
import mekanism.api.recipes.book.SmeltingBookCategory.SmeltingBookInfo;
import mekanism.api.recipes.book.WashingBookCategory;
import mekanism.api.recipes.book.WashingBookCategory.WashingBookInfo;
import mekanism.api.recipes.vanilla_input.BiChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.BiItemRecipeInput;
import mekanism.api.recipes.vanilla_input.ReactionRecipeInput;
import mekanism.api.recipes.vanilla_input.RotaryRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleFluidChemicalRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleFluidRecipeInput;
import mekanism.api.recipes.vanilla_input.SingleItemChemicalRecipeInput;
import mekanism.client.MekanismClient;
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
import mekanism.common.registration.impl.RecipeBookCategoryDeferredRegister.MekanismRecipeBookTypes;
import mekanism.common.registration.impl.RecipeTypeDeferredRegister;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import mekanism.common.registries.MekanismRecipeBookCategories;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jspecify.annotations.Nullable;

public class MekanismRecipeType<VANILLA_INPUT extends RecipeInput, BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
      RECIPE extends MekanismRecipe<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO>, INPUT_CACHE extends IInputRecipeCache>
      implements RecipeType<RECIPE>, IMekanismRecipeTypeProvider<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO, RECIPE, INPUT_CACHE> {

    public static final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister(Mekanism.MODID);

    public static final RecipeTypeRegistryObject<SingleRecipeInput, CrushingBookCategory, CrushingBookInfo, ItemStackToItemStackRecipe<CrushingBookCategory, CrushingBookInfo>, SingleItem<ItemStackToItemStackRecipe<CrushingBookCategory, CrushingBookInfo>>> CRUSHING =
          register(MekanismRecipeTypes.NAME_CRUSHING, MekanismRecipeBookCategories.CRUSHING, recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, EnrichingBookCategory, EnrichingBookInfo, ItemStackToItemStackRecipe<EnrichingBookCategory, EnrichingBookInfo>, SingleItem<ItemStackToItemStackRecipe<EnrichingBookCategory, EnrichingBookInfo>>> ENRICHING =
          register(MekanismRecipeTypes.NAME_ENRICHING, MekanismRecipeBookCategories.ENRICHING, recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, SmeltingBookCategory, SmeltingBookInfo, ItemStackToItemStackRecipe<SmeltingBookCategory, SmeltingBookInfo>, SingleItem<ItemStackToItemStackRecipe<SmeltingBookCategory, SmeltingBookInfo>>> SMELTING =
          register(MekanismRecipeTypes.NAME_SMELTING, MekanismRecipeBookCategories.SMELTING, recipeType -> new SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput));

    public static final RecipeTypeRegistryObject<BiChemicalRecipeInput, ChemicalInfusingBookCategory, ChemicalInfusingBookInfo, ChemicalChemicalToChemicalRecipe<ChemicalInfusingBookCategory, ChemicalInfusingBookInfo>, EitherSideChemical<ChemicalChemicalToChemicalRecipe<ChemicalInfusingBookCategory, ChemicalInfusingBookInfo>>> CHEMICAL_INFUSING =
          register(MekanismRecipeTypes.NAME_CHEMICAL_INFUSING, MekanismRecipeBookCategories.CHEMICAL_INFUSING, EitherSideChemical::new);

    public static final RecipeTypeRegistryObject<BiItemRecipeInput, CombiningBookCategory, CombiningBookInfo, CombinerRecipe, DoubleItem<CombinerRecipe>> COMBINING =
          register(MekanismRecipeTypes.NAME_COMBINING, MekanismRecipeBookCategories.COMBINING, recipeType -> new DoubleItem<>(recipeType, CombinerRecipe::getMainInput, CombinerRecipe::getExtraInput));

    public static final RecipeTypeRegistryObject<SingleFluidRecipeInput, ElectrolysisBookCategory, ElectrolysisBookInfo, ElectrolysisRecipe, SingleFluid<ElectrolysisRecipe>> SEPARATING =
          register(MekanismRecipeTypes.NAME_SEPARATING, MekanismRecipeBookCategories.SEPARATING, recipeType -> new SingleFluid<>(recipeType, ElectrolysisRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleFluidChemicalRecipeInput, WashingBookCategory, WashingBookInfo, FluidChemicalToChemicalRecipe<WashingBookCategory, WashingBookInfo>, FluidChemical<FluidChemicalToChemicalRecipe<WashingBookCategory, WashingBookInfo>>> WASHING =
          register(MekanismRecipeTypes.NAME_WASHING, MekanismRecipeBookCategories.WASHING, recipeType -> new FluidChemical<>(recipeType, FluidChemicalToChemicalRecipe::getFluidInput, FluidChemicalToChemicalRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleFluidRecipeInput, EvaporatingBookCategory, EvaporatingBookInfo, FluidToFluidRecipe<EvaporatingBookCategory, EvaporatingBookInfo>, SingleFluid<FluidToFluidRecipe<EvaporatingBookCategory, EvaporatingBookInfo>>> EVAPORATING =
          register(MekanismRecipeTypes.NAME_EVAPORATING, MekanismRecipeBookCategories.EVAPORATING, recipeType -> new SingleFluid<>(recipeType, FluidToFluidRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleChemicalRecipeInput, ActivatingBookCategory, ActivatingBookInfo, ChemicalToChemicalRecipe<ActivatingBookCategory, ActivatingBookInfo>, SingleChemical<ChemicalToChemicalRecipe<ActivatingBookCategory, ActivatingBookInfo>>> ACTIVATING =
          register(MekanismRecipeTypes.NAME_ACTIVATING, MekanismRecipeBookCategories.ACTIVATING, recipeType -> new SingleChemical<>(recipeType, ChemicalToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleChemicalRecipeInput, CentrifugingBookCategory, CentrifugingBookInfo, ChemicalToChemicalRecipe<CentrifugingBookCategory, CentrifugingBookInfo>, SingleChemical<ChemicalToChemicalRecipe<CentrifugingBookCategory, CentrifugingBookInfo>>> CENTRIFUGING =
          register(MekanismRecipeTypes.NAME_CENTRIFUGING, MekanismRecipeBookCategories.CENTRIFUGING, recipeType -> new SingleChemical<>(recipeType, ChemicalToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleChemicalRecipeInput, CrystallizingBookCategory, CrystallizingBookInfo, ChemicalCrystallizerRecipe, SingleChemical<ChemicalCrystallizerRecipe>> CRYSTALLIZING =
          register(MekanismRecipeTypes.NAME_CRYSTALLIZING, MekanismRecipeBookCategories.CRYSTALLIZING, recipeType -> new SingleChemical<>(recipeType, ChemicalCrystallizerRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, DissolutionBookCategory, DissolutionBookInfo, ChemicalDissolutionRecipe, ItemChemical<ChemicalDissolutionRecipe>> DISSOLUTION =
          register(MekanismRecipeTypes.NAME_DISSOLUTION, MekanismRecipeBookCategories.DISSOLUTION, recipeType -> new ItemChemical<>(recipeType, ChemicalDissolutionRecipe::getItemInput, ChemicalDissolutionRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, CompressingBookCategory, CompressingBookInfo, ItemStackChemicalToItemStackRecipe<CompressingBookCategory, CompressingBookInfo>, ItemChemical<ItemStackChemicalToItemStackRecipe<CompressingBookCategory, CompressingBookInfo>>> COMPRESSING =
          register(MekanismRecipeTypes.NAME_COMPRESSING, MekanismRecipeBookCategories.COMPRESSING, recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, PurifyingBookCategory, PurifyingBookInfo, ItemStackChemicalToItemStackRecipe<PurifyingBookCategory, PurifyingBookInfo>, ItemChemical<ItemStackChemicalToItemStackRecipe<PurifyingBookCategory, PurifyingBookInfo>>> PURIFYING =
          register(MekanismRecipeTypes.NAME_PURIFYING, MekanismRecipeBookCategories.PURIFYING, recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, InjectingBookCategory, InjectingBookInfo, ItemStackChemicalToItemStackRecipe<InjectingBookCategory, InjectingBookInfo>, ItemChemical<ItemStackChemicalToItemStackRecipe<InjectingBookCategory, InjectingBookInfo>>> INJECTING =
          register(MekanismRecipeTypes.NAME_INJECTING, MekanismRecipeBookCategories.INJECTING, recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, NucleosynthesizingBookCategory, NucleosynthesizingBookInfo, NucleosynthesizingRecipe, ItemChemical<NucleosynthesizingRecipe>> NUCLEOSYNTHESIZING =
          register(MekanismRecipeTypes.NAME_NUCLEOSYNTHESIZING, MekanismRecipeBookCategories.NUCLEOSYNTHESIZING, recipeType -> new ItemChemical<>(recipeType, NucleosynthesizingRecipe::getItemInput, NucleosynthesizingRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, EnergyConversionBookCategory, EnergyConversionBookInfo, ItemStackToEnergyRecipe, SingleItem<ItemStackToEnergyRecipe>> ENERGY_CONVERSION =
          register(MekanismRecipeTypes.NAME_ENERGY_CONVERSION, MekanismRecipeBookCategories.ENERGY_CONVERSION, recipeType -> new SingleItem<>(recipeType, ItemStackToEnergyRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, ChemicalConversionBookCategory, ChemicalConversionBookInfo, ItemStackToChemicalRecipe<ChemicalConversionBookCategory, ChemicalConversionBookInfo>, SingleItem<ItemStackToChemicalRecipe<ChemicalConversionBookCategory, ChemicalConversionBookInfo>>> CHEMICAL_CONVERSION =
          register(MekanismRecipeTypes.NAME_CHEMICAL_CONVERSION, MekanismRecipeBookCategories.CHEMICAL_CONVERSION, recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, OxidizingBookCategory, OxidizingBookInfo, ItemStackToChemicalRecipe<OxidizingBookCategory, OxidizingBookInfo>, SingleItem<ItemStackToChemicalRecipe<OxidizingBookCategory, OxidizingBookInfo>>> OXIDIZING =
          register(MekanismRecipeTypes.NAME_OXIDIZING, MekanismRecipeBookCategories.OXIDIZING, recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, PigmentExtractingBookCategory, PigmentExtractingBookInfo, ItemStackToChemicalRecipe<PigmentExtractingBookCategory, PigmentExtractingBookInfo>, SingleItem<ItemStackToChemicalRecipe<PigmentExtractingBookCategory, PigmentExtractingBookInfo>>> PIGMENT_EXTRACTING =
          register(MekanismRecipeTypes.NAME_PIGMENT_EXTRACTING, MekanismRecipeBookCategories.PIGMENT_EXTRACTING, recipeType -> new SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput));

    public static final RecipeTypeRegistryObject<BiChemicalRecipeInput, PigmentMixingBookCategory, PigmentMixingBookInfo, ChemicalChemicalToChemicalRecipe<PigmentMixingBookCategory, PigmentMixingBookInfo>, EitherSideChemical<ChemicalChemicalToChemicalRecipe<PigmentMixingBookCategory, PigmentMixingBookInfo>>> PIGMENT_MIXING =
          register(MekanismRecipeTypes.NAME_PIGMENT_MIXING, MekanismRecipeBookCategories.PIGMENT_MIXING, EitherSideChemical::new);

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, MetallurgicInfusingBookCategory, MetallurgicInfusingBookInfo, ItemStackChemicalToItemStackRecipe<MetallurgicInfusingBookCategory, MetallurgicInfusingBookInfo>, ItemChemical<ItemStackChemicalToItemStackRecipe<MetallurgicInfusingBookCategory, MetallurgicInfusingBookInfo>>> METALLURGIC_INFUSING =
          register(MekanismRecipeTypes.NAME_METALLURGIC_INFUSING, MekanismRecipeBookCategories.METALLURGIC_INFUSING, recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<SingleItemChemicalRecipeInput, PaintingBookCategory, PaintingBookInfo, ItemStackChemicalToItemStackRecipe<PaintingBookCategory, PaintingBookInfo>, ItemChemical<ItemStackChemicalToItemStackRecipe<PaintingBookCategory, PaintingBookInfo>>> PAINTING =
          register(MekanismRecipeTypes.NAME_PAINTING, MekanismRecipeBookCategories.PAINTING, recipeType -> new ItemChemical<>(recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput));

    public static final RecipeTypeRegistryObject<ReactionRecipeInput, ReactingBookCategory, ReactingBookInfo, PressurizedReactionRecipe, ItemFluidChemical<PressurizedReactionRecipe>> REACTION =
          register(MekanismRecipeTypes.NAME_REACTION, MekanismRecipeBookCategories.REACTION, recipeType -> new ItemFluidChemical<>(recipeType, PressurizedReactionRecipe::getInputSolid, PressurizedReactionRecipe::getInputFluid, PressurizedReactionRecipe::getInputChemical));

    public static final RecipeTypeRegistryObject<RotaryRecipeInput, RotaryBookCategory, RotaryBookInfo, RotaryRecipe, RotaryInputRecipeCache> ROTARY = register(MekanismRecipeTypes.NAME_ROTARY, MekanismRecipeBookCategories.ROTARY, RotaryInputRecipeCache::new);

    public static final RecipeTypeRegistryObject<SingleRecipeInput, SawingBookCategory, SawingBookInfo, SawmillRecipe, SingleItem<SawmillRecipe>> SAWING =
          register(MekanismRecipeTypes.NAME_SAWING, MekanismRecipeBookCategories.SAWING, recipeType -> new SingleItem<>(recipeType, SawmillRecipe::getInput));

    public static final RecipeTypeRegistryObject<SingleRecipeInput, LiquificationBookCategory, LiquificationBookInfo, ItemStackToFluidOptionalItemRecipe<LiquificationBookCategory, LiquificationBookInfo>, SingleItem<ItemStackToFluidOptionalItemRecipe<LiquificationBookCategory, LiquificationBookInfo>>> LIQUIFICATION =
          register(MekanismRecipeTypes.NAME_LIQUIFICATION, MekanismRecipeBookCategories.LIQUIFICATION, recipeType -> new SingleItem<>(recipeType, ItemStackToFluidOptionalItemRecipe::getInput));

    private static <VANILLA_INPUT extends RecipeInput, BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>,
          RECIPE extends MekanismRecipe<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO>, INPUT_CACHE extends IInputRecipeCache>
    RecipeTypeRegistryObject<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO, RECIPE, INPUT_CACHE> register(
          Identifier name, MekanismRecipeBookTypes recipeBookTypes,
          Function<MekanismRecipeType<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO, RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator
    ) {
        if (!Mekanism.MODID.equals(name.getNamespace())) {
            throw new IllegalStateException("Name must be in " + Mekanism.MODID + " namespace");
        }
        return RECIPE_TYPES.registerMek(name.getPath(), registryName -> new MekanismRecipeType<>(registryName, recipeBookTypes, inputCacheCreator));
    }

    public static void clearCache() {
        for (Holder<RecipeType<?>> entry : RECIPE_TYPES.getEntries()) {
            //Note: We expect all entries to be a MekanismRecipeType, but we validate it just to be sure
            if (entry.value() instanceof MekanismRecipeType<?, ?, ?, ?, ?> recipeType) {
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
            MekanismRecipeType<?, ?, ?, ?, ?> recipeType = (MekanismRecipeType<?, ?, ?, ?, ?>) holder.value();
            foundIncompleteRecipes |= recipeType.checkMyIncompleteRecipes(recipeManager);
        }
        return foundIncompleteRecipes;
    }

    private final MekanismRecipeBookTypes bookCategories;
    private final RecipeBookType recipeBookType;
    private final Identifier registryName;
    private final INPUT_CACHE inputCache;

    private List<RecipeHolder<RECIPE>> cachedRecipes = Collections.emptyList();

    private MekanismRecipeType(Identifier name, MekanismRecipeBookTypes bookCategories,
          Function<MekanismRecipeType<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO, RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
        this.registryName = name;
        this.inputCache = inputCacheCreator.apply(this);
        this.bookCategories = bookCategories;
        String bookName = name.getNamespace() + "_" + name.getPath();
        this.recipeBookType = RecipeBookType.valueOf(bookName.toUpperCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    @Override
    public Identifier getRegistryName() {
        return registryName;
    }

    @Override
    public MekanismRecipeType<VANILLA_INPUT, BOOK_CATEGORY, BOOK_INFO, RECIPE, INPUT_CACHE> getRecipeType() {
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

    @Override
    public List<RecipeHolder<RECIPE>> getRecipes(@Nullable Level world) {
        RecipeMap recipeMap = getRecipeMap(world);
        if (recipeMap == RecipeMap.EMPTY) {
            //If we failed, then return no recipes
            return Collections.emptyList();
        }
        return getRecipes(recipeMap);
    }

    /// {@return the corresponding recipe book type}
    public RecipeBookType getRecipeBookType() {
        return recipeBookType;
    }

    /// {@return the corresponding recipe book categories}
    public MekanismRecipeBookTypes getBookCategories() {
        return bookCategories;
    }

    @Override
    public List<RecipeHolder<RECIPE>> getRecipes(RecipeMap recipeMap) {
        if (cachedRecipes.isEmpty()) {
            //Note: This is a fresh immutable list that gets returned
            Collection<RecipeHolder<RECIPE>> recipes = getRecipesUncached(recipeMap);
            //Make the list of cached recipes immutable and filter out any incomplete recipes
            // as there is no reason to potentially look the partial complete piece up if
            // the other portion of the recipe is incomplete
            cachedRecipes = recipes.stream()
                  .filter(recipe -> !recipe.value().placementInfo().isImpossibleToPlace())
                  .toList();
        }
        return cachedRecipes;
    }

    /// Get a list of recipes directly from the manager
    ///
    /// @param recipeMap The recipes map
    private Collection<RecipeHolder<RECIPE>> getRecipesUncached(RecipeMap recipeMap) {
        Collection<RecipeHolder<RECIPE>> recipes = recipeMap.byType(this);
        if (this == SMELTING.get()) {
            //Ensure the recipes can be modified
            recipes = new ArrayList<>(recipes);
            for (RecipeHolder<SmeltingRecipe> recipeHolder : recipeMap.byType(RecipeType.SMELTING)) {
                SmeltingRecipe smeltingRecipe = recipeHolder.value();
                if (smeltingRecipe.input().isEmpty()) {
                    continue;
                }
                ItemStackToItemStackRecipe<SmeltingBookCategory, SmeltingBookInfo> possiblyUnwrapped = WrappedSmelterRecipe.tryUnwrap(smeltingRecipe);
                ResourceKey<Recipe<?>> generateId = RegistryUtils.syntheticKey(recipeHolder.id(), "mekanism_generated");
                recipes.add(new RecipeHolder<>(generateId, castRecipe(possiblyUnwrapped)));

            }
        }
        return recipes;
    }

    @SuppressWarnings("unchecked")
    private RECIPE castRecipe(MekanismRecipe<?, ?, ?> o) {
        if (o.getType() != this) {
            throw new IllegalArgumentException("Wrong recipe type");
        }
        return (RECIPE) o;
    }

    private boolean checkMyIncompleteRecipes(RecipeManager recipeManager) {
        boolean incomplete = false;
        for (RecipeHolder<RECIPE> holder : getRecipesUncached(recipeManager.recipeMap())) {
            if (holder.value().placementInfo().isImpossibleToPlace()) {
                Mekanism.logger.error("Incomplete recipe detected: {}", holder.id());
                incomplete = true;
                holder.value().logMissingTags();
            }
        }
        return incomplete;
    }

    private static RecipeMap getRecipeMap(@Nullable Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.recipeAccess().recipeMap();
        } else if (level != null) {
            if (level.isClientSide()) {
                return MekanismClient.clientRecipes();
            }
        }
        //No level, try to look it up
        else if (FMLEnvironment.getDist().isClient()) {
            return MekanismClient.clientRecipes();
        } else {
            MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
            if (currentServer != null) {
                return currentServer.getRecipeManager().recipeMap();
            }
        }
        return RecipeMap.EMPTY;
    }

    /// Helper for getting a recipe from a world's recipe manager.
    public static <I extends RecipeInput, RECIPE_TYPE extends Recipe<I>> Optional<RecipeHolder<RECIPE_TYPE>> getRecipeFor(RecipeType<RECIPE_TYPE> recipeType, I input,
          Level level) {
        //Only allow looking up complete recipes or special recipes as we only use this method for vanilla recipe types
        // and special recipes return that they are not complete
        return getRecipeMap(level).getRecipesFor(recipeType, input, level).findFirst()
              /*.filter(recipe -> recipe.value().isSpecial() || !recipe.value().isIncomplete())*/;
    }

    /// Helper for getting a recipe from a world's recipe manager.
    @Nullable
    public static RecipeHolder<?> byKey(Level level, ResourceKey<Recipe<?>> id) {
        //Only allow looking up complete recipes or special recipes as we only use this method for vanilla recipe types
        // and special recipes return that they are not complete
        return getRecipeMap(level).byKey(id)
              /*.filter(recipe -> recipe.value().isSpecial() || !recipe.value().isIncomplete())*/;
    }
}