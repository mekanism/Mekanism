package mekanism.common.tile;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackGasToGasCachedRecipe;
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
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityOperationalMachine;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.item.ItemStack;

public class TileEntityChemicalDissolutionChamber extends TileEntityOperationalMachine<ItemStackGasToGasRecipe> {

    public static final long MAX_GAS = 10_000;
    public static final int BASE_INJECT_USAGE = 1;
    public static final int BASE_TICKS_REQUIRED = 100;
    public BasicGasTank injectTank;
    public BasicGasTank outputTank;
    public double injectUsage = BASE_INJECT_USAGE;
    public long injectUsageThisTick;
    public long gasOutput = 256;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final IInputHandler<@NonNull GasStack> gasInputHandler;

    private MachineEnergyContainer<TileEntityChemicalDissolutionChamber> energyContainer;
    private GasInventorySlot gasInputSlot;
    private InputInventorySlot inputSlot;
    private GasInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalDissolutionChamber() {
        super(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, BASE_TICKS_REQUIRED);
        itemInputHandler = InputHelper.getInputHandler(inputSlot);
        gasInputHandler = InputHelper.getInputHandler(injectTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGas(this::getDirection);
        builder.addTank(injectTank = BasicGasTank.input(MAX_GAS, gas -> containsRecipe(recipe -> recipe.getGasInput().testType(gas)), this), RelativeSide.LEFT);
        builder.addTank(outputTank = BasicGasTank.ejectOutput(MAX_GAS, this), RelativeSide.RIGHT);
        return builder.build();
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
        builder.addSlot(gasInputSlot = GasInventorySlot.fillOrConvert(injectTank, this::getWorld, this, 6, 65), RelativeSide.BOTTOM);
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getItemInput().testType(item)), this, 26, 36),
              RelativeSide.TOP, RelativeSide.LEFT);
        builder.addSlot(outputSlot = GasInventorySlot.drain(outputTank, this, 155, 25), RelativeSide.RIGHT);
        //TODO: Make this be accessible from some side for automation??
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 155, 5));
        gasInputSlot.setSlotOverlay(SlotOverlay.MINUS);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        gasInputSlot.fillTankOrConvert();
        outputSlot.drainTank();
        injectUsageThisTick = StatUtils.inversePoisson(injectUsage);
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
        GasUtils.emit(EnumSet.of(getRightSide()), outputTank, this, gasOutput);
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ItemStackGasToGasRecipe> getRecipeType() {
        return MekanismRecipeType.DISSOLUTION;
    }

    @Nullable
    @Override
    public CachedRecipe<ItemStackGasToGasRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ItemStackGasToGasRecipe getRecipe(int cacheIndex) {
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
    public CachedRecipe<ItemStackGasToGasRecipe> createNewCachedRecipe(@Nonnull ItemStackGasToGasRecipe recipe, int cacheIndex) {
        return new ItemStackGasToGasCachedRecipe(recipe, itemInputHandler, gasInputHandler, () -> injectUsageThisTick, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setRequiredTicks(() -> ticksRequired)
              .setOnFinish(() -> markDirty(false))
              .setOperatingTicksChanged(this::setOperatingTicks);
    }

    @Override
    public TileComponentUpgrade getComponent() {
        return upgradeComponent;
    }

    @Override
    public void recalculateUpgrades(Upgrade upgrade) {
        super.recalculateUpgrades(upgrade);
        if (upgrade == Upgrade.GAS || upgrade == Upgrade.SPEED) {
            injectUsage = MekanismUtils.getGasPerTickMean(this, BASE_INJECT_USAGE);
        }
    }

    public MachineEnergyContainer<TileEntityChemicalDissolutionChamber> getEnergyContainer() {
        return energyContainer;
    }
}