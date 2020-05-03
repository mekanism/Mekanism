package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.recipes.GasToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.GasToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
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
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.slot.EnergySlotInfo;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;

public class TileEntityChemicalCrystallizer extends TileEntityProgressMachine<GasToItemStackRecipe> {

    public static final long MAX_GAS = 10_000;

    public BasicGasTank inputTank;

    private final IOutputHandler<@NonNull ItemStack> outputHandler;
    private final IInputHandler<@NonNull GasStack> inputHandler;

    private MachineEnergyContainer<TileEntityChemicalCrystallizer> energyContainer;
    private GasInventorySlot inputSlot;
    private OutputInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalCrystallizer() {
        super(MekanismBlocks.CHEMICAL_CRYSTALLIZER, 200);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
        configComponent.setupItemIOConfig(inputSlot, outputSlot, energySlot);
        configComponent.setupInputConfig(TransmissionType.ENERGY, new EnergySlotInfo(true, false, energyContainer));

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM);

        inputHandler = InputHelper.getInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputSlot);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(inputTank = BasicGasTank.input(MAX_GAS, gas -> containsRecipe(recipe -> recipe.getInput().testType(gas)), this));
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
        builder.addSlot(inputSlot = GasInventorySlot.fill(inputTank, this, 8, 65));
        builder.addSlot(outputSlot = OutputInventorySlot.at(this, 129, 57));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 152, 5));
        inputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        inputSlot.fillTank();
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<GasToItemStackRecipe> getRecipeType() {
        return MekanismRecipeType.CRYSTALLIZING;
    }

    @Nullable
    @Override
    public GasToItemStackRecipe getRecipe(int cacheIndex) {
        GasStack gasStack = inputHandler.getInput();
        if (gasStack.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(gasStack));
    }

    @Nullable
    @Override
    public CachedRecipe<GasToItemStackRecipe> createNewCachedRecipe(@Nonnull GasToItemStackRecipe recipe, int cacheIndex) {
        return new GasToItemStackCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    public MachineEnergyContainer<TileEntityChemicalCrystallizer> getEnergyContainer() {
        return energyContainer;
    }
}