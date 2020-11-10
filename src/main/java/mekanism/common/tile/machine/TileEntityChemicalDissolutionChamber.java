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
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ChemicalDissolutionCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.BoxedChemicalOutputHandler;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.inventory.slot.chemical.MergedChemicalInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.item.ItemStack;

public class TileEntityChemicalDissolutionChamber extends TileEntityProgressMachine<ChemicalDissolutionRecipe> {

    private static final long MAX_CHEMICAL = 10_000;
    public static final int BASE_INJECT_USAGE = 1;
    public static final int BASE_TICKS_REQUIRED = 100;
    public IGasTank injectTank;
    public MergedChemicalTank outputTank;
    public double injectUsage = BASE_INJECT_USAGE;
    public long injectUsageThisTick;

    private final BoxedChemicalOutputHandler outputHandler;
    private final IInputHandler<@NonNull ItemStack> itemInputHandler;
    private final ILongInputHandler<@NonNull GasStack> gasInputHandler;

    private MachineEnergyContainer<TileEntityChemicalDissolutionChamber> energyContainer;
    private GasInventorySlot gasInputSlot;
    private InputInventorySlot inputSlot;
    private MergedChemicalInventorySlot<MergedChemicalTank> outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityChemicalDissolutionChamber() {
        super(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, BASE_TICKS_REQUIRED);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.INFUSION, /*TransmissionType.PIGMENT, */
              TransmissionType.SLURRY, TransmissionType.ENERGY);
        configComponent.setupItemIOExtraConfig(inputSlot, outputSlot, gasInputSlot, energySlot);
        configComponent.setupIOConfig(TransmissionType.GAS, injectTank, outputTank.getGasTank(), RelativeSide.RIGHT).setEjecting(true);
        configComponent.setupOutputConfig(TransmissionType.INFUSION, outputTank.getInfusionTank(), RelativeSide.RIGHT);
        configComponent.setupOutputConfig(TransmissionType.PIGMENT, outputTank.getPigmentTank(), RelativeSide.RIGHT);
        configComponent.setupOutputConfig(TransmissionType.SLURRY, outputTank.getSlurryTank(), RelativeSide.RIGHT);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.INFUSION, /* TransmissionType.PIGMENT, */
              TransmissionType.SLURRY);

        itemInputHandler = InputHelper.getInputHandler(inputSlot);
        gasInputHandler = InputHelper.getInputHandler(injectTank);
        outputHandler = new BoxedChemicalOutputHandler(outputTank);
    }

    @Override
    protected void presetVariables() {
        outputTank = MergedChemicalTank.create(
              ChemicalTankBuilder.GAS.output(MAX_CHEMICAL, this),
              ChemicalTankBuilder.INFUSION.output(MAX_CHEMICAL, this),
              ChemicalTankBuilder.PIGMENT.output(MAX_CHEMICAL, this),
              ChemicalTankBuilder.SLURRY.output(MAX_CHEMICAL, this)
        );
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(injectTank = ChemicalTankBuilder.GAS.input(MAX_CHEMICAL, gas -> containsRecipe(recipe -> recipe.getGasInput().testType(gas)), this));
        builder.addTank(outputTank.getGasTank());
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks() {
        ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> builder = ChemicalTankHelper.forSideInfusionWithConfig(this::getDirection, this::getConfig);
        builder.addTank(outputTank.getInfusionTank());
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks() {
        ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> builder = ChemicalTankHelper.forSidePigmentWithConfig(this::getDirection, this::getConfig);
        builder.addTank(outputTank.getPigmentTank());
        return builder.build();
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks() {
        ChemicalTankHelper<Slurry, SlurryStack, ISlurryTank> builder = ChemicalTankHelper.forSideSlurryWithConfig(this::getDirection, this::getConfig);
        builder.addTank(outputTank.getSlurryTank());
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
        builder.addSlot(gasInputSlot = GasInventorySlot.fillOrConvert(injectTank, this::getWorld, this, 8, 65));
        builder.addSlot(inputSlot = InputInventorySlot.at(item -> containsRecipe(recipe -> recipe.getItemInput().testType(item)), this, 28, 36));
        builder.addSlot(outputSlot = MergedChemicalInventorySlot.drain(outputTank, this, 152, 25));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 152, 5));
        gasInputSlot.setSlotOverlay(SlotOverlay.MINUS);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        gasInputSlot.fillTankOrConvert();
        outputSlot.drainChemicalTanks();
        injectUsageThisTick = StatUtils.inversePoisson(injectUsage);
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ChemicalDissolutionRecipe> getRecipeType() {
        return MekanismRecipeType.DISSOLUTION;
    }

    @Nullable
    @Override
    public ChemicalDissolutionRecipe getRecipe(int cacheIndex) {
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
    public CachedRecipe<ChemicalDissolutionRecipe> createNewCachedRecipe(@Nonnull ChemicalDissolutionRecipe recipe, int cacheIndex) {
        return new ChemicalDissolutionCachedRecipe(recipe, itemInputHandler, gasInputHandler, () -> injectUsageThisTick, outputHandler)
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