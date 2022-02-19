package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.chemical.ChemicalChemicalToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IEitherSideRecipeLookupHandler.EitherSideChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.EitherSideChemical;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo.GasSlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;

public class TileEntityChemicalInfuser extends TileEntityRecipeMachine<ChemicalInfuserRecipe> implements EitherSideChemicalRecipeLookupHandler<Gas, GasStack, ChemicalInfuserRecipe> {

    public static final long MAX_GAS = 10_000;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getLeftInput", "getLeftInputCapacity", "getLeftInputNeeded",
                                                                                        "getLeftInputFilledPercentage"})
    public IGasTank leftTank;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getRightInput", "getRightInputCapacity", "getRightInputNeeded",
                                                                                        "getRightInputFilledPercentage"})
    public IGasTank rightTank;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"})
    public IGasTank centerTank;

    private FloatingLong clientEnergyUsed = FloatingLong.ZERO;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull GasStack> leftInputHandler;
    private final IInputHandler<@NonNull GasStack> rightInputHandler;

    private MachineEnergyContainer<TileEntityChemicalInfuser> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getLeftInputItem")
    private GasInventorySlot leftInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private GasInventorySlot outputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getRightInputItem")
    private GasInventorySlot rightInputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalInfuser() {
        super(MekanismBlocks.CHEMICAL_INFUSER);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.ENERGY);

        ConfigInfo itemConfig = configComponent.getConfig(TransmissionType.ITEM);
        if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.INPUT_1, new InventorySlotInfo(true, true, leftInputSlot));
            itemConfig.addSlotInfo(DataType.INPUT_2, new InventorySlotInfo(true, true, rightInputSlot));
            itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(true, true, outputSlot));
            itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, leftInputSlot, rightInputSlot, outputSlot));
            itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, energySlot));
            //Set default config directions
            itemConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
            itemConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
            itemConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
            itemConfig.setDataType(DataType.ENERGY, RelativeSide.BACK);
        }

        ConfigInfo gasConfig = configComponent.getConfig(TransmissionType.GAS);
        if (gasConfig != null) {
            gasConfig.addSlotInfo(DataType.INPUT_1, new GasSlotInfo(true, false, leftTank));
            gasConfig.addSlotInfo(DataType.INPUT_2, new GasSlotInfo(true, false, rightTank));
            gasConfig.addSlotInfo(DataType.OUTPUT, new GasSlotInfo(false, true, centerTank));
            gasConfig.addSlotInfo(DataType.INPUT_OUTPUT, new GasSlotInfo(true, true, leftTank, rightTank, centerTank));
            gasConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
            gasConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
            gasConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
            gasConfig.setEjecting(true);
        }

        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.GAS);

        leftInputHandler = InputHelper.getInputHandler(leftTank);
        rightInputHandler = InputHelper.getInputHandler(rightTank);
        outputHandler = OutputHelper.getOutputHandler(centerTank);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(leftTank = ChemicalTankBuilder.GAS.input(MAX_GAS, gas -> containsRecipe(gas, rightTank.getStack()), this::containsRecipe, recipeCacheLookupMonitor));
        builder.addTank(rightTank = ChemicalTankBuilder.GAS.input(MAX_GAS, gas -> containsRecipe(gas, leftTank.getStack()), this::containsRecipe, recipeCacheLookupMonitor));
        builder.addTank(centerTank = ChemicalTankBuilder.GAS.output(MAX_GAS, this));
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
        builder.addSlot(leftInputSlot = GasInventorySlot.fill(leftTank, this, 6, 56));
        builder.addSlot(rightInputSlot = GasInventorySlot.fill(rightTank, this, 154, 56));
        builder.addSlot(outputSlot = GasInventorySlot.drain(centerTank, this, 80, 65));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 154, 14));
        leftInputSlot.setSlotType(ContainerSlotType.INPUT);
        leftInputSlot.setSlotOverlay(SlotOverlay.MINUS);
        rightInputSlot.setSlotType(ContainerSlotType.INPUT);
        rightInputSlot.setSlotOverlay(SlotOverlay.MINUS);
        outputSlot.setSlotType(ContainerSlotType.OUTPUT);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        leftInputSlot.fillTank();
        rightInputSlot.fillTank();
        outputSlot.drainTank();
        clientEnergyUsed = recipeCacheLookupMonitor.updateAndProcess(energyContainer);
    }

    @Nonnull
    @ComputerMethod(nameOverride = "getEnergyUsage")
    public FloatingLong getEnergyUsed() {
        return clientEnergyUsed;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ChemicalInfuserRecipe, EitherSideChemical<Gas, GasStack, ChemicalInfuserRecipe>> getRecipeType() {
        return MekanismRecipeType.CHEMICAL_INFUSING;
    }

    @Nullable
    @Override
    public ChemicalInfuserRecipe getRecipe(int cacheIndex) {
        return findFirstRecipe(leftInputHandler, rightInputHandler);
    }

    @Nonnull
    @Override
    public CachedRecipe<ChemicalInfuserRecipe> createNewCachedRecipe(@Nonnull ChemicalInfuserRecipe recipe, int cacheIndex) {
        return new ChemicalChemicalToChemicalCachedRecipe<>(recipe, leftInputHandler, rightInputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
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

    public MachineEnergyContainer<TileEntityChemicalInfuser> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
    }
}