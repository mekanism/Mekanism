package mekanism.common.tile.machine;

import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerHeatCapacitorWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.FuelInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

public class TileEntityFuelwoodHeater extends TileEntityMekanism {

    public int burnTime;
    public int maxBurnTime;

    private double lastEnvironmentLoss;
    private double lastTransferLoss;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFuelItem")
    private FuelInventorySlot fuelSlot;
    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature")
    private BasicHeatCapacitor heatCapacitor;

    public TileEntityFuelwoodHeater(BlockPos pos, BlockState state) {
        super(MekanismBlocks.FUELWOOD_HEATER, pos, state);
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fuelSlot = FuelInventorySlot.forFuel(stack -> ForgeHooks.getBurnTime(stack, null), listener, 15, 29));
        return builder.build();
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        builder.addCapacitor(heatCapacitor = BasicHeatCapacitor.create(100, 5, 10, ambientTemperature, listener));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (burnTime == 0) {
            maxBurnTime = burnTime = fuelSlot.burn();
        }
        if (burnTime > 0) {
            int ticks = Math.min(burnTime, MekanismConfig.general.fuelwoodTickMultiplier.get());
            burnTime -= ticks;
            heatCapacitor.handleHeat(MekanismConfig.general.heatPerFuelTick.get() * ticks);
            setActive(true);
        } else {
            setActive(false);
        }
        HeatTransfer loss = simulate();
        lastEnvironmentLoss = loss.environmentTransfer();
        lastTransferLoss = loss.adjacentTransfer();
    }

    @ComputerMethod(nameOverride = "getTransferLoss")
    public double getLastTransferLoss() {
        return lastTransferLoss;
    }

    @ComputerMethod(nameOverride = "getEnvironmentalLoss")
    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        burnTime = nbt.getInt(NBTConstants.BURN_TIME);
        maxBurnTime = nbt.getInt(NBTConstants.MAX_BURN_TIME);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        nbtTags.putInt(NBTConstants.BURN_TIME, burnTime);
        nbtTags.putInt(NBTConstants.MAX_BURN_TIME, maxBurnTime);
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> burnTime, value -> burnTime = value));
        container.track(SyncableInt.create(() -> maxBurnTime, value -> maxBurnTime = value));
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }
}