package mekanism.common.tile.factory;

import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigCardAccess.ISpecialConfigData;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.block.FactoryType;
import mekanism.api.gas.GasTank;
import mekanism.api.infuse.InfusionTank;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.SideData;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFactory.MachineFuelType;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.base.ProcessInfo;
import mekanism.common.block.machine.factory.BlockFactory;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.inventory.IInventorySlotHolder;
import mekanism.common.inventory.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.TileEntityMetallurgicInfuser;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.StatUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class TileEntityFactory<RECIPE extends MekanismRecipe> extends TileEntityMekanism implements IComputerIntegration, ISideConfiguration, ISpecialConfigData,
      ITierUpgradeable, ISustainedData, IComparatorSupport, ITileCachedRecipeHolder<RECIPE> {

    public static final int ENERGY_SLOT_ID = 1;
    public static final int EXTRA_SLOT_ID = 4;

    private static final String[] methods = new String[]{"getEnergy", "getProgress", "facing", "canOperate", "getMaxEnergy", "getEnergyNeeded"};
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
    /**
     * How much secondary energy each operation consumes per tick
     */
    private double secondaryEnergyPerTick = 0;
    protected int secondaryEnergyThisTick;
    /**
     * How long it takes this factory to switch recipe types.
     */
    private static int RECIPE_TICKS_REQUIRED = 40;
    /**
     * How many recipe ticks have progressed.
     */
    private int recipeTicks;
    /**
     * The amount of infuse this machine has stored.
     */
    public final InfusionTank infusionTank;

    public final GasTank gasTank;

    public boolean sorting;

    public boolean upgraded;

    public double lastUsage;

    public TileComponentEjector ejectorComponent;
    public TileComponentConfig configComponent;
    /**
     * This machine's recipe type.
     */
    //TODO: Remove this and factor recipe specific things to their proper subclasses. Also move any factory type information to FactoryType
    @Nonnull
    @Deprecated
    private RecipeType recipeType;

    @Nonnull
    protected FactoryType type;

    private IInventorySlot typeInputSlot;
    private OutputInventorySlot typeOutputSlot;

    protected TileEntityFactory(IBlockProvider blockProvider) {
        super(blockProvider);
        BlockFactory factoryBlock = (BlockFactory) blockProvider.getBlock();
        this.type = factoryBlock.getFactoryType();
        //TODO: Do this better/potentially remove RecipeType all together
        // Ideally we would pass information for handling directly to the FactoryType, based on the slots and let it figure it all out
        recipeType = RecipeType.values()[type.ordinal()];

        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.GAS);

        configComponent.addOutput(TransmissionType.ITEM, new SideData("None", EnumColor.GRAY, InventoryUtils.EMPTY));

        int[] inputSlots;
        int[] outputSlots;
        switch (tier) {
            case ADVANCED:
                inputSlots = new int[]{5, 6, 7, 8, 9};
                outputSlots = new int[]{10, 11, 12, 13, 14};
                break;
            case ELITE:
                inputSlots = new int[]{5, 6, 7, 8, 9, 10, 11};
                outputSlots = new int[]{12, 13, 14, 15, 16, 17, 18};
                break;
            case ULTIMATE:
                inputSlots = new int[]{5, 6, 7, 8, 9, 10, 11, 12, 13};
                outputSlots = new int[]{14, 15, 16, 17, 18, 19, 20, 21, 22};
                break;
            case BASIC:
            default:
                inputSlots = new int[]{5, 6, 7};
                outputSlots = new int[]{8, 9, 10};
                break;
        }

        configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", EnumColor.DARK_RED, inputSlots));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", EnumColor.DARK_BLUE, outputSlots));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", EnumColor.DARK_GREEN, new int[]{ENERGY_SLOT_ID}));
        configComponent.addOutput(TransmissionType.ITEM, new SideData("Extra", EnumColor.PURPLE, new int[]{EXTRA_SLOT_ID}));
        configComponent.setConfig(TransmissionType.ITEM, new byte[]{4, 0, 0, 3, 1, 2});

        configComponent.addOutput(TransmissionType.GAS, new SideData("None", EnumColor.GRAY, InventoryUtils.EMPTY));
        configComponent.addOutput(TransmissionType.GAS, new SideData("Gas", EnumColor.DARK_RED, new int[]{0}));
        configComponent.fillConfig(TransmissionType.GAS, 1);
        configComponent.setCanEject(TransmissionType.GAS, false);

        configComponent.setInputConfig(TransmissionType.ENERGY);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));

        progress = new int[tier.processes];
        //TODO: Theoretically this should work as it initializes them all as null, but is there a better/proper way to do this
        cachedRecipes = new CachedRecipe[tier.processes];
        activeStates = new boolean[cachedRecipes.length];
        gasTank = new GasTank(TileEntityAdvancedElectricMachine.MAX_GAS * tier.processes);
        infusionTank = new InfusionTank(TileEntityMetallurgicInfuser.MAX_INFUSE * tier.processes);
        setRecipeType(recipeType);
    }

    @Override
    protected void setSupportedTypes(Block block) {
        super.setSupportedTypes(block);
        //TODO: Do this in a better way, but currently we need to hijack this to set our tier earlier
        this.tier = ((BlockFactory) block).getTier();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper.Builder builder = InventorySlotHelper.Builder.forSide(this::getDirection);
        addSlots(builder);
        return builder.build();
    }

    protected void addSlots(InventorySlotHelper.Builder builder) {
        //return configComponent.getOutput(TransmissionType.ITEM, side, getDirection()).availableSlots;
        //TODO: Some way to tie slots to a config component? So that we can filter by the config component?
        // This can probably be done by letting the configurations know the relative side information?
        builder.addSlot(EnergyInventorySlot.discharge(this, 7, 13));
        //TODO: Make these two slots not show up on the auto generation of gui
        //TODO: Make this input slot only accept other machines for factories
        builder.addSlot(typeInputSlot = InputInventorySlot.at(this, tier == FactoryTier.ULTIMATE ? 214 : 180, 75));
        builder.addSlot(typeOutputSlot = OutputInventorySlot.at(this, tier == FactoryTier.ULTIMATE ? 214 : 180, 112));
    }

    @Override
    public boolean upgrade(BaseTier upgradeTier) {
        int upgradeOrdinal = upgradeTier.ordinal();
        if (upgradeOrdinal != tier.ordinal() + 1 || upgradeOrdinal > EnumUtils.FACTORY_TIERS.length) {
            return false;
        }
        World world = getWorld();
        if (world == null) {
            return false;
        }

        //TODO: Upgrading remove this if and fix the block state setting. A bunch of the TileEntity stuff may be able to be moved to the block classes themselves
        if (true) {
            return false;
        }
        world.removeBlock(getPos(), false);
        //world.setBlockState(getPos(), MekanismBlocks.MachineBlock.getStateFromMeta(5 + tier.ordinal() + 1));

        //TODO: Make this copy the settings over, probably make a method TileEntityMekanism#copySettings(TileEntityMekanism other)
        /*TileEntityFactory factory = Objects.requireNonNull((TileEntityFactory) world.getTileEntity(getPos()));

        //Basic
        factory.facing = facing;
        factory.clientFacing = clientFacing;
        factory.ticker = ticker;
        factory.redstone = redstone;
        factory.redstoneLastTick = redstoneLastTick;
        factory.doAutoSync = doAutoSync;

        //Electric
        factory.electricityStored = electricityStored;

        //Factory
        //TODO: Copy this
        System.arraycopy(progress, 0, factory.progress, 0, tier.processes);

        factory.recipeTicks = recipeTicks;
        factory.isActive = isActive;
        //TODO: Transfer cached recipe
        //factory.prevEnergy = prevEnergy;
        factory.gasTank.setGas(gasTank.getGas());
        factory.sorting = sorting;
        factory.setControlType(getControlType());
        factory.upgradeComponent.readFrom(upgradeComponent);
        factory.ejectorComponent.readFrom(ejectorComponent);
        factory.configComponent.readFrom(configComponent);
        factory.ejectorComponent.setOutputData(TransmissionType.ITEM, factory.configComponent.getOutputs(TransmissionType.ITEM).get(2));
        factory.setRecipeType(recipeType);
        factory.upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
        factory.securityComponent.readFrom(securityComponent);
        factory.infuseStored.copyFrom(infuseStored);

        for (int i = 0; i < tier.processes + 5; i++) {
            factory.inventory.set(i, inventory.get(i));
        }

        for (int i = 0; i < tier.processes; i++) {
            int output = getOutputSlot(i);
            if (!inventory.get(output).isEmpty()) {
                int newOutput = 5 + factory.tier.processes + i;
                factory.inventory.set(newOutput, inventory.get(output));
            }
        }

        for (Upgrade upgrade : factory.upgradeComponent.getSupportedTypes()) {
            factory.recalculateUpgrades(upgrade);
        }

        factory.upgraded = true;
        factory.markDirty();
        Mekanism.packetHandler.sendUpdatePacket(factory);*/
        return true;
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            if (ticker == 1) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType());
            }
            ChargeUtils.discharge(ENERGY_SLOT_ID, this);

            handleSecondaryFuel();
            sortInventory();
            ItemStack typeInputStack = typeInputSlot.getStack();
            if (!typeInputStack.isEmpty() && typeOutputSlot.isEmpty()) {
                RecipeType toSet = null;

                for (RecipeType type : RecipeType.values()) {
                    if (ItemHandlerHelper.canItemStacksStack(typeInputStack, type.getStack())) {
                        toSet = type;
                        break;
                    }
                }
                if (toSet != null && recipeType != toSet) {
                    if (recipeTicks < RECIPE_TICKS_REQUIRED) {
                        recipeTicks++;
                    } else {
                        recipeTicks = 0;
                        ItemStack returnStack = getMachineStack();

                        upgradeComponent.write(ItemDataUtils.getDataMap(returnStack));
                        upgradeComponent.setSupported(Upgrade.GAS, toSet.fuelEnergyUpgrades());
                        upgradeComponent.read(ItemDataUtils.getDataMapIfPresentNN(typeInputStack));

                        typeInputSlot.setStack(ItemStack.EMPTY);
                        typeOutputSlot.setStack(returnStack);

                        setRecipeType(toSet);
                        gasTank.setEmpty();
                        secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);
                        world.notifyNeighborsOfStateChange(getPos(), getBlockType());
                        MekanismUtils.saveChunk(this);
                    }
                } else {
                    recipeTicks = 0;
                }
            } else {
                recipeTicks = 0;
            }

            //TODO: Factor this out as it is only needed by ItemStackGasToItemStack factories
            secondaryEnergyThisTick = recipeType.fuelEnergyUpgrades() ? StatUtils.inversePoisson(secondaryEnergyPerTick) : (int) Math.ceil(secondaryEnergyPerTick);

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
    }

    //TODO: Move references of checking type against infusion to instanceof the infusing factory
    @Nonnull
    public FactoryType getFactoryType() {
        return type;
    }

    public void setRecipeType(@Nonnull RecipeType type) {
        recipeType = Objects.requireNonNull(type);
        setMaxEnergy(getBaseStorage());
        setEnergyPerTick(getBaseUsage());
        upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
        secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);

        if (type.getFuelType() == MachineFuelType.CHANCE) {
            SideData data = configComponent.getOutputs(TransmissionType.ITEM).get(2);
            //Append the "extra" slot to the available slots
            //TODO: FIXME this won't work at all with the fact that it isn't just a singular extra slot now
            data.availableSlots = Arrays.copyOf(data.availableSlots, data.availableSlots.length + 1);
            data.availableSlots[data.availableSlots.length - 1] = EXTRA_SLOT_ID;
        }

        for (Upgrade upgrade : upgradeComponent.getSupportedTypes()) {
            recalculateUpgrades(upgrade);
        }
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return configComponent.hasSideForData(TransmissionType.ENERGY, getDirection(), 1, side);
    }

    public void sortInventory() {
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

    public double getSecondaryEnergyPerTick(RecipeType type) {
        return MekanismUtils.getSecondaryEnergyPerTickMean(this, type.getSecondaryEnergyPerTick());
    }

    /**
     * Handles filling the secondary fuel tank based on the item in the extra slot
     */
    protected void handleSecondaryFuel() {
    }

    public ItemStack getMachineStack() {
        return blockProvider.getItemStack();
    }

    /**
     * Like isItemValidForSlot makes no assumptions about current stored types
     */
    public abstract boolean isValidInputItem(@Nonnull ItemStack stack);

    /**
     * Like isItemValidForSlot makes no assumptions about current stored types
     */
    public abstract boolean isValidExtraItem(@Nonnull ItemStack stack);

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

    public int getScaledInfuseLevel(int i) {
        return infusionTank.getStored() * i / infusionTank.getCapacity();
    }

    public int getScaledRecipeProgress(int i) {
        return recipeTicks * i / RECIPE_TICKS_REQUIRED;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            int type = dataStream.readInt();
            if (type == 0) {
                sorting = !sorting;
            } else if (type == 1) {
                gasTank.setEmpty();
                infusionTank.setEmpty();
            }
            return;
        }

        super.handlePacketData(dataStream);

        if (isRemote()) {
            upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
            recipeTicks = dataStream.readInt();
            sorting = dataStream.readBoolean();
            upgraded = dataStream.readBoolean();
            lastUsage = dataStream.readDouble();
            for (int i = 0; i < tier.processes; i++) {
                progress[i] = dataStream.readInt();
            }
            TileUtils.readTankData(dataStream, infusionTank);
            TileUtils.readTankData(dataStream, gasTank);
            if (upgraded) {
                markDirty();
                MekanismUtils.updateBlock(getWorld(), getPos());
                upgraded = false;
            }
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        upgradeComponent.setSupported(Upgrade.GAS, recipeType.fuelEnergyUpgrades());
        recipeTicks = nbtTags.getInt("recipeTicks");
        sorting = nbtTags.getBoolean("sorting");
        infusionTank.read(nbtTags.getCompound("infuseStored"));
        //TODO: Save/Load operating ticks properly given the variable is stored in the CachedRecipe
        for (int i = 0; i < tier.processes; i++) {
            progress[i] = nbtTags.getInt("progress" + i);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("recipeTicks", recipeTicks);
        nbtTags.putBoolean("sorting", sorting);
        if (!infusionTank.isEmpty()) {
            nbtTags.put("infuseStored", infusionTank.write(new CompoundNBT()));
        }
        //TODO: Save/Load operating ticks properly given the variable is stored in the CachedRecipe
        for (int i = 0; i < tier.processes; i++) {
            nbtTags.putInt("progress" + i, getProgress(i));
        }
        return nbtTags;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(recipeTicks);
        data.add(sorting);
        data.add(upgraded);
        data.add(lastUsage);

        //TODO: Do this better
        int[] progressToSync = new int[progress.length];
        for (int i = 0; i < progress.length; i++) {
            progressToSync[i] = getProgress(i);
        }
        data.add(progressToSync);
        TileUtils.addTankData(data, infusionTank);
        TileUtils.addTankData(data, gasTank);
        upgraded = false;
        return data;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                if (arguments[0] == null) {
                    return new Object[]{"Please provide a target operation."};
                }
                if (!(arguments[0] instanceof Double) && !(arguments[0] instanceof Integer)) {
                    return new Object[]{"Invalid characters."};
                }
                if ((Integer) arguments[0] < 0 || (Integer) arguments[0] > progress.length) {
                    return new Object[]{"No such operation found."};
                }
                return new Object[]{getProgress((Integer) arguments[0])};
            case 2:
                return new Object[]{getDirection()};
            case 3:
                if (arguments[0] == null) {
                    return new Object[]{"Please provide a target operation."};
                }
                if (!(arguments[0] instanceof Double) && !(arguments[0] instanceof Integer)) {
                    return new Object[]{"Invalid characters."};
                }
                if ((Integer) arguments[0] < 0 || (Integer) arguments[0] > cachedRecipes.length) {
                    return new Object[]{"No such operation found."};
                }
                CachedRecipe<RECIPE> cachedRecipe = cachedRecipes[(Integer) arguments[0]];
                //TODO: Decide if we should try to get the cached recipe if it is null
                return new Object[]{cachedRecipe != null && cachedRecipe.canFunction()};
            case 4:
                return new Object[]{getMaxEnergy()};
            case 5:
                return new Object[]{getNeededEnergy()};
            default:
                throw new NoSuchMethodException();
        }
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.CONFIG_CARD_CAPABILITY) {
            return Capabilities.CONFIG_CARD_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY) {
            return Capabilities.SPECIAL_CONFIG_DATA_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (configComponent.isCapabilityDisabled(capability, side, getDirection())) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        switch (upgrade) {
            case ENERGY:
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage())); // incorporate speed upgrades
                break;
            case GAS:
                //TODO: Move gas upgrade to only be in specific factories? At least for calculations?
                secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);
                break;
            case SPEED:
                ticksRequired = MekanismUtils.getTicks(this, BASE_TICKS_REQUIRED);
                setEnergyPerTick(MekanismUtils.getEnergyPerTick(this, getBaseUsage()));
                secondaryEnergyPerTick = getSecondaryEnergyPerTick(recipeType);
                break;
            default:
                break;
        }
    }

    @Override
    public CompoundNBT getConfigurationData(CompoundNBT nbtTags) {
        nbtTags.putBoolean("sorting", sorting);
        return nbtTags;
    }

    @Override
    public void setConfigurationData(CompoundNBT nbtTags) {
        sorting = nbtTags.getBoolean("sorting");
    }

    @Override
    public String getDataType() {
        return getName().getFormattedText();
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        infusionTank.writeSustainedData(itemStack);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        infusionTank.readSustainedData(itemStack);
    }

    @Override
    public int getRedstoneLevel() {
        return ItemHandlerHelper.calcRedstoneFromInventory(this);
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

}