package mekanism.common.tile.factory;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.block.FactoryType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITileNetwork;
import mekanism.common.base.ProcessInfo;
import mekanism.common.block.machine.BlockFactory;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class TileEntityFactory<RECIPE extends MekanismRecipe> extends TileEntityMekanism implements ISideConfiguration, ISpecialConfigData, ITileNetwork,
      ITileCachedRecipeHolder<RECIPE> {

    private final CachedRecipe<RECIPE>[] cachedRecipes;
    private boolean[] activeStates;
    protected ProcessInfo[] processInfoSlots;
    /**
     * This Factory's tier.
     */
    public FactoryTier tier;
    /**
     * An int[] used to track all current operations' progress.
     */
    public int[] progress;
    /**
     * How many ticks it takes, by default, to run an operation.
     */
    public int BASE_TICKS_REQUIRED = 200;
    /**
     * How many ticks it takes, with upgrades, to run an operation
     */
    public int ticksRequired = 200;
    public boolean sorting;
    public double lastUsage;

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;
    /**
     * This machine's factory type.
     */
    @Nonnull
    protected FactoryType type;

    protected List<IInventorySlot> inputSlots;
    protected List<IInventorySlot> outputSlots;
    protected EnergyInventorySlot energySlot;

    protected TileEntityFactory(IBlockProvider blockProvider) {
        super(blockProvider);
        BlockFactory factoryBlock = (BlockFactory) blockProvider.getBlock();
        this.type = factoryBlock.getFactoryType();
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            inputSlots = new ArrayList<>();
            outputSlots = new ArrayList<>();

            for (ProcessInfo info : processInfoSlots) {
                inputSlots.add(info.getInputSlot());
                outputSlots.add(info.getOutputSlot());
                if (info.getSecondaryOutputSlot() != null) {
                    outputSlots.add(info.getSecondaryOutputSlot());
                }
            }
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, false, inputSlots));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(false, true, outputSlots));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.LEFT, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);
            itemConfig.setDataType(RelativeSide.BACK, DataType.ENERGY);

            IInventorySlot extraSlot = getExtraSlot();
            if (extraSlot != null) {
                itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, extraSlot));
                itemConfig.setDataType(RelativeSide.BOTTOM, DataType.EXTRA);
            }
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT, new EnergySlotInfo(true, false));
            energyConfig.fill(DataType.INPUT);
            energyConfig.setCanEject(false);
        }

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, itemConfig);

        progress = new int[tier.processes];
        //TODO: Theoretically this should work as it initializes them all as null, but is there a better/proper way to do this
        cachedRecipes = new CachedRecipe[tier.processes];
        activeStates = new boolean[cachedRecipes.length];
    }

    @Override
    protected void presetVariables() {
        tier = ((BlockFactory) getBlockType()).getTier();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        addSlots(builder);
        //Add the energy slot after adding the other slots so that it has lowest priority in shift clicking
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 7, 13));
        return builder.build();
    }

    protected abstract void addSlots(InventorySlotHelper builder);

    @Nullable
    protected IInventorySlot getExtraSlot() {
        return null;
    }

    @Override
    protected void onUpdateServer() {
        if (ticker == 1) {
            world.notifyNeighborsOfStateChange(getPos(), getBlockType());
        }
        energySlot.discharge(this);

        handleSecondaryFuel();
        sortInventory();

        double prev = getEnergy();
        for (int i = 0; i < cachedRecipes.length; i++) {
            CachedRecipe<RECIPE> cachedRecipe = cachedRecipes[i] = getUpdatedCache(i);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            } else {
                //If we don't have a recipe in that slot make sure that our active state for that position is false
                //TODO: Check if this is needed, it probably is already the case that if the cached recipe is null then
                // we should already have activeState as false
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
        lastUsage = prev - getEnergy();
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        ISlotInfo slotInfo = configComponent.getSlotInfo(TransmissionType.ENERGY, side);
        return slotInfo instanceof EnergySlotInfo && slotInfo.canInput();
    }

    private void sortInventory() {
        if (sorting) {
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
                    if (stack.isEmpty() && !inputProducesOutput(checkInfo.getProcess(), checkStack, primaryInfo.getOutputSlot(), primaryInfo.getSecondaryOutputSlot(), true) ||
                        checkStack.isEmpty() && !inputProducesOutput(primaryInfo.getProcess(), stack, checkInfo.getOutputSlot(), checkInfo.getSecondaryOutputSlot(), true)) {
                        continue;
                    }

                    //Balance the two slots
                    int total = count + checkStack.getCount();
                    ItemStack newStack = stack.isEmpty() ? checkStack : stack;
                    primaryInputSlot.setStack(StackUtils.size(newStack, (total + 1) / 2));
                    checkInputSlot.setStack(StackUtils.size(newStack, total / 2));
                    markDirty();
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
        if (isRemote()) {
            return progress[cacheIndex];
        }
        CachedRecipe<RECIPE> cachedRecipe = cachedRecipes[cacheIndex];
        if (cachedRecipe == null) {
            return 0;
        }
        return cachedRecipe.getOperatingTicks();
    }

    public double getScaledProgress(int i, int process) {
        return (double) getProgress(process) * i / (double) ticksRequired;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                sorting = !sorting;
            } else if (type == 1) {
                clearSecondaryTank();
            }
        }
    }

    protected void clearSecondaryTank() {
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        sorting = nbtTags.getBoolean(NBTConstants.SORTING);
        //TODO: Save/Load operating ticks properly given the variable is stored in the CachedRecipe
        for (int i = 0; i < tier.processes; i++) {
            progress[i] = nbtTags.getInt(NBTConstants.PROGRESS + i);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putBoolean(NBTConstants.SORTING, sorting);
        //TODO: Save/Load operating ticks properly given the variable is stored in the CachedRecipe
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        } else if (capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY) {
            return Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        return configComponent.isCapabilityDisabled(capability, side) || super.isCapabilityDisabled(capability, side);
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
        nbtTags.putBoolean(NBTConstants.SORTING, sorting);
        return nbtTags;
    }

    @Override
    public void setConfigurationData(CompoundNBT nbtTags) {
        sorting = nbtTags.getBoolean(NBTConstants.SORTING);
    }

    @Override
    public String getDataType() {
        return getName().getFormattedText();
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

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.trackArray(progress);
        container.track(SyncableDouble.create(() -> lastUsage, value -> lastUsage = value));
        container.track(SyncableBoolean.create(() -> sorting, value -> sorting = value));
        container.track(SyncableInt.create(() -> ticksRequired, value -> ticksRequired = value));
    }
}