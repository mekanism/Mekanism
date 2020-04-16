package mekanism.generators.common.tile.fission;

import java.util.Set;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.FissionReactorUpdateProtocol;
import mekanism.generators.common.content.fission.SynchronizedFissionReactorData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityFissionReactorCasing extends TileEntityMultiblock<SynchronizedFissionReactorData> {

    public Set<ValveData> valveViewing = new ObjectOpenHashSet<>();
    public float prevWaterScale, prevFuelScale, prevSteamScale, prevWasteScale;

    public TileEntityFissionReactorCasing() {
        super(GeneratorsBlocks.FISSION_REACTOR_CASING);
    }

    public TileEntityFissionReactorCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (!clientHasStructure || !isRendering) {
            for (ValveData data : valveViewing) {
                TileEntityFissionReactorCasing tile = MekanismUtils.getTileEntity(TileEntityFissionReactorCasing.class, getWorld(), data.location.getPos());
                if (tile != null) {
                    tile.clientHasStructure = false;
                }
            }
            valveViewing.clear();
        }
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && isRendering) {
            boolean needsPacket = false;
            // burn reactor fuel, create energy
            if (structure.active) {
                structure.burnFuel();
            } else {
                structure.lastBurnRate = 0;
            }
            if ((structure.lastBurnRate > 0) != structure.clientBurning) {
                needsPacket = true;
                structure.clientBurning = structure.lastBurnRate > 0;
                SynchronizedFissionReactorData.burningMap.put(structure.inventoryID, structure.clientBurning);
            }
            // handle coolant heating (water -> steam)
            structure.handleCoolant();
            // external heat dissipation
            structure.lastEnvironmentLoss = structure.simulateEnvironment();
            // adjacent heat transfer
            structure.lastTransferLoss = 0;
            for (ValveData valve : structure.valves) {
                TileEntity tile = world.getTileEntity(valve.location.getPos());
                if (tile instanceof ITileHeatHandler) {
                    structure.lastTransferLoss += ((ITileHeatHandler) tile).simulateAdjacent();
                }
            }
            // update temperature
            structure.update(null);
            structure.handleDamage();

            // update scales
            float waterScale = MekanismUtils.getScale(prevWaterScale, structure.waterTank), fuelScale = MekanismUtils.getScale(prevFuelScale, structure.fuelTank);
            float steamScale = MekanismUtils.getScale(prevSteamScale, structure.steamTank), wasteScale = MekanismUtils.getScale(prevWasteScale, structure.wasteTank);
            if (waterScale != prevWaterScale || fuelScale != prevFuelScale || steamScale != prevSteamScale || wasteScale != prevWasteScale) {
                needsPacket = true;
                prevWaterScale = waterScale;
                prevFuelScale = fuelScale;
                prevSteamScale = steamScale;
                prevWasteScale = wasteScale;
            }
            if (needsPacket) {
                sendUpdatePacket();
            }
            // save changed data
            markDirty(false);
        }
    }

    public double getLastEnvironmentLoss() { return structure != null ? structure.lastEnvironmentLoss : 0; }
    public double getLastTransferLoss() { return structure != null ? structure.lastTransferLoss : 0; }
    public double getTemperature() { return structure != null ? structure.heatCapacitor.getTemperature() : 0; }
    public long getHeatCapacity() { return structure != null ? Math.round(structure.heatCapacitor.getHeatCapacity()) : 0; }
    public long getSurfaceArea() { return structure != null ? structure.surfaceArea : 0; }
    public double getBoilEfficiency() { return structure != null ? (double) Math.round(structure.getBoilEfficiency() * 1000) / 1000 : 0; }
    public long getLastBoilRate() { return structure != null ? structure.lastBoilRate : 0; }
    public long getLastBurnRate() { return structure != null ? structure.lastBurnRate : 0; }
    public long getMaxBurnRate() { return structure != null ? structure.fuelAssemblies * SynchronizedFissionReactorData.BURN_PER_ASSEMBLY : 1; }
    public long getRateLimit() { return structure != null ? structure.rateLimit : 0; }
    public boolean isReactorActive() { return structure != null ? structure.active : false; }
    public void setReactorActive(boolean active) { if (structure != null) structure.active = active; }

    public String getDamageString() {
        if (structure == null) return "0%";
        return Math.round((structure.reactorDamage / SynchronizedFissionReactorData.MAX_DAMAGE) * 100) + "%";
    }
    public EnumColor getDamageColor() {
        if (structure == null) return EnumColor.BRIGHT_GREEN;
        double damage = structure.reactorDamage / SynchronizedFissionReactorData.MAX_DAMAGE;
        return damage < 0.25 ? EnumColor.BRIGHT_GREEN : (damage < 0.5 ? EnumColor.YELLOW : (damage < 0.75 ? EnumColor.ORANGE : EnumColor.DARK_RED));
    }
    public EnumColor getTempColor() {
        double temp = getTemperature();
        return temp < 600 ? EnumColor.BRIGHT_GREEN : (temp < 1000 ? EnumColor.YELLOW :
              (temp < 1200 ? EnumColor.ORANGE : (temp < 1600 ? EnumColor.RED : EnumColor.DARK_RED)));
    }

    public void setRateLimitFromPacket(int rate) {
        if (structure != null) {
            structure.rateLimit = Math.min(getMaxBurnRate(), rate);
        }
    }

    @Override
    public SynchronizedFissionReactorData getNewStructure() {
        return new SynchronizedFissionReactorData(this);
    }

    @Override
    protected UpdateProtocol<SynchronizedFissionReactorData> getProtocol() {
        return new FissionReactorUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SynchronizedFissionReactorData> getManager() {
        return MekanismGenerators.fissionReactorManager;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (structure != null && isRendering) {
            updateTag.putFloat(NBTConstants.SCALE, prevWaterScale);
            updateTag.putFloat(NBTConstants.SCALE_ALT, prevFuelScale);
            updateTag.putFloat(NBTConstants.SCALE_ALT_2, prevSteamScale);
            updateTag.putFloat(NBTConstants.SCALE_ALT_3, prevWasteScale);
            updateTag.putInt(NBTConstants.VOLUME, structure.getVolume());
            updateTag.put(NBTConstants.FLUID_STORED, structure.waterTank.getFluid().writeToNBT(new CompoundNBT()));
            updateTag.put(NBTConstants.GAS_STORED, structure.fuelTank.getStack().write(new CompoundNBT()));
            updateTag.put(NBTConstants.GAS_STORED_ALT, structure.steamTank.getStack().write(new CompoundNBT()));
            updateTag.put(NBTConstants.GAS_STORED_ALT_2, structure.wasteTank.getStack().write(new CompoundNBT()));
            ListNBT valves = new ListNBT();
            for (ValveData valveData : structure.valves) {
                if (valveData.activeTicks > 0) {
                    CompoundNBT valveNBT = new CompoundNBT();
                    valveData.location.write(valveNBT);
                    valveNBT.putInt(NBTConstants.SIDE, valveData.side.ordinal());
                }
            }
            updateTag.put(NBTConstants.VALVE, valves);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (clientHasStructure && isRendering && structure != null) {
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevWaterScale = scale);
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT, scale -> prevFuelScale = scale);
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT_2, scale -> prevSteamScale = scale);
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT_3, scale -> prevWasteScale = scale);
            NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> structure.setVolume(value));
            NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, value -> structure.waterTank.setStack(value));
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> structure.fuelTank.setStack(value));
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED_ALT, value -> structure.steamTank.setStack(value));
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED_ALT_2, value -> structure.wasteTank.setStack(value));
            valveViewing.clear();
            if (tag.contains(NBTConstants.VALVE, NBT.TAG_LIST)) {
                ListNBT valves = tag.getList(NBTConstants.VALVE, NBT.TAG_COMPOUND);
                for (int i = 0; i < valves.size(); i++) {
                    CompoundNBT valveNBT = valves.getCompound(i);
                    ValveData data = new ValveData();
                    data.location = Coord4D.read(valveNBT);
                    data.side = Direction.byIndex(valveNBT.getInt(NBTConstants.SIDE));
                    valveViewing.add(data);
                    TileEntityFissionReactorCasing tile = MekanismUtils.getTileEntity(TileEntityFissionReactorCasing.class, getWorld(), data.location.getPos());
                    if (tile != null) {
                        tile.clientHasStructure = true;
                    }
                }
            }
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.getVolume(), value -> {
            if (structure != null) structure.setVolume(value);
        }));
        container.track(SyncableFluidStack.create(() -> structure == null ? FluidStack.EMPTY : structure.waterTank.getFluid(), value -> {
            if (structure != null) structure.waterTank.setStack(value);
        }));
        container.track(SyncableGasStack.create(() -> structure == null ? GasStack.EMPTY : structure.fuelTank.getStack(), value -> {
            if (structure != null) structure.fuelTank.setStack(value);
        }));
        container.track(SyncableGasStack.create(() -> structure == null ? GasStack.EMPTY : structure.steamTank.getStack(), value -> {
            if (structure != null) structure.steamTank.setStack(value);
        }));
        container.track(SyncableGasStack.create(() -> structure == null ? GasStack.EMPTY : structure.wasteTank.getStack(), value -> {
            if (structure != null) structure.wasteTank.setStack(value);
        }));
        container.track(SyncableDouble.create(() -> structure == null ? 0 : structure.heatCapacitor.getHeat(), value -> {
            if (structure != null) structure.heatCapacitor.setHeat(value);
        }));
        container.track(SyncableDouble.create(() -> structure == null ? 0 : structure.heatCapacitor.getHeatCapacity(), value -> {
            if (structure != null) structure.heatCapacitor.setHeatCapacity(value, false);
        }));
        container.track(SyncableLong.create(this::getLastBoilRate, value -> {
            if (structure != null) structure.lastBoilRate = value;
        }));
        container.track(SyncableBoolean.create(this::isReactorActive, value -> {
            if (structure != null) structure.active = value;
        }));
        container.track(SyncableDouble.create(() -> structure == null ? 0 : structure.reactorDamage, value -> {
            if (structure != null) structure.reactorDamage = value;
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.fuelAssemblies, value -> {
            if (structure != null) structure.fuelAssemblies = value;
        }));
        container.track(SyncableLong.create(() -> structure == null ? 0 : structure.lastBurnRate, value -> {
            if (structure != null) structure.lastBurnRate = value;
        }));
        container.track(SyncableLong.create(() -> structure == null ? 0 : structure.rateLimit, value -> {
            if (structure != null) structure.rateLimit = value;
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.surfaceArea, value -> {
            if (structure != null) structure.surfaceArea = value;
        }));
    }
}
