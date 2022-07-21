package mekanism.common.tile.factory;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipe.OperationTracker.RecipeError;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FactoryInputInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.monitor.FactoryRecipeCacheLookupMonitor;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.MachineUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityFactory<RECIPE extends MekanismRecipe> extends TileEntityConfigurableMachine implements IRecipeLookupHandler<RECIPE>, ISustainedData {

    /**
     * How many ticks it takes, by default, to run an operation.
     */
    protected static final int BASE_TICKS_REQUIRED = 200;

    protected FactoryRecipeCacheLookupMonitor<RECIPE>[] recipeCacheLookupMonitors;
    protected BooleanSupplier[] recheckAllRecipeErrors;
    protected final ErrorTracker errorTracker;
    private final boolean[] activeStates;
    protected ProcessInfo[] processInfoSlots;
    /**
     * This Factory's tier.
     */
    public FactoryTier tier;
    /**
     * An int[] used to track all current operations' progress.
     */
    public final int[] progress;
    /**
     * How many ticks it takes, with upgrades, to run an operation
     */
    private int ticksRequired = 200;
    private boolean sorting;
    private boolean sortingNeeded = true;
    private FloatingLong lastUsage = FloatingLong.ZERO;

    /**
     * This machine's factory type.
     */
    @NotNull
    protected final FactoryType type;

    protected MachineEnergyContainer<TileEntityFactory<?>> energyContainer;
    protected final List<IInventorySlot> inputSlots;
    protected final List<IInventorySlot> outputSlots;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    protected EnergyInventorySlot energySlot;

    protected TileEntityFactory(IBlockProvider blockProvider, BlockPos pos, BlockState state, List<RecipeError> errorTypes, Set<RecipeError> globalErrorTypes) {
        super(blockProvider, pos, state);
        type = Attribute.get(blockProvider, AttributeFactoryType.class).getFactoryType();
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
        inputSlots = new ArrayList<>();
        outputSlots = new ArrayList<>();

        for (ProcessInfo info : processInfoSlots) {
            inputSlots.add(info.inputSlot());
            outputSlots.add(info.outputSlot());
            if (info.secondaryOutputSlot() != null) {
                outputSlots.add(info.secondaryOutputSlot());
            }
        }
        configComponent.setupItemIOConfig(inputSlots, outputSlots, energySlot, false);
        IInventorySlot extraSlot = getExtraSlot();
        if (extraSlot != null) {
            ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
            if (itemConfig != null) {
                itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, extraSlot));
                itemConfig.setDataType(DataType.EXTRA, RelativeSide.BOTTOM);
            }
        }
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        progress = new int[tier.processes];
        activeStates = new boolean[tier.processes];
        recheckAllRecipeErrors = new BooleanSupplier[tier.processes];
        for (int i = 0; i < recheckAllRecipeErrors.length; i++) {
            //Note: We store one per slot so that we can recheck the different slots at different times to reduce the load on the server
            recheckAllRecipeErrors[i] = TileEntityRecipeMachine.shouldRecheckAllErrors(this);
        }
        errorTracker = new ErrorTracker(errorTypes, globalErrorTypes, tier.processes);
    }

    /**
     * Used for slots/contents pertaining to the inventory checks to mark sorting as being needed again and recipes as needing to be rechecked. This combines with the
     * passed in listener to allow for abstracting the comparator type checks up to the base level.
     */
    protected IContentsListener markAllMonitorsChanged(IContentsListener listener) {
        return () -> {
            listener.onContentsChanged();
            //Note: Updating sorting is handled by the onChange calls
            for (FactoryRecipeCacheLookupMonitor<RECIPE> cacheLookupMonitor : recipeCacheLookupMonitors) {
                cacheLookupMonitor.onChange();
            }
        };
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        tier = Attribute.getTier(getBlockType(), FactoryTier.class);
        Runnable setSortingNeeded = () -> sortingNeeded = true;
        recipeCacheLookupMonitors = new FactoryRecipeCacheLookupMonitor[tier.processes];
        for (int i = 0; i < recipeCacheLookupMonitors.length; i++) {
            recipeCacheLookupMonitors[i] = new FactoryRecipeCacheLookupMonitor<>(this, i, setSortingNeeded);
        }
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        addSlots(builder, listener, () -> {
            listener.onContentsChanged();
            //Mark sorting as being needed again
            sortingNeeded = true;
        });
        //Add the energy slot after adding the other slots so that it has the lowest priority in shift clicking
        //Note: We can just pass ourselves as the listener instead of the listener that updates sorting as well,
        // as changes to it won't change anything about the sorting of the recipe
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 7, 13));
        return builder.build();
    }

    protected abstract void addSlots(InventorySlotHelper builder, IContentsListener listener, IContentsListener updateSortingListener);

    @Nullable
    protected IInventorySlot getExtraSlot() {
        return null;
    }

    public FactoryType getFactoryType() {
        return type;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();

        handleSecondaryFuel();
        if (sortingNeeded && isSorting()) {
            //If sorting is needed, and we have sorting enabled mark
            // sorting as no longer needed and sort the inventory
            sortingNeeded = false;
            // Note: If sorting happens, sorting will be marked as needed once more
            // (due to changes in the inventory), but this is fine, and we purposely
            // mark sorting being needed as false before instead of after this method
            // call, because while it tries to optimize the layout, if the optimization
            // would make it so that some slots are now empty (because of stacked inputs
            // being required), we want to make sure we are able to fill those slots
            // with other items.
            sortInventory();
        } else if (!sortingNeeded && CommonWorldTickHandler.flushTagAndRecipeCaches) {
            //Otherwise, if sorting isn't currently needed and the recipe cache is invalid
            // Mark sorting as being needed again for the next check as recipes may
            // have changed so our current sort may be incorrect
            sortingNeeded = true;
        }

        //Copy this so that if it changes we still have the original amount. Don't bother making it a constant though as this way
        // we can then use minusEqual instead of subtract to remove an extra copy call
        FloatingLong prev = energyContainer.getEnergy().copy();
        for (int i = 0; i < recipeCacheLookupMonitors.length; i++) {
            if (!recipeCacheLookupMonitors[i].updateAndProcess()) {
                //If we don't have a recipe in that slot make sure that our active state for that position is false
                activeStates[i] = false;
            }
        }

        //Update the active state based on the current active state of each recipe
        boolean isActive = false;
        for (boolean state : activeStates) {
            if (state) {
                isActive = true;
                break;
            }
        }
        setActive(isActive);
        //If none of the recipes are actively processing don't bother with any subtraction
        lastUsage = isActive ? prev.minusEqual(energyContainer.getEnergy()) : FloatingLong.ZERO;
    }

    /**
     * Checks if the cached recipe (or recipe for current factory if the cache is out of date) can produce a specific output.
     *
     * @param process             Which process the cache recipe is.
     * @param fallbackInput       Used if the cached recipe is null or to validate the cached recipe is not out of date.
     * @param outputSlot          The output slot for this slot.
     * @param secondaryOutputSlot The secondary output slot or null if we only have one output slot
     * @param updateCache         True to make the cached recipe get updated if it is out of date.
     *
     * @return True if the recipe produces the given output.
     */
    public boolean inputProducesOutput(int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot,
          boolean updateCache) {
        return outputSlot.isEmpty() || getRecipeForInput(process, fallbackInput, outputSlot, secondaryOutputSlot, updateCache) != null;
    }

    protected abstract boolean isCachedRecipeValid(@Nullable CachedRecipe<RECIPE> cached, @NotNull ItemStack stack);

    @Nullable
    protected RECIPE getRecipeForInput(int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot,
          boolean updateCache) {
        if (!CommonWorldTickHandler.flushTagAndRecipeCaches) {
            //If our recipe caches are valid, grab our cached recipe and see if it is still valid
            CachedRecipe<RECIPE> cached = getCachedRecipe(process);
            if (cached != null && isCachedRecipeValid(cached, fallbackInput)) {
                //Our input matches the recipe we have cached for this slot
                return cached.getRecipe();
            }
        }
        //If there is no cached item input, or it doesn't match our fallback then it is an out of date cache, so we ignore the fact that we have a cache
        RECIPE foundRecipe = findRecipe(process, fallbackInput, outputSlot, secondaryOutputSlot);
        if (foundRecipe == null) {
            //We could not find any valid recipe for the given item that matches the items in the current output slots
            return null;
        }
        if (updateCache) {
            //If we want to update the cache, then create a new cache with the recipe we found and update the cache
            recipeCacheLookupMonitors[process].updateCachedRecipe(foundRecipe);
        }
        return foundRecipe;
    }

    @Nullable
    protected abstract RECIPE findRecipe(int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot,
          @Nullable IInventorySlot secondaryOutputSlot);

    protected abstract int getNeededInput(RECIPE recipe, ItemStack inputStack);

    @Nullable
    private CachedRecipe<RECIPE> getCachedRecipe(int cacheIndex) {
        //TODO: Sanitize that cacheIndex is in bounds?
        return recipeCacheLookupMonitors[cacheIndex].getCachedRecipe(cacheIndex);
    }

    public BooleanSupplier getWarningCheck(RecipeError error, int processIndex) {
        return errorTracker.getWarningCheck(error, processIndex);
    }

    @Override
    public void clearRecipeErrors(int cacheIndex) {
        Arrays.fill(errorTracker.trackedErrors[cacheIndex], false);
    }

    protected void setActiveState(boolean state, int cacheIndex) {
        activeStates[cacheIndex] = state;
    }

    /**
     * Handles filling the secondary fuel tank based on the item in the extra slot
     */
    protected void handleSecondaryFuel() {
    }

    /**
     * Like isItemValidForSlot makes no assumptions about current stored types
     */
    public abstract boolean isValidInputItem(@NotNull ItemStack stack);

    public int getProgress(int cacheIndex) {
        return progress[cacheIndex];
    }

    @Override
    public int getSavedOperatingTicks(int cacheIndex) {
        return getProgress(cacheIndex);
    }

    public double getScaledProgress(int i, int process) {
        return (double) getProgress(process) * i / ticksRequired;
    }

    public void toggleSorting() {
        sorting = !isSorting();
        markForSave();
    }

    @ComputerMethod(nameOverride = "isAutoSortEnabled")
    public boolean isSorting() {
        return sorting;
    }

    @NotNull
    @ComputerMethod(nameOverride = "getEnergyUsage")
    public FloatingLong getLastUsage() {
        return lastUsage;
    }

    @ComputerMethod
    public int getTicksRequired() {
        return ticksRequired;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains(NBTConstants.PROGRESS, Tag.TAG_INT_ARRAY)) {
            int[] savedProgress = nbt.getIntArray(NBTConstants.PROGRESS);
            if (tier.processes != savedProgress.length) {
                Arrays.fill(progress, 0);
            }
            for (int i = 0; i < tier.processes && i < savedProgress.length; i++) {
                progress[i] = savedProgress[i];
            }
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        nbtTags.put(NBTConstants.PROGRESS, new IntArrayTag(Arrays.copyOf(progress, progress.length)));
    }

    @Override
    public void writeSustainedData(CompoundTag data) {
        data.putBoolean(NBTConstants.SORTING, isSorting());
    }

    @Override
    public void readSustainedData(CompoundTag data) {
        NBTUtils.setBooleanIfPresent(data, NBTConstants.SORTING, value -> sorting = value);
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.SORTING, NBTConstants.SORTING);
        return remap;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        }
    }

    @NotNull
    @Override
    public List<Component> getInfo(@NotNull Upgrade upgrade) {
        return UpgradeUtils.getMultScaledInfo(this, upgrade);
    }

    @Override
    public boolean isConfigurationDataCompatible(BlockEntityType<?> tileType) {
        if (super.isConfigurationDataCompatible(tileType)) {
            //Check exact match first
            return true;
        }
        //Then check other factory tiers
        for (FactoryTier factoryTier : EnumUtils.FACTORY_TIERS) {
            if (factoryTier != tier && MekanismTileEntityTypes.getFactoryTile(factoryTier, type).get() == tileType) {
                return true;
            }
        }
        //And finally check if it is the non factory version (it will be missing sorting data, but we can gracefully ignore that)
        return type.getBaseMachine().getTileType().get() == tileType;
    }

    public boolean hasSecondaryResourceBar() {
        return false;
    }

    public MachineEnergyContainer<TileEntityFactory<?>> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.trackArray(progress);
        errorTracker.track(container);
        container.track(SyncableFloatingLong.create(this::getLastUsage, value -> lastUsage = value));
        container.track(SyncableBoolean.create(this::isSorting, value -> sorting = value));
        container.track(SyncableInt.create(this::getTicksRequired, value -> ticksRequired = value));
    }

    @Override
    public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
        if (upgradeData instanceof MachineUpgradeData data) {
            redstone = data.redstone;
            setControlType(data.controlType);
            getEnergyContainer().setEnergy(data.energyContainer.getEnergy());
            sorting = data.sorting;
            energySlot.deserializeNBT(data.energySlot.serializeNBT());
            System.arraycopy(data.progress, 0, progress, 0, data.progress.length);
            for (int i = 0; i < data.inputSlots.size(); i++) {
                //Copy the stack using NBT so that if it is not actually valid due to a reload we don't crash
                inputSlots.get(i).deserializeNBT(data.inputSlots.get(i).serializeNBT());
            }
            for (int i = 0; i < data.outputSlots.size(); i++) {
                outputSlots.get(i).setStack(data.outputSlots.get(i).getStack());
            }
            for (ITileComponent component : getComponents()) {
                component.read(data.components);
            }
        } else {
            super.parseUpgradeData(upgradeData);
        }
    }

    //Methods relating to IComputerTile
    protected void validateValidProcess(int process) throws ComputerException {
        if (process < 0 || process >= progress.length) {
            throw new ComputerException("Process: '%d' is out of bounds, as this factory only has '%d' processes (zero indexed).", process, progress.length);
        }
    }

    @ComputerMethod
    private void setAutoSort(boolean enabled) throws ComputerException {
        validateSecurityIsPublic();
        if (sorting != enabled) {
            sorting = enabled;
            markForSave();
        }
    }

    @ComputerMethod
    private int getRecipeProgress(int process) throws ComputerException {
        validateValidProcess(process);
        return getProgress(process);
    }

    @ComputerMethod
    private ItemStack getInput(int process) throws ComputerException {
        validateValidProcess(process);
        return processInfoSlots[process].inputSlot().getStack();
    }

    @ComputerMethod
    private ItemStack getOutput(int process) throws ComputerException {
        validateValidProcess(process);
        return processInfoSlots[process].outputSlot().getStack();
    }
    //End methods IComputerTile

    private void sortInventory() {
        Map<HashedItem, RecipeProcessInfo> processes = new HashMap<>();
        List<ProcessInfo> emptyProcesses = new ArrayList<>();
        for (ProcessInfo processInfo : processInfoSlots) {
            IInventorySlot inputSlot = processInfo.inputSlot();
            if (inputSlot.isEmpty()) {
                emptyProcesses.add(processInfo);
            } else {
                ItemStack inputStack = inputSlot.getStack();
                HashedItem item = HashedItem.raw(inputStack);
                RecipeProcessInfo recipeProcessInfo = processes.computeIfAbsent(item, i -> new RecipeProcessInfo());
                recipeProcessInfo.processes.add(processInfo);
                recipeProcessInfo.totalCount += inputStack.getCount();
                if (recipeProcessInfo.lazyMinPerSlot == null && !CommonWorldTickHandler.flushTagAndRecipeCaches) {
                    //If we don't have a lazily initialized min per slot calculation set for it yet
                    // and our cache is not invalid/out of date due to a reload
                    CachedRecipe<RECIPE> cachedRecipe = getCachedRecipe(processInfo.process());
                    if (isCachedRecipeValid(cachedRecipe, inputStack)) {
                        // And our current process has a cached recipe then set the lazily initialized per slot value
                        // Note: If something goes wrong, and we end up with zero as how much we need as an input
                        // we just bump the value up to one to make sure we properly handle it
                        recipeProcessInfo.lazyMinPerSlot = () -> Math.max(1, getNeededInput(cachedRecipe.getRecipe(), inputStack));
                    }
                }
            }
        }
        if (processes.isEmpty()) {
            //If all input slots are empty, just exit
            return;
        }
        for (Entry<HashedItem, RecipeProcessInfo> entry : processes.entrySet()) {
            RecipeProcessInfo recipeProcessInfo = entry.getValue();
            if (recipeProcessInfo.lazyMinPerSlot == null) {
                //If we don't have a lazy initializer for our minPerSlot setup, that means that there is
                // no valid cached recipe for any of the slots of this type currently, so we want to try and
                // get the recipe we will have for the first slot, once we end up with more items in the stack
                recipeProcessInfo.lazyMinPerSlot = () -> {
                    //Note: We put all of this logic in the lazy init, so that we don't actually call any of this
                    // until it is needed. That way if we have no empty slots and all our input slots are filled
                    // we don't do any extra processing here, and can properly short circuit
                    HashedItem item = entry.getKey();
                    ItemStack largerInput = item.createStack(Math.min(item.getStack().getMaxStackSize(), recipeProcessInfo.totalCount));
                    ProcessInfo processInfo = recipeProcessInfo.processes.get(0);
                    //Try getting a recipe for our input with a larger size, and update the cache if we find one
                    RECIPE recipe = getRecipeForInput(processInfo.process(), largerInput, processInfo.outputSlot(), processInfo.secondaryOutputSlot(), true);
                    if (recipe != null) {
                        return Math.max(1, getNeededInput(recipe, largerInput));
                    }
                    return 1;
                };
            }
        }
        if (!emptyProcesses.isEmpty()) {
            //If we have any empty slots, we need to factor them in as valid slots for items to transferred to
            addEmptySlotsAsTargets(processes, emptyProcesses);
            //Note: Any remaining empty slots are "ignored" as we don't have any
            // spare items to distribute to them
        }
        //Distribute items among the slots
        distributeItems(processes);
    }

    private void addEmptySlotsAsTargets(Map<HashedItem, RecipeProcessInfo> processes, List<ProcessInfo> emptyProcesses) {
        for (Entry<HashedItem, RecipeProcessInfo> entry : processes.entrySet()) {
            RecipeProcessInfo recipeProcessInfo = entry.getValue();
            int minPerSlot = recipeProcessInfo.getMinPerSlot();
            int maxSlots = recipeProcessInfo.totalCount / minPerSlot;
            if (maxSlots <= 1) {
                //If we don't have enough to even fill the input for a slot for a single recipe; skip
                continue;
            }
            //Otherwise, if we have at least enough items for two slots see how many we already have with items in them
            int processCount = recipeProcessInfo.processes.size();
            if (maxSlots <= processCount) {
                //If we don't have enough extra to fill another slot skip
                continue;
            }
            //Note: This is some arbitrary input stack one of the stacks contained
            ItemStack sourceStack = entry.getKey().getStack();
            int emptyToAdd = maxSlots - processCount;
            int added = 0;
            List<ProcessInfo> toRemove = new ArrayList<>();
            for (ProcessInfo emptyProcess : emptyProcesses) {
                if (inputProducesOutput(emptyProcess.process(), sourceStack, emptyProcess.outputSlot(), emptyProcess.secondaryOutputSlot(), true)) {
                    //If the input is valid for the stuff in the empty process' output slot
                    // then add our empty process to our recipeProcessInfo, and mark
                    // the empty process as accounted for
                    recipeProcessInfo.processes.add(emptyProcess);
                    toRemove.add(emptyProcess);
                    added++;
                    if (added >= emptyToAdd) {
                        //If we added as many as we could based on how much input we have; exit
                        break;
                    }
                }
            }
            emptyProcesses.removeAll(toRemove);
            if (emptyProcesses.isEmpty()) {
                //We accounted for all our empty processes, stop looking at inputs
                // for purposes of distributing empty slots among them
                break;
            }
        }
    }

    private void distributeItems(Map<HashedItem, RecipeProcessInfo> processes) {
        for (Entry<HashedItem, RecipeProcessInfo> entry : processes.entrySet()) {
            RecipeProcessInfo recipeProcessInfo = entry.getValue();
            int processCount = recipeProcessInfo.processes.size();
            if (processCount == 1) {
                //If there is only one process with the item in it; short-circuit, no balancing is needed
                continue;
            }
            HashedItem item = entry.getKey();
            //Note: This isn't based on any limits the slot may have (but we currently don't have any reduced ones here, so it doesn't matter)
            int maxStackSize = item.getStack().getMaxStackSize();
            int numberPerSlot = recipeProcessInfo.totalCount / processCount;
            if (numberPerSlot == maxStackSize) {
                //If all the slots are already maxed out; short-circuit, no balancing is needed
                continue;
            }
            int remainder = recipeProcessInfo.totalCount % processCount;
            int minPerSlot = recipeProcessInfo.getMinPerSlot();
            if (minPerSlot > 1) {
                int perSlotRemainder = numberPerSlot % minPerSlot;
                if (perSlotRemainder > 0) {
                    //Reduce the number we distribute per slot by what our excess
                    // is if we are trying to balance it by the size of the input
                    // required by the recipe
                    numberPerSlot -= perSlotRemainder;
                    // and then add how many items we removed to our remainder
                    remainder += perSlotRemainder * processCount;
                    // Note: After this processing the remainder is at most:
                    // processCount - 1 + processCount * (minPerSlot - 1) =
                    // processCount - 1 + processCount * minPerSlot - processCount =
                    // processCount * minPerSlot - 1
                    // Which means that reducing the remainder by minPerSlot for each
                    // slot while we still have a remainder, will make sure
                }
                if (numberPerSlot + minPerSlot > maxStackSize) {
                    //If adding how much we want per slot would cause the slot to overflow
                    // we reduce how much we set per slot to how much there is room for
                    // Note: we can do this safely because while our remainder may be
                    // processCount * minPerSlot - 1 (as shown above), if we are in
                    // this if statement, that means that we really have at most:
                    // processCount * maxStackSize - 1 items being distributed and
                    // have: processCount * numberPerSlot + remainder
                    // which means that our remainder is actually at most:
                    // processCount * (maxStackSize - numberPerSlot) - 1
                    // so we can safely set our per slot distribution to maxStackSize - numberPerSlot
                    minPerSlot = maxStackSize - numberPerSlot;
                }
            }
            for (int i = 0; i < processCount; i++) {
                ProcessInfo processInfo = recipeProcessInfo.processes.get(i);
                FactoryInputInventorySlot inputSlot = processInfo.inputSlot();
                int sizeForSlot = numberPerSlot;
                if (remainder > 0) {
                    //If we have a remainder, factor it into our slots
                    if (remainder > minPerSlot) {
                        //If our remainder is greater than how much we need to fill out the min amount for the slot based
                        // on the recipe then, to keep it distributed as evenly as possible, increase our size for the slot
                        // by how much we need, and decrease our remainder by that amount
                        sizeForSlot += minPerSlot;
                        remainder -= minPerSlot;
                    } else {
                        //Otherwise, add our entire remainder to the size for slot, and mark our remainder as fully used
                        sizeForSlot += remainder;
                        remainder = 0;
                    }
                }
                if (inputSlot.isEmpty()) {
                    //Note: sizeForSlot should never be zero here as we would not have added
                    // the empty slot to this item's distribution grouping if it would not
                    // end up getting any items; check it just in case though before creating
                    // a stack for the slot and setting it
                    if (sizeForSlot > 0) {
                        //Note: We use setStackUnchecked here, as there is a very small chance that
                        // the stack is not actually valid for the slot because of a reload causing
                        // recipes to change. If this is the case, then we want to properly not crash,
                        // but we would rather not add any extra overhead about revalidating the item
                        // each time as it can get somewhat expensive.
                        inputSlot.setStackUnchecked(item.createStack(sizeForSlot));
                    }
                } else {
                    //Slot is not currently empty
                    if (sizeForSlot == 0) {
                        //If the amount of the item we want to set it to is zero (all got used by earlier stacks, which might
                        // happen if the recipe requires a stacked input (minPerSlot > 1)), then we need to set the slot to empty
                        inputSlot.setEmpty();
                    } else if (inputSlot.getCount() != sizeForSlot) {
                        //Otherwise, if our slot doesn't already contain the amount we want it to,
                        // we need to adjust how much is stored in it, and log an error if it changed
                        // by a different amount then we expected
                        //Note: We use setStackSize here rather than setStack to avoid an unnecessary stack copy call
                        // as copying item stacks can sometimes be rather expensive in a heavily modded environment
                        MekanismUtils.logMismatchedStackSize(sizeForSlot, inputSlot.setStackSize(sizeForSlot, Action.EXECUTE));
                    }
                }
            }
        }
    }

    public record ProcessInfo(int process, @NotNull FactoryInputInventorySlot inputSlot, @NotNull IInventorySlot outputSlot,
                              @Nullable IInventorySlot secondaryOutputSlot) {
    }

    private static class RecipeProcessInfo {

        private final List<ProcessInfo> processes = new ArrayList<>();
        @Nullable
        private IntSupplier lazyMinPerSlot;
        private int minPerSlot = 1;
        private int totalCount;

        public int getMinPerSlot() {
            if (lazyMinPerSlot != null) {
                //Get the value lazily
                minPerSlot = lazyMinPerSlot.getAsInt();
                lazyMinPerSlot = null;
            }
            return minPerSlot;
        }
    }

    protected static class ErrorTracker {

        private final List<RecipeError> errorTypes;
        private final IntSet globalTypes;

        //TODO: See if we can get it so we only have to sync a single version of global types?
        private final boolean[][] trackedErrors;
        private final int processes;

        public ErrorTracker(List<RecipeError> errorTypes, Set<RecipeError> globalErrorTypes, int processes) {
            //Copy the list if it is mutable to ensure it doesn't get changed, otherwise just use the list
            this.errorTypes = List.copyOf(errorTypes);
            globalTypes = new IntArraySet(globalErrorTypes.size());
            for (int i = 0; i < this.errorTypes.size(); i++) {
                RecipeError error = this.errorTypes.get(i);
                if (globalErrorTypes.contains(error)) {
                    globalTypes.add(i);
                }
            }
            this.processes = processes;
            trackedErrors = new boolean[this.processes][];
            int errors = this.errorTypes.size();
            for (int i = 0; i < trackedErrors.length; i++) {
                trackedErrors[i] = new boolean[errors];
            }
        }

        private void track(MekanismContainer container) {
            container.trackArray(trackedErrors);
        }

        public void onErrorsChanged(Set<RecipeError> errors, int processIndex) {
            boolean[] processTrackedErrors = trackedErrors[processIndex];
            for (int i = 0; i < processTrackedErrors.length; i++) {
                processTrackedErrors[i] = errors.contains(errorTypes.get(i));
            }
        }

        private BooleanSupplier getWarningCheck(RecipeError error, int processIndex) {
            if (processIndex >= 0 && processIndex < processes) {
                int errorIndex = errorTypes.indexOf(error);
                if (errorIndex >= 0) {
                    if (globalTypes.contains(errorIndex)) {
                        return () -> Arrays.stream(trackedErrors).anyMatch(processTrackedErrors -> processTrackedErrors[errorIndex]);
                    }
                    return () -> trackedErrors[processIndex][errorIndex];
                }
            }
            //Something went wrong
            return () -> false;
        }
    }
}