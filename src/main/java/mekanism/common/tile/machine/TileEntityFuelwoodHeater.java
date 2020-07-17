package mekanism.common.tile.machine;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.holder.heat.HeatCapacitorHelper;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.FuelInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeHooks;

public class TileEntityFuelwoodHeater extends TileEntityMekanism {

    public int burnTime;
    public int maxBurnTime;

    public double lastEnvironmentLoss;

    private FuelInventorySlot fuelSlot;
    private BasicHeatCapacitor heatCapacitor;

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

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        HeatCapacitorHelper builder = HeatCapacitorHelper.forSide(this::getDirection);
        builder.addCapacitor(heatCapacitor = BasicHeatCapacitor.create(100, 5, 10, this));
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
            heatCapacitor.handleHeat(MekanismConfig.general.heatPerFuelTick.get());
        }
        HeatTransfer loss = simulate();
        lastEnvironmentLoss = loss.getEnvironmentTransfer();
        setActive(burning);
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        burnTime = nbtTags.getInt(NBTConstants.BURN_TIME);
        maxBurnTime = nbtTags.getInt(NBTConstants.MAX_BURN_TIME);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt(NBTConstants.BURN_TIME, burnTime);
        nbtTags.putInt(NBTConstants.MAX_BURN_TIME, maxBurnTime);
        return nbtTags;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> burnTime, value -> burnTime = value));
        container.track(SyncableInt.create(() -> maxBurnTime, value -> maxBurnTime = value));
        container.track(SyncableDouble.create(() -> lastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }
}