package mekanism.generators.common.tile.reactor;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.VariableCapacityGasTank;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.EnergyCompatUtils;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.FusionReactor;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.item.ItemHohlraum;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityReactorController extends TileEntityReactorBlock {

    public static final int MAX_WATER = 100 * FluidAttributes.BUCKET_VOLUME;
    public static final long MAX_STEAM = MAX_WATER * 100L;
    public static final long MAX_FUEL = FluidAttributes.BUCKET_VOLUME;

    public BasicEnergyContainer energyContainer;
    public BasicHeatCapacitor heatCapacitor;
    public IExtendedFluidTank waterTank;
    public IChemicalTank<Gas, GasStack> steamTank;
    public IChemicalTank<Gas, GasStack> deuteriumTank;
    public IChemicalTank<Gas, GasStack> tritiumTank;
    public IChemicalTank<Gas, GasStack> fuelTank;
    public double plasmaTemperature;

    private AxisAlignedBB box;
    private double clientTemp = HeatAPI.AMBIENT_TEMP;
    private boolean clientBurning = false;

    private IInventorySlot reactorSlot;

    private int localMaxWater = MAX_WATER;
    private long localMaxSteam = MAX_STEAM;

    public TileEntityReactorController() {
        super(GeneratorsBlocks.REACTOR_CONTROLLER);
    }

    @Nonnull
    @Override
    protected IChemicalTankHolder<Gas, GasStack> getInitialGasTanks() {
        ChemicalTankHelper<Gas, GasStack> builder = ChemicalTankHelper.forSideGas(this::getDirection);
        builder.addTank(deuteriumTank = BasicGasTank.input(MAX_FUEL, gas -> gas.isIn(GeneratorTags.Gases.DEUTERIUM), this));
        builder.addTank(tritiumTank = BasicGasTank.input(MAX_FUEL, gas -> gas.isIn(GeneratorTags.Gases.TRITIUM), this));
        builder.addTank(fuelTank = BasicGasTank.input(MAX_FUEL, gas -> gas.isIn(GeneratorTags.Gases.FUSION_FUEL), this));
        builder.addTank(steamTank = VariableCapacityGasTank.output(() -> localMaxSteam, gas -> gas == MekanismGases.STEAM.getGas(), this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(waterTank = VariableCapacityFluidTank.input(() -> localMaxWater, fluid -> fluid.getFluid().isIn(FluidTags.WATER), this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = BasicEnergyContainer.output(MachineEnergyContainer.validateBlock(this).getStorage(), this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        builder.addCapacitor(heatCapacitor = BasicHeatCapacitor.create(FusionReactor.caseHeatCapacity, FusionReactor.getInverseConductionCoefficient(), FusionReactor.inverseInsulation, this));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //TODO: FIXME, make the slot only "exist" or at least be accessible when the reactor is formed
        builder.addSlot(reactorSlot = BasicInventorySlot.at(stack -> stack.getItem() instanceof ItemHohlraum, this, 80, 39));
        return builder.build();
    }

    @Override
    public boolean handles(SubstanceType type) {
        if (type == SubstanceType.GAS || type == SubstanceType.FLUID || type == SubstanceType.HEAT) {
            return false;
        }
        return super.handles(type);
    }

    public IInventorySlot getReactorSlot() {
        return reactorSlot;
    }

    @Override
    public boolean isFrame() {
        return false;
    }

    public void radiateNeutrons(int neutrons) {
        //future impl
    }

    @Override
    public void formMultiblock(boolean keepBurning) {
        if (getReactor() == null) {
            setReactor(new FusionReactor(this));
        }
        getReactor().formMultiblock(keepBurning);
    }

    public double getPlasmaTemp() {
        return isFormed() ? getReactor().getLastPlasmaTemp() : 0;
    }

    public double getCaseTemp() {
        return isFormed() ? getReactor().getLastCaseTemp() : 0;
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (isFormed()) {
            getReactor().simulateServer();
            if (getReactor().isBurning() != clientBurning || Math.abs(getReactor().getLastPlasmaTemp() - clientTemp) > 1_000_000) {
                clientBurning = getReactor().isBurning();
                clientTemp = getReactor().getLastPlasmaTemp();
                sendUpdatePacket();
            }
        }
    }

    @Override
    protected boolean canPlaySound() {
        return isBurning();
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        tag.putDouble(NBTConstants.PLASMA_TEMP, plasmaTemperature);
        tag.putBoolean(NBTConstants.FORMED, isFormed());
        if (isFormed()) {
            tag.putInt(NBTConstants.INJECTION_RATE, getReactor().getInjectionRate());
            tag.putBoolean(NBTConstants.BURNING, getReactor().isBurning());
        }
        return tag;
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        NBTUtils.setDoubleIfPresent(tag, NBTConstants.PLASMA_TEMP, (temp) -> plasmaTemperature = temp);
        boolean formed = tag.getBoolean(NBTConstants.FORMED);
        if (formed) {
            setReactor(new FusionReactor(this));
            getReactor().setInjectionRate(tag.getInt(NBTConstants.INJECTION_RATE));
            getReactor().setBurning(tag.getBoolean(NBTConstants.BURNING));
            getReactor().updateTemperatures();
        }
    }

    public void updateMaxCapacities(int capRate) {
        localMaxWater = MAX_WATER * capRate;
        localMaxSteam = MAX_STEAM * capRate;
    }

    public void setInjectionRateFromPacket(int rate) {
        if (getReactor() != null) {
            getReactor().setInjectionRate(Math.max(0, rate - (rate % 2)));
            markDirty(false);
        }
    }

    public boolean isFormed() {
        return getReactor() != null && getReactor().isFormed();
    }

    public boolean isBurning() {
        return isFormed() && getReactor().isBurning();
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if (active == (getReactor() == null)) {
            setReactor(active ? new FusionReactor(this) : null);
        }
    }

    @Override
    public boolean renderUpdate() {
        return true;
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (box == null) {
            box = new AxisAlignedBB(getPos().getX() - 1, getPos().getY() - 3, getPos().getZ() - 1, getPos().getX() + 2, getPos().getY(), getPos().getZ() + 2);
        }
        return box;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && !isFormed()) {
            return true;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ||
                   capability == Capabilities.HEAT_HANDLER_CAPABILITY || EnergyCompatUtils.isEnergyCapability(capability)) {
            //Never allow the gas handler, fluid handler, or energy cap to be enabled here even though internally we can handle both of them
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        boolean formed = isFormed();
        updateTag.putBoolean(NBTConstants.HAS_STRUCTURE, formed);
        if (formed) {
            updateTag.putDouble(NBTConstants.PLASMA_TEMP, getPlasmaTemp());
            updateTag.putBoolean(NBTConstants.BURNING, isBurning());
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        boolean formed = tag.getBoolean(NBTConstants.HAS_STRUCTURE);
        FusionReactor reactor = getReactor();
        if (formed) {
            if (reactor == null || !reactor.isFormed()) {
                BlockPos corner = getPos().subtract(new Vec3i(2, 4, 2));
                Mekanism.proxy.doMultiblockSparkle(this, corner, 5, 5, 6, tile -> tile instanceof TileEntityReactorBlock);
            }
            if (reactor == null) {
                setReactor(reactor = new FusionReactor(this));
            }
            reactor.formed = true;
            NBTUtils.setDoubleIfPresent(tag, NBTConstants.PLASMA_TEMP, reactor::setLastPlasmaTemp);
            NBTUtils.setBooleanIfPresent(tag, NBTConstants.BURNING, reactor::setBurning);
        } else if (reactor != null) {
            setReactor(null);
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getPlasmaTemp, value -> {
            if (getReactor() != null) {
                getReactor().setPlasmaTemp(value);
                getReactor().setLastPlasmaTemp(value);
            }
        }));
        container.track(SyncableDouble.create(this::getCaseTemp, value -> {
            if (getReactor() != null) {
                getReactor().setLastCaseTemp(value);
            }
        }));
    }

    public void addFuelTabContainerTrackers(MekanismContainer container) {
        container.track(SyncableInt.create(() -> getReactor() == null ? 0 : getReactor().getInjectionRate(), value -> {
            if (getReactor() != null) {
                getReactor().setInjectionRate(value);
            }
        }));
        container.track(SyncableGasStack.create(fuelTank));
        container.track(SyncableGasStack.create(deuteriumTank));
        container.track(SyncableGasStack.create(tritiumTank));
    }

    public void addHeatTabContainerTrackers(MekanismContainer container) {
        container.track(SyncableFluidStack.create(waterTank));
        container.track(SyncableGasStack.create(steamTank));
        container.track(SyncableInt.create(() -> localMaxWater, (val) -> localMaxWater = val));
        container.track(SyncableLong.create(() -> localMaxSteam, (val) -> localMaxSteam = val));
    }
}