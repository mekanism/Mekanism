package mekanism.common.tile.prefab;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.BasicGasTank;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
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
import mekanism.common.base.IChemicalTankHolder;
import mekanism.common.capabilities.holder.ChemicalTankHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.upgrade.AdvancedMachineUpgradeData;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public abstract class TileEntityAdvancedElectricMachine extends TileEntityBasicMachine<ItemStackGasToItemStackRecipe> implements IGasHandler, ISustainedData {

    private static final String[] methods = new String[]{"getEnergy", "getSecondaryStored", "getProgress", "isActive", "facing", "canOperate", "getMaxEnergy",
                                                         "getEnergyNeeded"};
    public static final int BASE_TICKS_REQUIRED = 200;
    public static final int BASE_GAS_PER_TICK = 1;
    public static final int MAX_GAS = 210;
    /**
     * How much secondary energy (fuel) this machine uses per tick, not including upgrades.
     */
    public int BASE_SECONDARY_ENERGY_PER_TICK;
    /**
     * How much secondary energy this machine uses per tick, including upgrades.
     */
    public double secondaryEnergyPerTick;
    private int gasUsageThisTick;
    public BasicGasTank gasTank;

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
            energyConfig.addSlotInfo(DataType.INPUT, new EnergySlotInfo(true, false));
            energyConfig.fill(DataType.INPUT);
            energyConfig.setCanEject(false);
        }

        BASE_SECONDARY_ENERGY_PER_TICK = secondaryPerTick;
        secondaryEnergyPerTick = secondaryPerTick;
        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(TransmissionType.ITEM, itemConfig);

        itemInputHandler = InputHelper.getInputHandler(inputSlot);
        gasInputHandler = InputHelper.getInputHandler(gasTank);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(gasTank = BasicGasTank.input(MAX_GAS, this::isValidGas, this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getItemInput().testType(item)), this, 64, 17));
        builder.addSlot(secondarySlot = GasInventorySlot.fillOrConvert(gasTank, this::isValidGas, this::getWorld, this, 64, 53));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 116, 35));
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 39, 35));
        return builder.build();
    }

    public boolean isValidGas(@Nonnull Gas gas) {
        return containsRecipe(recipe -> recipe.getGasInput().testType(gas));
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            secondarySlot.fillTankOrConvert();
            //TODO: Is there some better way to do this rather than storing it and then doing it like this?
            // TODO: Also evaluate if there is a better way of doing the secondary calculation when not using statistical mechanics
            gasUsageThisTick = useStatisticalMechanics() ? StatUtils.inversePoisson(secondaryEnergyPerTick) : (int) Math.ceil(secondaryEnergyPerTick);
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
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
        return new ItemStackGasToItemStackCachedRecipe(recipe, itemInputHandler, gasInputHandler, () -> gasUsageThisTick, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(this::getEnergyPerTick, this::getEnergy, energy -> setEnergy(getEnergy() - energy))
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(this::markDirty)
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.SPEED || (upgrade == Upgrade.GAS && getSupportedUpgrade().contains(Upgrade.GAS))) {
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
        if (!gasTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "gasStored", gasTank.getStack().write(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        gasTank.setStack(GasStack.readFromNBT(ItemDataUtils.getCompound(itemStack, "gasStored")));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("gasTank.stored", "gasStored");
        return remap;
    }

    @Nonnull
    @Override
    public AdvancedMachineUpgradeData getUpgradeData() {
        return new AdvancedMachineUpgradeData(redstone, getControlType(), getEnergy(), getOperatingTicks(), gasTank.getStack(), secondarySlot, energySlot, inputSlot,
              outputSlot, getComponents());
    }
}