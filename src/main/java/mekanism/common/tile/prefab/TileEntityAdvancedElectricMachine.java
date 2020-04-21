package mekanism.common.tile.prefab;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackGasToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.upgrade.AdvancedMachineUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.item.ItemStack;

public abstract class TileEntityAdvancedElectricMachine extends TileEntityBasicMachine<ItemStackGasToItemStackRecipe> {

    public static final int BASE_TICKS_REQUIRED = 200;
    public static final long MAX_GAS = 210;
    public static final long BASE_GAS_PER_TICK = 1;

    /**
     * How much secondary energy this machine uses per tick, including upgrades.
     */
    public double gasUsage;
    private long gasUsageThisTick;
    public BasicGasTank gasTank;

    protected final IOutputHandler<@NonNull ItemStack> outputHandler;
    protected final IInputHandler<@NonNull ItemStack> itemInputHandler;
    protected final ILongInputHandler<@NonNull GasStack> gasInputHandler;

    private MachineEnergyContainer<TileEntityAdvancedElectricMachine> energyContainer;
    private InputInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private GasInventorySlot secondarySlot;
    private EnergyInventorySlot energySlot;

    public TileEntityAdvancedElectricMachine(IBlockProvider blockProvider, int ticksRequired) {
        super(blockProvider, ticksRequired);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, false, inputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(false, true, outputSlot));
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, secondarySlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
            //Set default config directions
            itemConfig.setDataType(RelativeSide.TOP, DataType.INPUT);
            itemConfig.setDataType(RelativeSide.RIGHT, DataType.OUTPUT);
            itemConfig.setDataType(RelativeSide.BOTTOM, DataType.EXTRA);
            itemConfig.setDataType(RelativeSide.BACK, DataType.ENERGY);
        }

        ConfigInfo energyConfig = configComponent.getConfig(TransmissionType.ENERGY);
        if (energyConfig != null) {
            energyConfig.addSlotInfo(DataType.INPUT, new EnergySlotInfo(true, false, energyContainer));
            energyConfig.fill(DataType.INPUT);
            energyConfig.setCanEject(false);
        }

        gasUsage = BASE_GAS_PER_TICK;
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, itemConfig);

        itemInputHandler = InputHelper.getInputHandler(inputSlot);
        gasInputHandler = InputHelper.getInputHandler(gasTank);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(gasTank = BasicGasTank.input(MAX_GAS, gas -> containsRecipe(recipe -> recipe.getGasInput().testType(gas)), this));
        return builder.build();
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
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getItemInput().testType(item)), this, 64, 17));
        builder.addSlot(secondarySlot = GasInventorySlot.fillOrConvert(gasTank, this::getWorld, this, 64, 53));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 116, 35));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 39, 35));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        energySlot.fillContainerOrConvert();
        secondarySlot.fillTankOrConvert();
        //TODO: Is there some better way to do this rather than storing it and then doing it like this?
        // Also evaluate if there is a better way of doing the secondary calculation when not using statistical mechanics
        gasUsageThisTick = useStatisticalMechanics() ? StatUtils.inversePoisson(gasUsage) : MathUtils.clampToLong(Math.ceil(gasUsage));
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
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
        return new ItemStackGasToItemStackCachedRecipe<>(recipe, itemInputHandler, gasInputHandler, () -> gasUsageThisTick, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED || (upgrade == Upgrade.GAS && getSupportedUpgrade().contains(Upgrade.GAS))) {
            gasUsage = MekanismUtils.getGasPerTickMean(this, BASE_GAS_PER_TICK);
        }
    }

    @Nonnull
    @Override
    public AdvancedMachineUpgradeData getUpgradeData() {
        return new AdvancedMachineUpgradeData(redstone, getControlType(), getEnergyContainer(), getOperatingTicks(), gasTank.getStack(), secondarySlot, energySlot, inputSlot,
              outputSlot, getComponents());
    }

    public MachineEnergyContainer<TileEntityAdvancedElectricMachine> getEnergyContainer() {
        return energyContainer;
    }
}