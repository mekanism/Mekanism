package mekanism.common.tile.machine;

import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.UnknownNullability;

public class TileEntityFuelwoodHeater extends TileEntityMekanism {

    public static final double HEAT_CAPACITY = 100;
    public static final double INVERSE_CONDUCTION_COEFFICIENT = 5;
    public static final double INVERSE_INSULATION_COEFFICIENT = 10;

    public int burnTime;
    public int maxBurnTime;

    private double lastEnvironmentLoss;
    private double lastTransferLoss;

    @UnknownNullability//Initialized via getInitialInventory
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFuelItem", docPlaceholder = "fuel slot")
    FuelInventorySlot fuelSlot;
    @UnknownNullability//Initialized via getInitialHeatCapacitors
    @WrappingComputerMethod(wrapper = ComputerHeatCapacitorWrapper.class, methodNames = "getTemperature", docPlaceholder = "heater")
    BasicHeatCapacitor heatCapacitor;

    public TileEntityFuelwoodHeater(BlockPos pos, BlockState state) {
        super(MekanismBlocks.FUELWOOD_HEATER, pos, state);
    }

    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(fuelSlot = FuelInventorySlot.forFuel(itemType -> level == null ? 0 : itemType.toStack().getBurnTime(null, level.fuelValues()), listener, 15, 29));
        return builder.build();
    }

    @Override
    protected ISingleContainerHolder<IHeatCapacitor> getInitialHeatCapacitor(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        heatCapacitor = BasicHeatCapacitor.create(HEAT_CAPACITY, INVERSE_CONDUCTION_COEFFICIENT, INVERSE_INSULATION_COEFFICIENT, ambientTemperature, listener);
        return _ -> heatCapacitor;
    }

    @Override
    protected boolean onUpdateServer(ServerLevel level) {
        boolean sendUpdatePacket = super.onUpdateServer(level);
        try (Transaction transaction = Transaction.openRoot()) {
            if (burnTime == 0) {
                maxBurnTime = fuelSlot.burn(level.fuelValues(), transaction);
                burnTime = maxBurnTime;
            }
            setActive(burnTime > 0);
            if (burnTime > 0) {
                int ticks = Math.min(burnTime, MekanismConfig.general.fuelwoodTickMultiplier.get());
                burnTime -= ticks;
                heatCapacitor.handleHeat(MekanismConfig.general.heatPerFuelTick.get() * ticks, transaction);
            }
            HeatTransfer loss = simulate(transaction);
            lastEnvironmentLoss = loss.environmentTransfer();
            lastTransferLoss = loss.adjacentTransfer();
            transaction.commit();
        }
        return sendUpdatePacket;
    }

    public double getTemperature() {
        return heatCapacitor.getTemperature();
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
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        burnTime = input.getIntOr(SerializationConstants.BURN_TIME, burnTime);
        maxBurnTime = input.getIntOr(SerializationConstants.MAX_BURN_TIME, maxBurnTime);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(SerializationConstants.BURN_TIME, burnTime);
        output.putInt(SerializationConstants.MAX_BURN_TIME, maxBurnTime);
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
