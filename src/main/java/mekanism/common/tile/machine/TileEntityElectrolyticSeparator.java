package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ElectrolysisCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.ElectrolyticSeparatorEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler.FluidRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.SingleFluid;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityChemicalTank.GasMode;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.GasSlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

public class TileEntityElectrolyticSeparator extends TileEntityRecipeMachine<ElectrolysisRecipe> implements IHasGasMode, FluidRecipeLookupHandler<ElectrolysisRecipe>,
      ISustainedData {

    /**
     * This separator's water slot.
     */
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"})
    public BasicFluidTank fluidTank;
    /**
     * The maximum amount of gas this block can store.
     */
    private static final long MAX_GAS = 2_400;
    /**
     * The amount of oxygen this block is storing.
     */
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getLeftOutput", "getLeftOutputCapacity", "getLeftOutputNeeded",
                                                                                        "getLeftOutputFilledPercentage"})
    public IGasTank leftTank;
    /**
     * The amount of hydrogen this block is storing.
     */
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getRightOutput", "getRightOutputCapacity", "getRightOutputNeeded",
                                                                                        "getRightOutputFilledPercentage"})
    public IGasTank rightTank;
    @SyntheticComputerMethod(getter = "getLeftOutputDumpingMode")
    public GasMode dumpLeft = GasMode.IDLE;
    @SyntheticComputerMethod(getter = "getRightOutputDumpingMode")
    public GasMode dumpRight = GasMode.IDLE;
    private FloatingLong clientEnergyUsed = FloatingLong.ZERO;
    private FloatingLong recipeEnergyMultiplier = FloatingLong.ONE;
    private final IOutputHandler<@NonNull Pair<GasStack, GasStack>> outputHandler;
    private final IInputHandler<@NonNull FluidStack> inputHandler;

    private ElectrolyticSeparatorEnergyContainer energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem")
    private FluidInventorySlot fluidSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getLeftOutputItem")
    private GasInventorySlot leftOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getRightOutputItem")
    private GasInventorySlot rightOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityElectrolyticSeparator() {
        super(MekanismBlocks.ELECTROLYTIC_SEPARATOR);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.FLUID, TransmissionType.ENERGY);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT, new InventorySlotInfo(true, true, fluidSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT_1, new InventorySlotInfo(true, true, leftOutputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT_2, new InventorySlotInfo(true, true, rightOutputSlot));
            itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, fluidSlot, leftOutputSlot, rightOutputSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
            //Set default config directions
            itemConfig.setDataType(DataType.INPUT, RelativeSide.FRONT);
            itemConfig.setDataType(DataType.OUTPUT_1, RelativeSide.LEFT);
            itemConfig.setDataType(DataType.OUTPUT_2, RelativeSide.RIGHT);
            itemConfig.setDataType(DataType.ENERGY, RelativeSide.BACK);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            gasConfig.addSlotInfo(DataType.OUTPUT_1, new GasSlotInfo(false, true, leftTank));
            gasConfig.addSlotInfo(DataType.OUTPUT_2, new GasSlotInfo(false, true, rightTank));
            gasConfig.setDataType(DataType.OUTPUT_1, RelativeSide.LEFT);
            gasConfig.setDataType(DataType.OUTPUT_2, RelativeSide.RIGHT);
            gasConfig.setEjecting(true);
        }

        configComponent.setupInputConfig(TransmissionType.FLUID, fluidTank);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.GAS)
              .setCanTankEject(tank -> {
                  if (tank == leftTank) {
                      return dumpLeft != GasMode.DUMPING;
                  } else if (tank == rightTank) {
                      return dumpRight != GasMode.DUMPING;
                  }
                  return true;
              });

        inputHandler = InputHelper.getInputHandler(fluidTank);
        outputHandler = OutputHelper.getOutputHandler(leftTank, rightTank);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addTank(fluidTank = BasicFluidTank.input(24_000, this::containsRecipe, recipeCacheLookupMonitor));
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(leftTank = ChemicalTankBuilder.GAS.output(MAX_GAS, this));
        builder.addTank(rightTank = ChemicalTankBuilder.GAS.output(MAX_GAS, this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addContainer(energyContainer = ElectrolyticSeparatorEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addSlot(fluidSlot = FluidInventorySlot.fill(fluidTank, this, 26, 35));
        builder.addSlot(leftOutputSlot = GasInventorySlot.drain(leftTank, this, 59, 52));
        builder.addSlot(rightOutputSlot = GasInventorySlot.drain(rightTank, this, 101, 52));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 143, 35));
        fluidSlot.setSlotType(ContainerSlotType.INPUT);
        leftOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
        rightOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
        return builder.build();
    }

    @Override
    public void onCachedRecipeChanged(@Nullable CachedRecipe<ElectrolysisRecipe> cachedRecipe, int cacheIndex) {
        recipeEnergyMultiplier = cachedRecipe == null ? FloatingLong.ONE : cachedRecipe.getRecipe().getEnergyMultiplier();
        energyContainer.updateEnergyPerTick();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        fluidSlot.fillTank();

        leftOutputSlot.drainTank();
        rightOutputSlot.drainTank();
        clientEnergyUsed = recipeCacheLookupMonitor.updateAndProcess(energyContainer);

        handleTank(leftTank, dumpLeft);
        handleTank(rightTank, dumpRight);
    }

    private void handleTank(IGasTank tank, GasMode mode) {
        if (!tank.isEmpty()) {
            if (mode == GasMode.DUMPING) {
                tank.shrinkStack(8 * (long) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), Action.EXECUTE);
            } else if (mode == GasMode.DUMPING_EXCESS) {
                long target = getDumpingExcessTarget(tank);
                long stored = tank.getStored();
                if (target < stored) {
                    //Dump excess that we need to get to the target (capping at our eject rate for how much we can dump at once)
                    tank.shrinkStack(Math.min(stored - target, MekanismConfig.general.chemicalAutoEjectRate.get()), Action.EXECUTE);
                }
            }
        }
    }

    private long getDumpingExcessTarget(IGasTank tank) {
        return MathUtils.clampToLong(tank.getCapacity() * MekanismConfig.general.dumpExcessKeepRatio.get());
    }

    private boolean atDumpingExcessTarget(IGasTank tank) {
        //Check >= so that if we are past and our eject rate is just low then we don't continue making it, so we never get to the eject rate
        return tank.getStored() >= getDumpingExcessTarget(tank);
    }

    private boolean canFunction() {
        //We can function if:
        // - the tile can function
        // - at least one side is not set to dumping excess
        // - at least one side is not at the dumping excess target
        return MekanismUtils.canFunction(this) && (dumpLeft != GasMode.DUMPING_EXCESS || dumpRight != GasMode.DUMPING_EXCESS ||
                                                   !atDumpingExcessTarget(leftTank) || !atDumpingExcessTarget(rightTank));
    }

    public FloatingLong getRecipeEnergyMultiplier() {
        return recipeEnergyMultiplier;
    }

    @Nonnull
    @ComputerMethod(nameOverride = "getEnergyUsage")
    public FloatingLong getEnergyUsed() {
        return clientEnergyUsed;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ElectrolysisRecipe, SingleFluid<ElectrolysisRecipe>> getRecipeType() {
        return MekanismRecipeType.SEPARATING;
    }

    @Nullable
    @Override
    public ElectrolysisRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(inputHandler);
    }

    @Nonnull
    @Override
    public CachedRecipe<ElectrolysisRecipe> createNewCachedRecipe(@Nonnull ElectrolysisRecipe recipe, int cacheIndex) {
        return new ElectrolysisCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(this::canFunction)
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setOnFinish(() -> markDirty(false))
              .setPostProcessOperations(currentMax -> {
                  if (currentMax <= 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return currentMax;
                  }
                  return Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), currentMax);
              });
    }

    public ElectrolyticSeparatorEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void nextMode(int tank) {
        if (tank == 0) {
            dumpLeft = dumpLeft.getNext();
            markDirty(false);
        } else if (tank == 1) {
            dumpRight = dumpRight.getNext();
            markDirty(false);
        }
    }

    @Override
    protected void loadGeneralPersistentData(CompoundNBT data) {
        super.loadGeneralPersistentData(data);
        NBTUtils.setEnumIfPresent(data, NBTConstants.DUMP_LEFT, GasMode::byIndexStatic, mode -> dumpLeft = mode);
        NBTUtils.setEnumIfPresent(data, NBTConstants.DUMP_RIGHT, GasMode::byIndexStatic, mode -> dumpRight = mode);
    }

    @Override
    protected void addGeneralPersistentData(CompoundNBT data) {
        super.addGeneralPersistentData(data);
        data.putInt(NBTConstants.DUMP_LEFT, dumpLeft.ordinal());
        data.putInt(NBTConstants.DUMP_RIGHT, dumpRight.ordinal());
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        ItemDataUtils.setInt(itemStack, NBTConstants.DUMP_LEFT, dumpLeft.ordinal());
        ItemDataUtils.setInt(itemStack, NBTConstants.DUMP_RIGHT, dumpRight.ordinal());
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        dumpLeft = GasMode.byIndexStatic(ItemDataUtils.getInt(itemStack, NBTConstants.DUMP_LEFT));
        dumpRight = GasMode.byIndexStatic(ItemDataUtils.getInt(itemStack, NBTConstants.DUMP_RIGHT));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.DUMP_LEFT, NBTConstants.DUMP_LEFT);
        remap.put(NBTConstants.DUMP_RIGHT, NBTConstants.DUMP_RIGHT);
        return remap;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(GasMode::byIndexStatic, GasMode.IDLE, () -> dumpLeft, value -> dumpLeft = value));
        container.track(SyncableEnum.create(GasMode::byIndexStatic, GasMode.IDLE, () -> dumpRight, value -> dumpRight = value));
        container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
    }

    //Methods relating to IComputerTile
    @ComputerMethod
    private void setLeftOutputDumpingMode(GasMode mode) throws ComputerException {
        validateSecurityIsPublic();
        if (dumpLeft != mode) {
            dumpLeft = mode;
            markDirty(false);
        }
    }

    @ComputerMethod
    private void incrementLeftOutputDumpingMode() throws ComputerException {
        validateSecurityIsPublic();
        nextMode(0);
    }

    @ComputerMethod
    private void decrementLeftOutputDumpingMode() throws ComputerException {
        validateSecurityIsPublic();
        dumpLeft = dumpLeft.getPrevious();
        markDirty(false);
    }

    @ComputerMethod
    private void setRightOutputDumpingMode(GasMode mode) throws ComputerException {
        validateSecurityIsPublic();
        if (dumpRight != mode) {
            dumpRight = mode;
            markDirty(false);
        }
    }

    @ComputerMethod
    private void incrementRightOutputDumpingMode() throws ComputerException {
        validateSecurityIsPublic();
        nextMode(1);
    }

    @ComputerMethod
    private void decrementRightOutputDumpingMode() throws ComputerException {
        validateSecurityIsPublic();
        dumpRight = dumpRight.getPrevious();
        markDirty(false);
    }
    //End methods IComputerTile
}