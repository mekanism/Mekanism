package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IHeatTransfer;
import mekanism.api.NBTConstants;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.FuelInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityFuelwoodHeater extends TileEntityMekanism implements IHeatTransfer {

    private double temperature;
    public double heatToAbsorb = 0;

    public int burnTime;
    public int maxBurnTime;

    public double lastEnvironmentLoss;

    private FuelInventorySlot fuelSlot;

    public TileEntityFuelwoodHeater() {
        super(MekanismBlocks.FUELWOOD_HEATER);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fuelSlot = FuelInventorySlot.forFuel(ForgeHooks::getBurnTime, this, 15, 29));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        boolean burning = false;
        if (burnTime > 0) {
            burnTime--;
            burning = true;
        } else {
            maxBurnTime = burnTime = fuelSlot.burn();
            if (burnTime > 0) {
                burning = true;
            }
        }
        if (burning) {
            heatToAbsorb += MekanismConfig.general.heatPerFuelTick.get();
        }
        double[] loss = simulateHeat();
        applyTemperatureChange();
        lastEnvironmentLoss = loss[1];
        setActive(burning);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        temperature = nbtTags.getDouble(NBTConstants.TEMPERATURE);
        burnTime = nbtTags.getInt(NBTConstants.BURN_TIME);
        maxBurnTime = nbtTags.getInt(NBTConstants.MAX_BURN_TIME);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble(NBTConstants.TEMPERATURE, temperature);
        nbtTags.putInt(NBTConstants.BURN_TIME, burnTime);
        nbtTags.putInt(NBTConstants.MAX_BURN_TIME, maxBurnTime);
        return nbtTags;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public double getTemp() {
        return temperature;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return 5;
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return 1000;
    }

    @Override
    public void transferHeatTo(double heat) {
        heatToAbsorb += heat;
    }

    @Override
    public double[] simulateHeat() {
        return HeatUtils.simulate(this);
    }

    @Override
    public double applyTemperatureChange() {
        temperature += heatToAbsorb;
        heatToAbsorb = 0;
        return temperature;
    }

    @Nullable
    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        TileEntity adj = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        return MekanismUtils.toOptional(CapabilityUtils.getCapability(adj, Capabilities.HEAT_HANDLER_CAPABILITY, side.getOpposite())).orElse(null);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_HANDLER_CAPABILITY) {
            return Capabilities.HEAT_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getTemp, value -> temperature = value));
        container.track(SyncableInt.create(() -> burnTime, value -> burnTime = value));
        container.track(SyncableInt.create(() -> maxBurnTime, value -> maxBurnTime = value));
        container.track(SyncableDouble.create(() -> lastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }
}