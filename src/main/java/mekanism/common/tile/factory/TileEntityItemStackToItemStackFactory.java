package mekanism.common.tile.factory;

import java.util.List;
import java.util.Set;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.book.MekanismBookCategory;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.client.recipe_viewer.type.RVRecipeTypeWrapper;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.ItemRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.upgrade.MachineUpgradeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

//Smelting, enriching, crushing
public class TileEntityItemStackToItemStackFactory<BOOK_CATEGORY extends MekanismBookCategory, BOOK_INFO extends Recipe.BookInfo<BOOK_CATEGORY>>
      extends TileEntityItemToItemFactory<BOOK_CATEGORY, BOOK_INFO, ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>>
      implements ItemRecipeLookupHandler<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> {

    private static final TriPredicate<ItemStackToItemStackRecipe<?, ?>, ItemResource, ItemResource> CAN_OUTPUT_STACK =
          (recipe, input, outputContents) -> outputContents.isEmpty() || outputContents.matches(recipe.getOutput(input));
    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    private static final Set<RecipeError> GLOBAL_ERROR_TYPES = Set.of(RecipeError.NOT_ENOUGH_ENERGY);

    public TileEntityItemStackToItemStackFactory(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
    }

    @Override
    public boolean isItemValidForSlot(ItemResource itemType) {
        //contains recipe in general already validated by isValidInputItem
        return true;
    }

    @Override
    public boolean isValidInputItem(ItemResource itemType) {
        return containsRecipe(itemType);
    }

    @Override
    protected int getNeededInput(ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO> recipe, ItemResource inputType) {
        return recipe.getInput().getNeededAmount(inputType);
    }

    @Override
    protected boolean isCachedRecipeValid(@Nullable CachedRecipe<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> cached, ItemResource itemType) {
        return cached != null && cached.getRecipe().getInput().testType(itemType);
    }

    @Nullable
    @Override
    protected RecipeHolder<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> findRecipe(ItemResource fallbackInput, IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot) {
        return getRecipeType().getInputCache().findTypeBasedRecipe(level, fallbackInput, outputSlot.resource(), CAN_OUTPUT_STACK);
    }

    @Override
    public IMekanismRecipeTypeProvider<SingleRecipeInput, BOOK_CATEGORY, BOOK_INFO, ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>, SingleItem<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>>> getRecipeType() {
        return recipeViewerType().vanillaProvider();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RVRecipeTypeWrapper<SingleRecipeInput, BOOK_CATEGORY, BOOK_INFO, ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>,
          SingleItem<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>>> recipeViewerType() {
        return (RVRecipeTypeWrapper) switch (type) {
            case ENRICHING -> RecipeViewerRecipeType.ENRICHING;
            case CRUSHING -> RecipeViewerRecipeType.CRUSHING;
            //TODO: Make it so that it throws an error if it is not one of the three types
            default -> RecipeViewerRecipeType.SMELTING;
        };
    }

    @Nullable
    @Override
    public RecipeHolder<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandlers[cacheIndex]);
    }

    @Override
    public CachedRecipe<ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO>> createNewCachedRecipe(ItemStackToItemStackRecipe<BOOK_CATEGORY, BOOK_INFO> recipe, int cacheIndex) {
        return new OneInputCachedRecipe<>(recipe, recheckAllRecipeErrors[cacheIndex], inputHandlers[cacheIndex], outputHandlers[cacheIndex])
              .setErrorsChanged(errors -> errorTracker.onErrorsChanged(errors, cacheIndex))
              .setCanHolderFunction(this::canFunction)
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setBaselineMaxOperations(this::getOperationsPerTick);
    }

    @Override
    public MachineUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new MachineUpgradeData(provider, redstone, getControlType(), energyContainer, energySlot, recipeCacheLookupMonitors, inputSlots, outputSlots, isSorting(), getComponents(), problemPath());
    }
}