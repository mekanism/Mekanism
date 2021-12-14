package mekanism.common.tile.machine;

import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.chemical.FluidChemicalToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerChemicalTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.SlurryInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler.FluidChemicalRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.FluidChemical;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityChemicalWasher extends TileEntityRecipeMachine<FluidSlurryToSlurryRecipe> implements
      FluidChemicalRecipeLookupHandler<Slurry, SlurryStack, FluidSlurryToSlurryRecipe> {

    private static final long MAX_SLURRY = 10_000;
    private static final int MAX_FLUID = 10_000;
    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded", "getFluidFilledPercentage"})
    public BasicFluidTank fluidTank;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSlurryInput", "getSlurryInputCapacity", "getSlurryInputNeeded",
                                                                                        "getSlurryInputFilledPercentage"})
    public ISlurryTank inputTank;
    @WrappingComputerMethod(wrapper = ComputerChemicalTankWrapper.class, methodNames = {"getSlurryOutput", "getSlurryOutputCapacity", "getSlurryOutputNeeded",
                                                                                        "getSlurryOutputFilledPercentage"})
    public ISlurryTank outputTank;

    private FloatingLong clientEnergyUsed = FloatingLong.ZERO;

    private final IOutputHandler<@NonNull SlurryStack> outputHandler;
    private final IInputHandler<@NonNull FluidStack> fluidInputHandler;
    private final IInputHandler<@NonNull SlurryStack> slurryInputHandler;

    private MachineEnergyContainer<TileEntityChemicalWasher> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFluidItemInput")
    private FluidInventorySlot fluidSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFluidItemOutput")
    private OutputInventorySlot fluidOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private SlurryInventorySlot slurryOutputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalWasher() {
        super(MekanismBlocks.CHEMICAL_WASHER);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.SLURRY, TransmissionType.FLUID, TransmissionType.ENERGY);
        configComponent.setupItemIOConfig(Collections.singletonList(fluidSlot), Arrays.asList(slurryOutputSlot, fluidOutputSlot), energySlot, true);
        configComponent.setupIOConfig(TransmissionType.SLURRY, inputTank, outputTank, RelativeSide.RIGHT).setEjecting(true);
        configComponent.setupInputConfig(TransmissionType.FLUID, fluidTank);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.SLURRY);

        fluidInputHandler = InputHelper.getInputHandler(fluidTank);
        slurryInputHandler = InputHelper.getInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks() {
        ChemicalTankHelper<Slurry, SlurryStack, ISlurryTank> builder = ChemicalTankHelper.forSideSlurryWithConfig(this::getDirection, this::getConfig);
        builder.addTank(inputTank = ChemicalTankBuilder.SLURRY.input(MAX_SLURRY, slurry -> containsRecipeBA(fluidTank.getFluid(), slurry), this::containsRecipeB,
              recipeCacheLookupMonitor));
        builder.addTank(outputTank = ChemicalTankBuilder.SLURRY.output(MAX_SLURRY, this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
        builder.addTank(fluidTank = BasicFluidTank.input(MAX_FLUID, fluid -> containsRecipeAB(fluid, inputTank.getStack()), this::containsRecipeA, recipeCacheLookupMonitor));
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
        builder.addSlot(fluidSlot = FluidInventorySlot.fill(fluidTank, this, 180, 71));
        //Output slot for the fluid container that was used as an input
        builder.addSlot(fluidOutputSlot = OutputInventorySlot.at(this, 180, 102));
        builder.addSlot(slurryOutputSlot = SlurryInventorySlot.drain(outputTank, this, 152, 56));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, this, 152, 14));
        slurryOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
        fluidSlot.setSlotType(ContainerSlotType.INPUT);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        fluidSlot.fillTank(fluidOutputSlot);
        slurryOutputSlot.drainTank();
        clientEnergyUsed = recipeCacheLookupMonitor.updateAndProcess(energyContainer);
    }

    @Nonnull
    @ComputerMethod(nameOverride = "getEnergyUsage")
    public FloatingLong getEnergyUsed() {
        return clientEnergyUsed;
    }

    @Nonnull
    @Override
    public MekanismRecipeType<FluidSlurryToSlurryRecipe, FluidChemical<Slurry, SlurryStack, FluidSlurryToSlurryRecipe>> getRecipeType() {
        return MekanismRecipeType.WASHING;
    }

    @Nullable
    @Override
    public FluidSlurryToSlurryRecipe getRecipe(int cacheIndex) {
        return getRecipeType().getInputCache().findFirstRecipe(level, fluidInputHandler.getInput(), slurryInputHandler.getInput(), false);
    }

    @Nonnull
    @Override
    public CachedRecipe<FluidSlurryToSlurryRecipe> createNewCachedRecipe(@Nonnull FluidSlurryToSlurryRecipe recipe, int cacheIndex) {
        return new FluidChemicalToChemicalCachedRecipe<>(recipe, fluidInputHandler, slurryInputHandler, outputHandler)
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

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getCapacity());
    }

    public MachineEnergyContainer<TileEntityChemicalWasher> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> clientEnergyUsed = value));
    }
}