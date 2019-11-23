package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackGasToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.sustained.ISustainedData;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.recipe.GasConversionHandler;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.StatUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public abstract class TileEntityAdvancedElectricMachine extends TileEntityUpgradeableMachine<ItemStackGasToItemStackRecipe> implements IGasHandler, ISustainedData {

    private static final String[] methods = new String[]{"getEnergy", "getSecondaryStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy",
                                                         "getEnergyNeeded"};
    public static final int BASE_TICKS_REQUIRED = 200;
    public static final int BASE_GAS_PER_TICK = 1;
    public static int MAX_GAS = 210;
    /**
     * How much secondary energy (fuel) this machine uses per tick, not including upgrades.
     */
    public int BASE_SECONDARY_ENERGY_PER_TICK;
    /**
     * How much secondary energy this machine uses per tick, including upgrades.
     */
    public double secondaryEnergyPerTick;
    private int gasUsageThisTick;
    public GasTank gasTank;

    protected final IOutputHandler<@NonNull ItemStack> outputHandler;
    protected final IInputHandler<@NonNull ItemStack> itemInputHandler;
    protected final IInputHandler<@NonNull GasStack> gasInputHandler;

    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private GasInventorySlot secondarySlot;
    private EnergyInventorySlot energySlot;

    /**
     * Advanced Electric Machine -- a machine like this has a total of 4 slots. Input slot (0), fuel slot (1), output slot (2), energy slot (3), and the upgrade slot (4).
     * The machine will not run if it does not have enough electricity, or if it doesn't have enough fuel ticks.
     *
     * @param ticksRequired    - how many ticks it takes to smelt an item.
     * @param secondaryPerTick - how much secondary energy (fuel) this machine uses per tick.
     */
    public TileEntityAdvancedElectricMachine(IBlockProvider blockProvider, int ticksRequired, int secondaryPerTick) {
        super(blockProvider, ticksRequired, MekanismUtils.getResource(ResourceType.GUI, "advanced_machine.png"));
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(inputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(outputSlot));
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(secondarySlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(energySlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.TOP, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);
            itemConfig.setDataType(RelativeSide.BOTTOM, DataType.EXTRA);
            itemConfig.setDataType(RelativeSide.BACK, DataType.ENERGY);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT, new EnergySlotInfo());
            energyConfig.fill(DataType.INPUT);
            energyConfig.setCanEject(false);
        }

        BASE_SECONDARY_ENERGY_PER_TICK = secondaryPerTick;
        secondaryEnergyPerTick = secondaryPerTick;

        if (upgradeableSecondaryEfficiency()) {
            upgradeComponent.setSupported(Upgrade.GAS);
        }
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, itemConfig);

        itemInputHandler = InputHelper.getInputHandler(inputSlot);
        gasInputHandler = InputHelper.getInputHandler(gasTank);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Override
    protected void presetVariables() {
        gasTank = new GasTank(MAX_GAS);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getItemInput().testType(item)), this, 56, 17));
        builder.addSlot(secondarySlot = GasInventorySlot.fillOrConvert(gasTank, this::isValidGas, this, 56, 53));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 116, 35));
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 31, 35));
        return builder.build();
    }

    @Override
    protected void upgradeInventory(TileEntityFactory factory) {
        //TODO: Upgrade
        //Advanced Machine
        /*if (factory instanceof TileEntityItemStackGasToItemStackFactory) {
            ((TileEntityItemStackGasToItemStackFactory) factory).gasTank.setStack(gasTank.getStack());
        }

        NonNullList<ItemStack> factoryInventory = factory.getInventory();
        NonNullList<ItemStack> inventory = getInventory();
        factoryInventory.set(5, inventory.get(0));
        factoryInventory.set(4, inventory.get(1));
        factoryInventory.set(5 + 3, inventory.get(2));
        factoryInventory.set(1, inventory.get(3));
        factoryInventory.set(0, inventory.get(4));*/
    }

    /**
     * Gets the amount of ticks the declared itemstack can fuel this machine.
     *
     * @param itemStack - itemstack to check with
     *
     * @return fuel ticks
     */
    @Nonnull
    public GasStack getItemGas(ItemStack itemStack) {
        return GasConversionHandler.getItemGas(itemStack, gasTank, this::isValidGas);
    }

    public boolean isValidGas(@Nonnull Gas gas) {
        return containsRecipe(recipe -> recipe.getGasInput().testType(gas));
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            handleSecondaryFuel();
            //TODO: Is there some better way to do this rather than storing it and then doing it like this?
            // TODO: Also evaluate if there is a better way of doing the secondary calculation when not using statistical mechanics
            gasUsageThisTick = useStatisticalMechanics() ? StatUtils.inversePoisson(secondaryEnergyPerTick) : (int) Math.ceil(secondaryEnergyPerTick);
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
        }
    }

    public void handleSecondaryFuel() {
        //TODO: Move this stuff into the slot itself because of getStack not supposed to being modified
        ItemStack itemStack = secondarySlot.getStack();
        int needed = gasTank.getNeeded();
        if (!itemStack.isEmpty() && needed > 0) {
            GasStack gasStack = getItemGas(itemStack);
            if (needed >= gasStack.getAmount()) {
                if (itemStack.getItem() instanceof IGasItem) {
                    IGasItem item = (IGasItem) itemStack.getItem();
                    gasTank.fill(item.removeGas(itemStack, gasStack.getAmount()), Action.EXECUTE);
                } else {
                    gasTank.fill(gasStack, Action.EXECUTE);
                    if (secondarySlot.shrinkStack(1, Action.EXECUTE) != 1) {
                        //TODO: Print error that something went wrong
                    }
                }
            }
        }
    }

    public boolean upgradeableSecondaryEfficiency() {
        return false;
    }

    public boolean useStatisticalMechanics() {
        return false;
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackGasToItemStackRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ItemStackGasToItemStackRecipe getRecipe(int cacheIndex) {
        ItemStack stack = itemInputHandler.getInput();
        if (stack.isEmpty()) {
            return null;
        }
        GasStack gasStack = gasInputHandler.getInput();
        if (gasStack.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(stack, gasStack));
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackGasToItemStackRecipe> createNewCachedRecipe(@Nonnull ItemStackGasToItemStackRecipe recipe, int cacheIndex) {
        return new ItemStackGasToItemStackCachedRecipe(recipe, itemInputHandler, gasInputHandler, () -> gasUsageThisTick, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            TileUtils.readTankData(dataStream, gasTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        TileUtils.addTankData(data, gasTank);
        return data;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        gasTank.read(nbtTags.getCompound("gasTank"));
        gasTank.setCapacity(MAX_GAS);
        GasUtils.clearIfInvalid(gasTank, this::isValidGas);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.put("gasTank", gasTank.write(new CompoundNBT()));
        return nbtTags;
    }

    /**
     * Gets the scaled secondary energy level for the GUI.
     *
     * @param i - multiplier
     *
     * @return scaled secondary energy
     */
    public int getScaledGasLevel(int i) {
        return gasTank.getStored() * i / gasTank.getCapacity();
    }

    @Override
    public int receiveGas(Direction side, @Nonnull GasStack stack, Action action) {
        if (canReceiveGas(side, stack.getType())) {
            return gasTank.fill(stack, action);
        }
        return 0;
    }

    @Nonnull
    @Override
    public GasStack drawGas(Direction side, int amount, Action action) {
        return GasStack.EMPTY;
    }

    @Override
    public boolean canReceiveGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Override
    public boolean canDrawGas(Direction side, @Nonnull Gas type) {
        return false;
    }

    @Override
    @Nonnull
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{gasTank};
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        }
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED || (upgradeableSecondaryEfficiency() && upgrade == Upgrade.GAS)) {
            secondaryEnergyPerTick = MekanismUtils.getSecondaryEnergyPerTickMean(this, BASE_SECONDARY_ENERGY_PER_TICK);
        }
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
                return new Object[]{gasTank.getStored()};
            case 2:
                return new Object[]{getOperatingTicks()};
            case 3:
                return new Object[]{getActive()};
            case 4:
                return new Object[]{getDirection()};
            case 5:
                //TODO: Decide if we should try to get the cached recipe if it is null
                return new Object[]{cachedRecipe != null && cachedRecipe.canFunction()};
            case 6:
                return new Object[]{getMaxEnergy()};
            case 7:
                return new Object[]{getNeededEnergy()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        GasUtils.writeSustainedData(gasTank, itemStack);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        GasUtils.readSustainedData(gasTank, itemStack);
    }
}