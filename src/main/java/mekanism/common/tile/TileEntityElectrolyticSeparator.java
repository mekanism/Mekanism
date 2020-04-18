package mekanism.common.tile;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
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
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

public class TileEntityElectrolyticSeparator extends TileEntityMekanism implements ITileCachedRecipeHolder<ElectrolysisRecipe>, IHasGasMode {

    /**
     * This separator's water slot.
     */
    public BasicFluidTank fluidTank;
    /**
     * The maximum amount of gas this block can store.
     */
    private static final long MAX_GAS = 2_400;
    /**
     * The amount of oxygen this block is storing.
     */
    public BasicGasTank leftTank;
    /**
     * The amount of hydrogen this block is storing.
     */
    public BasicGasTank rightTank;
    /**
     * How fast this block can output gas.
     */
    public long output = 512;
    /**
     * The type of gas this block is outputting.
     */
    public GasMode dumpLeft = GasMode.IDLE;
    /**
     * Type type of gas this block is dumping.
     */
    public GasMode dumpRight = GasMode.IDLE;
    public CachedRecipe<ElectrolysisRecipe> cachedRecipe;
    public FloatingLong clientEnergyUsed = FloatingLong.ZERO;

    private final IOutputHandler<@NonNull Pair<GasStack, GasStack>> outputHandler;
    private final IInputHandler<@NonNull FluidStack> inputHandler;

    private ElectrolyticSeparatorEnergyContainer energyContainer;
    private FluidInventorySlot fluidSlot;
    private GasInventorySlot leftOutputSlot;
    private GasInventorySlot rightOutputSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityElectrolyticSeparator() {
        super(MekanismBlocks.ELECTROLYTIC_SEPARATOR);
        inputHandler = InputHelper.getInputHandler(fluidTank);
        outputHandler = OutputHelper.getOutputHandler(leftTank, rightTank);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(fluidTank = BasicFluidTank.input(24_000, fluid -> containsRecipe(recipe -> recipe.getInput().testType(fluid)), this),
              RelativeSide.FRONT, RelativeSide.BACK);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGas(this::getDirection);
        builder.addTank(leftTank = BasicGasTank.output(MAX_GAS, this), RelativeSide.LEFT);
        builder.addTank(rightTank = BasicGasTank.output(MAX_GAS, this), RelativeSide.RIGHT);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = ElectrolyticSeparatorEnergyContainer.input(this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fluidSlot = FluidInventorySlot.fill(fluidTank, this, 26, 35), RelativeSide.FRONT);
        builder.addSlot(leftOutputSlot = GasInventorySlot.drain(leftTank, this, 59, 52), RelativeSide.LEFT);
        builder.addSlot(rightOutputSlot = GasInventorySlot.drain(rightTank, this, 101, 52), RelativeSide.RIGHT);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 143, 35), RelativeSide.BACK);
        fluidSlot.setSlotType(ContainerSlotType.INPUT);
        leftOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
        rightOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        fluidSlot.fillTank();

        leftOutputSlot.drainTank();
        rightOutputSlot.drainTank();
        FloatingLong prev = energyContainer.getEnergy().copyAsConst();
        CachedRecipe<ElectrolysisRecipe> oldCache = cachedRecipe;
        cachedRecipe = getUpdatedCache(0);
        if (oldCache != cachedRecipe) {
            //If it is not the same literal object ensure we take our recipe's energy per tick into account
            energyContainer.updateEnergyPerTick();
        }
        if (cachedRecipe != null) {
            cachedRecipe.process();
        }
        //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
        clientEnergyUsed = prev.subtract(energyContainer.getEnergy());

        long dumpAmount = 8 * (long) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
        handleTank(leftTank, dumpLeft, getLeftSide(), dumpAmount);
        handleTank(rightTank, dumpRight, getRightSide(), dumpAmount);
    }

    private void handleTank(IGasTank tank, GasMode mode, Direction side, long dumpAmount) {
        if (!tank.isEmpty()) {
            if (mode == GasMode.DUMPING) {
                tank.shrinkStack(dumpAmount, Action.EXECUTE);
            } else {
                GasUtils.emit(EnumSet.of(side), tank, this, output);
                if (mode == GasMode.DUMPING_EXCESS) {
                    long needed = tank.getNeeded();
                    if (needed < output) {
                        tank.shrinkStack(output - needed, Action.EXECUTE);
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public MekanismRecipeType<ElectrolysisRecipe> getRecipeType() {
        return MekanismRecipeType.SEPARATING;
    }

    @Nullable
    @Override
    public CachedRecipe<ElectrolysisRecipe> getCachedRecipe(int cacheIndex) {
        return cachedRecipe;
    }

    @Nullable
    @Override
    public ElectrolysisRecipe getRecipe(int cacheIndex) {
        FluidStack fluid = inputHandler.getInput();
        if (fluid.isEmpty()) {
            return null;
        }
        return findFirstRecipe(recipe -> recipe.test(fluid));
    }

    @Nullable
    @Override
    public CachedRecipe<ElectrolysisRecipe> createNewCachedRecipe(@Nonnull ElectrolysisRecipe recipe, int cacheIndex) {
        return new ElectrolysisCachedRecipe(recipe, inputHandler, outputHandler)
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
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.DUMP_LEFT, GasMode::byIndexStatic, mode -> dumpLeft = mode);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.DUMP_RIGHT, GasMode::byIndexStatic, mode -> dumpRight = mode);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.DUMP_LEFT, dumpLeft.ordinal());
        nbtTags.putInt(NBTConstants.DUMP_RIGHT, dumpRight.ordinal());
        return nbtTags;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableEnum.create(GasMode::byIndexStatic, GasMode.IDLE, () -> dumpLeft, value -> dumpLeft = value));
        container.track(SyncableEnum.create(GasMode::byIndexStatic, GasMode.IDLE, () -> dumpRight, value -> dumpRight = value));
        container.track(SyncableFloatingLong.create(() -> clientEnergyUsed, value -> clientEnergyUsed = value));
    }
}