package mekanism.common.tile.factory;

import java.util.List;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.slot.FactoryInputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker.WarningType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.ItemRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleItem;
import mekanism.common.recipe.lookup.cache.SingleInputRecipeCache.CheckRecipeType;
import mekanism.common.recipe.lookup.monitor.FactoryRecipeCacheLookupMonitor;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.SawmillUpgradeData;
import mekanism.common.util.InventoryUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntitySawingFactory extends TileEntityFactory<SawmillRecipe> implements ItemRecipeLookupHandler<SawmillRecipe> {

    private static final CheckRecipeType<ItemStack, SawmillRecipe, ItemStack, ItemStack> OUTPUT_CHECK = (recipe, input, output, extra) -> {
        ChanceOutput chanceOutput = recipe.getOutput(input);
        if (InventoryUtils.areItemsStackable(chanceOutput.getMainOutput(), output)) {
            //If the input is good and the primary output matches, make sure that the secondary
            // output of this recipe will stack with what is currently in the secondary slot
            if (extra.isEmpty()) {
                return true;
            }
            ItemStack secondaryOutput = chanceOutput.getMaxSecondaryOutput();
            return secondaryOutput.isEmpty() || ItemStack.isSameItemSameComponents(secondaryOutput, extra);
        }
        return false;
    };
    private static final List<RecipeError> TRACKED_ERROR_TYPES = List.of(
          RecipeError.NOT_ENOUGH_ENERGY,
          RecipeError.NOT_ENOUGH_INPUT,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
          TileEntityPrecisionSawmill.NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
    );
    private static final Set<RecipeError> GLOBAL_ERROR_TYPES = Set.of(RecipeError.NOT_ENOUGH_ENERGY);

    protected IInputHandler<@NotNull ItemStack>[] inputHandlers;
    protected IOutputHandler<@NotNull ChanceOutput>[] outputHandlers;

    public TileEntitySawingFactory(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
    }

    @Override
    protected void addSlots(InventorySlotHelper builder, IContentsListener listener, IContentsListener updateSortingListener) {
        inputHandlers = new IInputHandler[tier.processes];
        outputHandlers = new IOutputHandler[tier.processes];
        processInfoSlots = new ProcessInfo[tier.processes];
        int baseX = tier == FactoryTier.BASIC ? 55 : tier == FactoryTier.ADVANCED ? 35 : tier == FactoryTier.ELITE ? 29 : 27;
        int baseXMult = tier == FactoryTier.BASIC ? 38 : tier == FactoryTier.ADVANCED ? 26 : 19;
        for (int i = 0; i < tier.processes; i++) {
            int xPos = baseX + (i * baseXMult);
            FactoryRecipeCacheLookupMonitor<SawmillRecipe> lookupMonitor = recipeCacheLookupMonitors[i];
            IContentsListener updateSortingAndUnpause = () -> {
                updateSortingListener.onContentsChanged();
                lookupMonitor.unpause();
            };
            OutputInventorySlot outputSlot = OutputInventorySlot.at(updateSortingAndUnpause, xPos, 57);
            OutputInventorySlot secondaryOutputSlot = OutputInventorySlot.at(updateSortingAndUnpause, xPos, 77);
            //Note: As we are an item factory that has comparator's based on items we can just use the monitor as a listener directly
            FactoryInputInventorySlot inputSlot = FactoryInputInventorySlot.create(this, i, outputSlot, secondaryOutputSlot, lookupMonitor, xPos, 13);
            int index = i;
            builder.addSlot(inputSlot).tracksWarnings(slot -> slot.warning(WarningType.NO_MATCHING_RECIPE, getWarningCheck(RecipeError.NOT_ENOUGH_INPUT, index)));
            builder.addSlot(outputSlot).tracksWarnings(slot -> slot.warning(WarningType.NO_SPACE_IN_OUTPUT, getWarningCheck(RecipeError.NOT_ENOUGH_OUTPUT_SPACE, index)));
            builder.addSlot(secondaryOutputSlot).tracksWarnings(slot -> slot.warning(WarningType.NO_SPACE_IN_OUTPUT,
                  getWarningCheck(TileEntityPrecisionSawmill.NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR, index)));
            inputHandlers[i] = InputHelper.getInputHandler(inputSlot, RecipeError.NOT_ENOUGH_INPUT);
            outputHandlers[i] = OutputHelper.getOutputHandler(outputSlot, RecipeError.NOT_ENOUGH_OUTPUT_SPACE, secondaryOutputSlot,
                  TileEntityPrecisionSawmill.NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR);
            processInfoSlots[i] = new ProcessInfo(i, inputSlot, outputSlot, secondaryOutputSlot);
        }
    }

    @Override
    public boolean isItemValidForSlot(@NotNull ItemStack stack) {
        //contains recipe in general already validated by isValidInputItem
        return true;
    }

    @Override
    public boolean isValidInputItem(@NotNull ItemStack stack) {
        return containsRecipe(stack);
    }

    @Override
    protected int getNeededInput(SawmillRecipe recipe, ItemStack inputStack) {
        return MathUtils.clampToInt(recipe.getInput().getNeededAmount(inputStack));
    }

    @Override
    protected boolean isCachedRecipeValid(@Nullable CachedRecipe<SawmillRecipe> cached, @NotNull ItemStack stack) {
        return cached != null && cached.getRecipe().getInput().testType(stack);
    }

    @Override
    protected SawmillRecipe findRecipe(int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot) {
        ItemStack extra = secondaryOutputSlot == null ? ItemStack.EMPTY : secondaryOutputSlot.getStack();
        return getRecipeType().getInputCache().findTypeBasedRecipe(level, fallbackInput, outputSlot.getStack(), extra, OUTPUT_CHECK);
    }

    @NotNull
    @Override
    public IMekanismRecipeTypeProvider<SingleRecipeInput, SawmillRecipe, SingleItem<SawmillRecipe>> getRecipeType() {
        return MekanismRecipeType.SAWING;
    }

    @Override
    public IRecipeViewerRecipeType<SawmillRecipe> recipeViewerType() {
        return RecipeViewerRecipeType.SAWING;
    }

    @Nullable
    @Override
    public SawmillRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandlers[cacheIndex]);
    }

    @NotNull
    @Override
    public CachedRecipe<SawmillRecipe> createNewCachedRecipe(@NotNull SawmillRecipe recipe, int cacheIndex) {
        return OneInputCachedRecipe.sawing(recipe, recheckAllRecipeErrors[cacheIndex], inputHandlers[cacheIndex], outputHandlers[cacheIndex])
              .setErrorsChanged(errors -> errorTracker.onErrorsChanged(errors, cacheIndex))
              .setCanHolderFunction(this::canFunction)
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks)
              .setBaselineMaxOperations(this::getOperationsPerTick);
    }

    @Override
    public void parseUpgradeData(HolderLookup.Provider provider, @NotNull IUpgradeData upgradeData) {
        if (upgradeData instanceof SawmillUpgradeData) {
            //Validate we have the correct type of data before passing it upwards
            super.parseUpgradeData(provider, upgradeData);
        } else {
            Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
        }
    }

    @NotNull
    @Override
    public SawmillUpgradeData getUpgradeData(HolderLookup.Provider provider) {
        return new SawmillUpgradeData(provider, redstone, getControlType(), getEnergyContainer(), progress, energySlot, inputSlots, outputSlots, isSorting(), getComponents());
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    ItemStack getSecondaryOutput(int process) throws ComputerException {
        validateValidProcess(process);
        IInventorySlot secondaryOutputSlot = processInfoSlots[process].secondaryOutputSlot();
        //This should never be null, but in case it is, handle it
        return secondaryOutputSlot == null ? ItemStack.EMPTY : secondaryOutputSlot.getStack();
    }
    //End methods IComputerTile
}