package mekanism.common.tile;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ElectrolysisCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.base.ITankManager;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.GasInventorySlot;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileCachedRecipeHolder;
import mekanism.common.util.GasUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityElectrolyticSeparator extends TileEntityMekanism implements ITankManager, ITileCachedRecipeHolder<ElectrolysisRecipe> {

    /**
     * This separator's water slot.
     */
    public BasicFluidTank fluidTank;
    /**
     * The maximum amount of gas this block can store.
     */
    private static final int MAX_GAS = 2_400;
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
    public int output = 512;
    /**
     * The type of gas this block is outputting.
     */
    public GasMode dumpLeft = GasMode.IDLE;
    /**
     * Type type of gas this block is dumping.
     */
    public GasMode dumpRight = GasMode.IDLE;
    public CachedRecipe<ElectrolysisRecipe> cachedRecipe;
    public double clientEnergyUsed;

    private final IOutputHandler<@NonNull Pair<GasStack, GasStack>> outputHandler;
    private final IInputHandler<@NonNull FluidStack> inputHandler;

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
        builder.addTank(fluidTank = BasicFluidTank.input(24_000, fluid -> containsRecipe(recipe -> recipe.getInput().testType(fluid)), this), RelativeSide.FRONT, RelativeSide.BACK);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGas(this::getDirection);
        builder.addTank(leftTank = BasicGasTank.output(MAX_GAS, this), RelativeSide.LEFT);
        builder.addTank(rightTank = BasicGasTank.output(MAX_GAS, this), RelativeSide.RIGHT);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fluidSlot = FluidInventorySlot.fill(fluidTank, this, 26, 35), RelativeSide.FRONT);
        builder.addSlot(leftOutputSlot = GasInventorySlot.drain(leftTank, this, 59, 52), RelativeSide.LEFT);
        builder.addSlot(rightOutputSlot = GasInventorySlot.drain(rightTank, this, 101, 52), RelativeSide.RIGHT);
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 143, 35), RelativeSide.BACK);
        fluidSlot.setSlotType(ContainerSlotType.INPUT);
        leftOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
        rightOutputSlot.setSlotType(ContainerSlotType.OUTPUT);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            energySlot.discharge(this);
            fluidSlot.fillTank();

            leftOutputSlot.drainTank();
            rightOutputSlot.drainTank();
            double prev = getEnergy();
            cachedRecipe = getUpdatedCache(0);
            if (cachedRecipe != null) {
                cachedRecipe.process();
            }
            //Update amount of energy that actually got used, as if we are "near" full we may not have performed our max number of operations
            clientEnergyUsed = prev - getEnergy();

            int dumpAmount = 8 * (int) Math.pow(2, upgradeComponent.getUpgrades(Upgrade.SPEED));
            handleTank(leftTank, dumpLeft, getLeftSide(), dumpAmount);
            handleTank(rightTank, dumpRight, getRightSide(), dumpAmount);
        }
    }

    private void handleTank(IChemicalTank<Gas, GasStack> tank, GasMode mode, Direction side, int dumpAmount) {
        if (!tank.isEmpty()) {
            if (mode == GasMode.DUMPING) {
                tank.shrinkStack(dumpAmount, Action.EXECUTE);
            } else {
                GasStack toSend = new GasStack(tank.getStack(), Math.min(tank.getStored(), output));
                tank.shrinkStack(GasUtils.emit(toSend, this, EnumSet.of(side)), Action.EXECUTE);
                if (mode == GasMode.DUMPING_EXCESS) {
                    int needed = tank.getNeeded();
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
              .setEnergyRequirements(() -> getEnergyPerTick() * recipe.getEnergyMultiplier(), this::getEnergy, energy -> setEnergy(getEnergy() - energy))
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
    public void recalculateUpgrades(Upgrade upgrade) {
        //Don't call super as we no-op speed upgrades as it is used for batch speed upgrades, and should not be changing the total amount produced
        // and only increase the max energy storage for the separator not also decrease energy per tick
        if (upgrade == Upgrade.ENERGY) {
            setMaxEnergy(MekanismUtils.getMaxEnergy(this, getBaseStorage()));
            setEnergy(Math.min(getMaxEnergy(), getEnergy()));
        }
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            byte type = dataStream.readByte();
            if (type == 0) {
                dumpLeft = dumpLeft.getNext();
            } else if (type == 1) {
                dumpRight = dumpRight.getNext();
            }
        } else {
            super.handlePacketData(dataStream);
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        dumpLeft = GasMode.byIndexStatic(nbtTags.getInt("dumpLeft"));
        dumpRight = GasMode.byIndexStatic(nbtTags.getInt("dumpRight"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("dumpLeft", dumpLeft.ordinal());
        nbtTags.putInt("dumpRight", dumpRight.ordinal());
        return nbtTags;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            //TODO: Make this just check the specific sides I think it is a shorter list?
            return side != null && side != getDirection() && side != getOppositeDirection() && side != getRightSide();
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public Object[] getManagedTanks() {
        return new Object[]{fluidTank, leftTank, rightTank};
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
        container.track(SyncableDouble.create(() -> clientEnergyUsed, value -> clientEnergyUsed = value));
    }
}