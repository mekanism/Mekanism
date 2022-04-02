package mekanism.common.tile.factory;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.SawmillRecipe.ChanceOutput;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
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
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.SawmillUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;

public class TileEntitySawingFactory extends TileEntityFactory<SawmillRecipe> implements ItemRecipeLookupHandler<SawmillRecipe> {

    private static final Map<RecipeError, Boolean> TRACKED_ERROR_TYPES = Map.of(
          RecipeError.NOT_ENOUGH_ENERGY, true,
          RecipeError.NOT_ENOUGH_INPUT, false,
          RecipeError.NOT_ENOUGH_OUTPUT_SPACE, false,
          TileEntityPrecisionSawmill.NOT_ENOUGH_SPACE_SECONDARY_OUTPUT_ERROR, false,
          RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT, false
    );

    protected IInputHandler<@NonNull ItemStack>[] inputHandlers;
    protected IOutputHandler<@NonNull ChanceOutput>[] outputHandlers;

    public TileEntitySawingFactory(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state, TRACKED_ERROR_TYPES);
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
            OutputInventorySlot outputSlot = OutputInventorySlot.at(updateSortingListener, xPos, 57);
            OutputInventorySlot secondaryOutputSlot = OutputInventorySlot.at(updateSortingListener, xPos, 77);
            //Note: As we are an item factory that has comparator's based on items we can just use the monitor as a listener directly
            FactoryInputInventorySlot inputSlot = FactoryInputInventorySlot.create(this, i, outputSlot, secondaryOutputSlot, recipeCacheLookupMonitors[i], xPos, 13);
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
    public boolean isValidInputItem(@Nonnull ItemStack stack) {
        return containsRecipe(stack);
    }

    @Override
    protected int getNeededInput(SawmillRecipe recipe, ItemStack inputStack) {
        return MathUtils.clampToInt(recipe.getInput().getNeededAmount(inputStack));
    }

    @Override
    protected boolean isCachedRecipeValid(@Nullable CachedRecipe<SawmillRecipe> cached, @Nonnull ItemStack stack) {
        return cached != null && cached.getRecipe().getInput().testType(stack);
    }

    @Override
    protected SawmillRecipe findRecipe(int process, @Nonnull ItemStack fallbackInput, @Nonnull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot) {
        ItemStack output = outputSlot.getStack();
        ItemStack extra = secondaryOutputSlot == null ? ItemStack.EMPTY : secondaryOutputSlot.getStack();
        return getRecipeType().getInputCache().findTypeBasedRecipe(level, fallbackInput, recipe -> {
            ChanceOutput chanceOutput = recipe.getOutput(fallbackInput);
            if (InventoryUtils.areItemsStackable(chanceOutput.getMainOutput(), output)) {
                //If the input is good and the primary output matches, make sure that the secondary
                // output of this recipe will stack with what is currently in the secondary slot
                if (extra.isEmpty()) {
                    return true;
                }
                ItemStack secondaryOutput = chanceOutput.getMaxSecondaryOutput();
                return secondaryOutput.isEmpty() || ItemHandlerHelper.canItemStacksStack(secondaryOutput, extra);
            }
            return false;
        });
    }

    @Nonnull
    @Override
    public IMekanismRecipeTypeProvider<SawmillRecipe, SingleItem<SawmillRecipe>> getRecipeType() {
        return MekanismRecipeType.SAWING;
    }

    @Nullable
    @Override
    public SawmillRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandlers[cacheIndex]);
    }

    @Nonnull
    @Override
    public CachedRecipe<SawmillRecipe> createNewCachedRecipe(@Nonnull SawmillRecipe recipe, int cacheIndex) {
        return OneInputCachedRecipe.sawing(recipe, recheckAllRecipeErrors[cacheIndex], inputHandlers[cacheIndex], outputHandlers[cacheIndex])
              .setErrorsChanged(errors -> errorTracker.onErrorsChanged(errors, cacheIndex))
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(active -> setActiveState(active, cacheIndex))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(this::getTicksRequired)
              .setOnFinish(this::markForSave)
              .setOperatingTicksChanged(operatingTicks -> progress[cacheIndex] = operatingTicks);
    }

    @Override
    public void parseUpgradeData(@Nonnull IUpgradeData upgradeData) {
        if (upgradeData instanceof SawmillUpgradeData) {
            //Validate we have the correct type of data before passing it upwards
            super.parseUpgradeData(upgradeData);
        } else {
            Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
        }
    }

    @Nonnull
    @Override
    public SawmillUpgradeData getUpgradeData() {
        return new SawmillUpgradeData(redstone, getControlType(), getEnergyContainer(), progress, energySlot, inputSlots, outputSlots, isSorting(), getComponents());
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private ItemStack getSecondaryOutput(int process) throws ComputerException {
        validateValidProcess(process);
        IInventorySlot secondaryOutputSlot = processInfoSlots[process].secondaryOutputSlot();
        //This should never be null, but in case it is, handle it
        return secondaryOutputSlot == null ? ItemStack.EMPTY : secondaryOutputSlot.getStack();
    }
    //End methods IComputerTile
}