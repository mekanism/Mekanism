package mekanism.common.tile.factory;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.base.ProcessInfo;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.prefab.BlockFactoryMachine.BlockFactory;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public abstract class TileEntityFactory<RECIPE extends MekanismRecipe> extends TileEntityMekanism implements ISideConfiguration, ISpecialConfigData,
      ITileCachedRecipeHolder<RECIPE> {

    /**
     * How many ticks it takes, by default, to run an operation.
     */
    private static final int BASE_TICKS_REQUIRED = 200;

    private final CachedRecipe<RECIPE>[] cachedRecipes;
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
    public int ticksRequired = 200;
    protected boolean sorting;
    public FloatingLong lastUsage = FloatingLong.ZERO;

    public final TileComponentEjector ejectorComponent;
    public final TileComponentConfig configComponent;
    /**
     * This machine's factory type.
     */
    @Nonnull
    protected final FactoryType type;

    protected MachineEnergyContainer<TileEntityFactory<?>> energyContainer;
    protected final List<IInventorySlot> inputSlots;
    protected final List<IInventorySlot> outputSlots;
    protected EnergyInventorySlot energySlot;

    protected TileEntityFactory(IBlockProvider blockProvider) {
        super(blockProvider);
        BlockFactory<?> factoryBlock = (BlockFactory<?>) blockProvider.getBlock();
        type = Attribute.get(factoryBlock, AttributeFactoryType.class).getFactoryType();
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
        inputSlots = new ArrayList<>();
        outputSlots = new ArrayList<>();

        for (ProcessInfo info : processInfoSlots) {
            inputSlots.add(info.getInputSlot());
            outputSlots.add(info.getOutputSlot());
            if (info.getSecondaryOutputSlot() != null) {
                outputSlots.add(info.getSecondaryOutputSlot());
            }
        }
        configComponent.setupItemIOConfig(inputSlots, outputSlots, energySlot, false);
        IInventorySlot extraSlot = getExtraSlot();
        if (extraSlot != null) {
            ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, extraSlot));
            itemConfig.setDataType(DataType.EXTRA, RelativeSide.BOTTOM);
        }
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        progress = new int[tier.processes];
        cachedRecipes = new CachedRecipe[tier.processes];
        activeStates = new boolean[cachedRecipes.length];
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY, this));
    }

    @Override
    protected void presetVariables() {
        tier = Attribute.getTier(getBlockType(), FactoryTier.class);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        addSlots(builder);
        //Add the energy slot after adding the other slots so that it has lowest priority in shift clicking
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 7, 13));
        return builder.build();
    }

    protected abstract void addSlots(InventorySlotHelper builder);

    @Nullable
    protected IInventorySlot getExtraSlot() {
        return null;
    }

    public FactoryType getFactoryType() {
        return type;
    }

    @Override
    protected void onUpdateServer() {
        energySlot.fillContainerOrConvert();

        handleSecondaryFuel();
        sortInventory();

        FloatingLong prev = energyContainer.getEnergy();
        for (int i = 0; i < cachedRecipes.length; i++) {
            CachedRecipe<RECIPE> cachedRecipe = cachedRecipes[i] = getUpdatedCache(i);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            } else {
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
        lastUsage = prev.subtract(energyContainer.getEnergy());
    }

    private void sortInventory() {
        if (isSorting()) {
            for (int i = 0; i < processInfoSlots.length; i++) {
                ProcessInfo primaryInfo = processInfoSlots[i];
                IInventorySlot primaryInputSlot = primaryInfo.getInputSlot();
                ItemStack stack = primaryInputSlot.getStack();
                int count = stack.getCount();
                for (int j = i + 1; j < processInfoSlots.length; j++) {
                    ProcessInfo checkInfo = processInfoSlots[j];
                    IInventorySlot checkInputSlot = checkInfo.getInputSlot();

                    ItemStack checkStack = checkInputSlot.getStack();
                    if (Math.abs(count - checkStack.getCount()) < 2 || !InventoryUtils.areItemsStackable(stack, checkStack)) {
                        continue;
                    }
                    //Output/Input will not match; Only check if the input spot is empty otherwise assume it works
                    if (stack.isEmpty() && !inputProducesOutput(primaryInfo.getProcess(), checkStack, primaryInfo.getOutputSlot(), primaryInfo.getSecondaryOutputSlot(), true) ||
                        checkStack.isEmpty() && !inputProducesOutput(checkInfo.getProcess(), stack, checkInfo.getOutputSlot(), checkInfo.getSecondaryOutputSlot(), true)) {
                        continue;
                    }

                    //Balance the two slots
                    int total = count + checkStack.getCount();
                    ItemStack newStack = stack.isEmpty() ? checkStack : stack;
                    primaryInputSlot.setStack(StackUtils.size(newStack, (total + 1) / 2));
                    checkInputSlot.setStack(StackUtils.size(newStack, total / 2));
                    markDirty(false);
                    return;
                }
            }
        }
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
    public abstract boolean inputProducesOutput(int process, @Nonnull ItemStack fallbackInput, @Nonnull IInventorySlot outputSlot,
          @Nullable IInventorySlot secondaryOutputSlot, boolean updateCache);

    @Nullable
    @Override
    public CachedRecipe<RECIPE> getCachedRecipe(int cacheIndex) {
        //TODO: Sanitize that cacheIndex is in bounds?
        return cachedRecipes[cacheIndex];
    }

    protected void updateCachedRecipe(@Nonnull CachedRecipe<RECIPE> newCache, int cacheIndex) {
        //TODO: Sanitize that cacheIndex is in bounds?
        cachedRecipes[cacheIndex] = newCache;
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
    public abstract boolean isValidInputItem(@Nonnull ItemStack stack);

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
        markDirty(false);
    }

    public boolean isSorting() {
        return sorting;
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        sorting = nbtTags.getBoolean(NBTConstants.SORTING);
        for (int i = 0; i < tier.processes; i++) {
            progress[i] = nbtTags.getInt(NBTConstants.PROGRESS + i);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.SORTING, isSorting());
        for (int i = 0; i < tier.processes; i++) {
            nbtTags.putInt(NBTConstants.PROGRESS + i, getProgress(i));
        }
        return nbtTags;
    }

    @Override
    public TileComponentConfig getConfig() {
        return configComponent;
    }

    @Override
    public Direction getOrientation() {
        return getDirection();
    }

    @Override
    public TileComponentEjector getEjector() {
        return ejectorComponent;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED) {
            ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
        }
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        nbtTags.putBoolean(NBTConstants.SORTING, isSorting());
        return nbtTags;
    }

    @Override
    public void setConfigurationData(CompoundNBT nbtTags) {
        sorting = nbtTags.getBoolean(NBTConstants.SORTING);
    }

    @Override
    public String getDataType() {
        return getName().getString();
    }

    public boolean hasSecondaryResourceBar() {
        return false;
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    public MachineEnergyContainer<TileEntityFactory<?>> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.trackArray(progress);
        container.track(SyncableFloatingLong.create(() -> lastUsage, value -> lastUsage = value));
        container.track(SyncableBoolean.create(this::isSorting, value -> sorting = value));
        container.track(SyncableInt.create(() -> ticksRequired, value -> ticksRequired = value));
    }
}