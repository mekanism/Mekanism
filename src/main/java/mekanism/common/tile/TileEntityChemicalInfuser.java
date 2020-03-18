package mekanism.common.tile;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.inventory.AutomationType;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ChemicalInfuserCachedRecipe;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;

public class TileEntityChemicalInfuser extends TileEntityMekanism implements ITankManager, ITileCachedRecipeHolder<ChemicalInfuserRecipe> {

    public static final int MAX_GAS = 10_000;
    public BasicGasTank leftTank;
    public BasicGasTank rightTank;
    public BasicGasTank centerTank;
    public int gasOutput = 256;

    public CachedRecipe<ChemicalInfuserRecipe> cachedRecipe;

    public double clientEnergyUsed;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull GasStack> leftInputHandler;
    private final IInputHandler<@NonNull GasStack> rightInputHandler;

    private MachineEnergyContainer<TileEntityChemicalInfuser> energyContainer;
    private GasInventorySlot leftInputSlot;
    private GasInventorySlot outputSlot;
    private GasInventorySlot rightInputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalInfuser() {
        super(MekanismBlocks.CHEMICAL_INFUSER);
        leftInputHandler = InputHelper.getInputHandler(leftTank);
        rightInputHandler = InputHelper.getInputHandler(rightTank);
        outputHandler = OutputHelper.getOutputHandler(centerTank);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGas(this::getDirection);
        builder.addTank(leftTank = BasicGasTank.input(MAX_GAS, gas -> isValidGas(gas, rightTank), this::isValidGas, this), RelativeSide.LEFT);
        builder.addTank(rightTank = BasicGasTank.input(MAX_GAS, gas -> isValidGas(gas, leftTank), this::isValidGas, this), RelativeSide.RIGHT);
        builder.addTank(centerTank = BasicGasTank.output(MAX_GAS, this), RelativeSide.FRONT);
        return builder.build();
    }

    private boolean isValidGas(@Nonnull Gas gas) {
        return containsRecipe(recipe -> recipe.getLeftInput().testType(gas) || recipe.getRightInput().testType(gas));
    }

    private boolean isValidGas(@Nonnull Gas gas, IChemicalTank<Gas, GasStack> otherTank) {
        if (otherTank.isEmpty()) {
            //If the other tank is empty, don't limit what can enter our tank based on it
            return true;
        }
        GasStack stack = otherTank.getStack();
        return containsRecipe(recipe -> {
            GasStackIngredient leftInput = recipe.getLeftInput();
            GasStackIngredient rightInput = recipe.getRightInput();
            return rightInput.testType(gas) && leftInput.testType(stack) || leftInput.testType(gas) && rightInput.testType(stack);
        });
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = MachineEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //TODO: Should our gas checking, also check the other tank's contents so we don't let putting the same gas in on both sides
        builder.addSlot(leftInputSlot = GasInventorySlot.fill(leftTank, this, 5, 56), RelativeSide.LEFT);
        builder.addSlot(rightInputSlot = GasInventorySlot.fill(rightTank, this, 155, 56), RelativeSide.RIGHT);
        builder.addSlot(outputSlot = GasInventorySlot.drain(centerTank, this, 80, 65), RelativeSide.FRONT);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 155, 5), RelativeSide.BOTTOM, RelativeSide.TOP);
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
        double prev = energyContainer.getEnergy();
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
        //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
        clientEnergyUsed = prev - energyContainer.getEnergy();
        GasUtils.emit(EnumSet.of(getDirection()), centerTank, this, gasOutput);
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ChemicalInfuserRecipe> getRecipeType() {
        return MekanismRecipeType.CHEMICAL_INFUSING;
    }

    @Nullable
    @Override
    public CachedRecipe<ChemicalInfuserRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ChemicalInfuserRecipe getRecipe(int cacheIndex) {
        GasStack leftGas = leftInputHandler.getInput();
        if (leftGas.isEmpty()) {
            return null;
        }
        GasStack rightGas = rightInputHandler.getInput();
        if (rightGas.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(leftGas, rightGas));
    }

    @Nullable
    @Override
    public CachedRecipe<ChemicalInfuserRecipe> createNewCachedRecipe(@Nonnull ChemicalInfuserRecipe recipe, int cacheIndex) {
        return new ChemicalInfuserCachedRecipe(recipe, leftInputHandler, rightInputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer::getEnergy, energy -> energyContainer.extract(energy, Action.EXECUTE, AutomationType.INTERNAL))
              .setOnFinish(this::markDirty)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax <= 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return currentMax;
                  }
                  return Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), currentMax);
              });
    }

    @Override
    public Object[] getManagedTanks() {
        return new Object[]{leftTank, rightTank, centerTank};
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    public MachineEnergyContainer<TileEntityChemicalInfuser> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(() -> clientEnergyUsed, value -> clientEnergyUsed = value));
    }
}