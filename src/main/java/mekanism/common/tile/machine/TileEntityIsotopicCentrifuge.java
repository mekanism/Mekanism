package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.GasToGasCachedRecipe;
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
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class TileEntityIsotopicCentrifuge extends TileEntityRecipeMachine<GasToGasRecipe> implements IBoundingBlock {

    public static final int MAX_GAS = 10_000;

    public IGasTank inputTank;
    public IGasTank outputTank;

    public FloatingLong clientEnergyUsed = FloatingLong.ZERO;

    private final IOutputHandler<@NonNull GasStack> outputHandler;
    private final IInputHandler<@NonNull GasStack> inputHandler;

    private MachineEnergyContainer<TileEntityIsotopicCentrifuge> energyContainer;
    private GasInventorySlot inputSlot;
    private GasInventorySlot outputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityIsotopicCentrifuge() {
        super(MekanismBlocks.ISOTOPIC_CENTRIFUGE);
        configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.ENERGY);
        configComponent.setupItemIOConfig(inputSlot, outputSlot, energySlot);
        configComponent.setupIOConfig(TransmissionType.GAS, inputTank, outputTank, RelativeSide.FRONT).setEjecting(true);
        configComponent.setupInputConfig(TransmissionType.ENERGY, energyContainer);

        ejectorComponent = new TileComponentEjector(this);
        ejectorComponent.setOutputData(configComponent, TransmissionType.ITEM, TransmissionType.GAS);

        inputHandler = InputHelper.getInputHandler(inputTank);
        outputHandler = OutputHelper.getOutputHandler(outputTank);
    }

    @Nonnull
    @Override
    public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
        builder.addTank(inputTank = ChemicalTankBuilder.GAS.create(MAX_GAS, ChemicalTankBuilder.GAS.notExternal, ChemicalTankBuilder.GAS.alwaysTrueBi,
              gas -> containsRecipe(recipe -> recipe.getInput().testType(gas)), ChemicalAttributeValidator.ALWAYS_ALLOW, this));
        builder.addTank(outputTank = ChemicalTankBuilder.GAS.output(MAX_GAS, this));
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
        builder.addSlot(inputSlot = GasInventorySlot.fill(inputTank, this, 5, 56));
        builder.addSlot(outputSlot = GasInventorySlot.drain(outputTank, this, 155, 56));
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 155, 5));
        inputSlot.setSlotType(ContainerSlotType.INPUT);
        inputSlot.setSlotOverlay(SlotOverlay.MINUS);
        outputSlot.setSlotType(ContainerSlotType.OUTPUT);
        outputSlot.setSlotOverlay(SlotOverlay.PLUS);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        inputSlot.fillTank();
        outputSlot.drainTank();
        FloatingLong prev = energyContainer.getEnergy().copyAsConst();
        cachedRecipe = getUpdatedCache(0);
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
        //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
        clientEnergyUsed = prev.subtract(energyContainer.getEnergy());
    }

    @Nonnull
    @Override
    public MekanismRecipeType<GasToGasRecipe> getRecipeType() {
        return MekanismRecipeType.CENTRIFUGING;
    }

    @Nullable
    @Override
    public GasToGasRecipe getRecipe(int cacheIndex) {
        GasStack gas = inputHandler.getInput();
        if (gas.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(gas));
    }

    @Nullable
    @Override
    public CachedRecipe<GasToGasRecipe> createNewCachedRecipe(@Nonnull GasToGasRecipe recipe, int cacheIndex) {
        return new GasToGasCachedRecipe(recipe, inputHandler, outputHandler)
              .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
              .setActive(this::setActive)
              .setOnFinish(() -> markDirty(false))
              .setEnergyRequirements(energyContainer::getEnergyPerTick, energyContainer)
              .setPostProcessOperations(currentMax -> {
                  if (currentMax <= 0) {
                      //Short circuit that if we already can't perform any outputs, just return
                      return currentMax;
                  }
                  return Math.min((int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED)), currentMax);
              });
    }

    public MachineEnergyContainer<TileEntityIsotopicCentrifuge> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(inputTank.getStored(), inputTank.getCapacity());
    }

    @Override
    public void onPlace() {
        WorldUtils.makeBoundingBlock(getWorld(), getPos().up(), getPos());
    }

    @Override
    public void onBreak(BlockState oldState) {
        World world = getWorld();
        if (world != null) {
            world.removeBlock(getPos().up(), false);
            world.removeBlock(getPos(), false);
        }
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos, pos.add(1, 2, 1));
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(() -> clientEnergyUsed, value -> clientEnergyUsed = value));
    }
}
