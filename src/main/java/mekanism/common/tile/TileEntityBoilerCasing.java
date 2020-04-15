package mekanism.common.tile;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableLong;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityBoilerCasing extends TileEntityMultiblock<SynchronizedBoilerData> {

    /**
     * A client-sided set of valves on this tank's structure that are currently active, used on the client for rendering fluids.
     */
    public Set<ValveData> valveViewing = new ObjectOpenHashSet<>();

    public float prevWaterScale;
    public float prevSteamScale;

    public TileEntityBoilerCasing() {
        this(MekanismBlocks.BOILER_CASING);
    }

    public TileEntityBoilerCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (!clientHasStructure || !isRendering) {
            for (ValveData data : valveViewing) {
                TileEntityBoilerCasing tile = MekanismUtils.getTileEntity(TileEntityBoilerCasing.class, getWorld(), data.location.getPos());
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
            for (ValveData data : structure.valves) {
                if (data.activeTicks > 0) {
                    data.activeTicks--;
                }
                if (data.activeTicks > 0 != data.prevActive) {
                    needsPacket = true;
                }
                data.prevActive = data.activeTicks > 0;
            }

            boolean newHot = structure.getTotalTemperature() >= SynchronizedBoilerData.BASE_BOIL_TEMP - 0.01;
            if (newHot != structure.clientHot) {
                needsPacket = true;
                structure.clientHot = newHot;
                SynchronizedBoilerData.hotMap.put(structure.inventoryID, structure.clientHot);
            }
            // external heat dissipation
            HeatTransfer transfer = structure.simulate();
            // update temperature
            structure.update(null);
            structure.lastEnvironmentLoss = transfer.getEnvironmentTransfer();
            if (structure.getTotalTemperature() >= SynchronizedBoilerData.BASE_BOIL_TEMP && !structure.waterTank.isEmpty()) {
                double heatAvailable = structure.getHeatAvailable();
                structure.lastMaxBoil = (int) Math.floor(heatAvailable / HeatUtils.getVaporizationEnthalpy());

                int amountToBoil = Math.min(structure.lastMaxBoil, structure.waterTank.getFluidAmount());
                amountToBoil = Math.min(amountToBoil, MathUtils.clampToInt(structure.steamTank.getNeeded()));
                if (!structure.waterTank.isEmpty()) {
                    structure.waterTank.shrinkStack(amountToBoil, Action.EXECUTE);
                }
                if (structure.steamTank.isEmpty()) {
                    structure.steamTank.setStack(MekanismGases.STEAM.getGasStack(amountToBoil));
                } else {
                    structure.steamTank.growStack(amountToBoil, Action.EXECUTE);
                }

                structure.handleHeat(-amountToBoil * HeatUtils.getVaporizationEnthalpy());
                structure.lastBoilRate = amountToBoil;
            } else {
                structure.lastBoilRate = 0;
                structure.lastMaxBoil = 0;
            }
            float waterScale = MekanismUtils.getScale(prevWaterScale, structure.waterTank);
            if (waterScale != prevWaterScale) {
                needsPacket = true;
                prevWaterScale = waterScale;
            }
            float steamScale = MekanismUtils.getScale(prevSteamScale, structure.steamTank);
            if (steamScale != prevSteamScale) {
                needsPacket = true;
                prevSteamScale = steamScale;
            }
            if (needsPacket) {
                sendUpdatePacket();
            }
            markDirty(false);
        }
    }

    @Nonnull
    @Override
    public SynchronizedBoilerData getNewStructure() {
        return new SynchronizedBoilerData(this);
    }

    @Override
    protected BoilerUpdateProtocol getProtocol() {
        return new BoilerUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SynchronizedBoilerData> getManager() {
        return Mekanism.boilerManager;
    }

    public double getLastEnvironmentLoss() {
        return structure == null ? 0 : structure.lastEnvironmentLoss;
    }

    public double getTemperature() {
        return structure == null ? 0 : structure.getTotalTemperature();
    }

    public int getLastBoilRate() {
        return structure == null ? 0 : structure.lastBoilRate;
    }

    public int getLastMaxBoil() {
        return structure == null ? 0 : structure.lastMaxBoil;
    }

    public int getSuperheatingElements() {
        return structure == null ? 0 : structure.superheatingElements;
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        return side -> structure == null ? Collections.emptyList() : structure.getHeatCapacitors(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        //Do not handle heat when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.HEAT) {
            return false;
        }
        return super.persists(type);
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (structure != null && isRendering) {
            updateTag.putFloat(NBTConstants.SCALE, prevWaterScale);
            updateTag.putFloat(NBTConstants.SCALE_ALT, prevSteamScale);
            updateTag.putInt(NBTConstants.VOLUME, structure.getWaterVolume());
            updateTag.putLong(NBTConstants.LOWER_VOLUME, structure.getSteamVolume());
            updateTag.put(NBTConstants.FLUID_STORED, structure.waterTank.getFluid().writeToNBT(new CompoundNBT()));
            updateTag.put(NBTConstants.GAS_STORED, structure.steamTank.getStack().write(new CompoundNBT()));
            updateTag.put(NBTConstants.RENDER_Y, structure.upperRenderLocation.write(new CompoundNBT()));
            updateTag.putBoolean(NBTConstants.HOT, structure.clientHot);
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
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT, scale -> prevSteamScale = scale);
            NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> structure.setWaterVolume(value));
            NBTUtils.setLongIfPresent(tag, NBTConstants.LOWER_VOLUME, value -> structure.setSteamVolume(value));
            NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, value -> structure.waterTank.setStack(value));
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> structure.steamTank.setStack(value));
            NBTUtils.setCoord4DIfPresent(tag, NBTConstants.RENDER_Y, value -> structure.upperRenderLocation = value);
            NBTUtils.setBooleanIfPresent(tag, NBTConstants.HOT, value -> structure.clientHot = value);
            valveViewing.clear();
            if (tag.contains(NBTConstants.VALVE, NBT.TAG_LIST)) {
                ListNBT valves = tag.getList(NBTConstants.VALVE, NBT.TAG_COMPOUND);
                for (int i = 0; i < valves.size(); i++) {
                    CompoundNBT valveNBT = valves.getCompound(i);
                    ValveData data = new ValveData();
                    data.location = Coord4D.read(valveNBT);
                    data.side = Direction.byIndex(valveNBT.getInt(NBTConstants.SIDE));
                    valveViewing.add(data);
                    TileEntityBoilerCasing tile = MekanismUtils.getTileEntity(TileEntityBoilerCasing.class, getWorld(), data.location.getPos());
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
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.getWaterVolume(), value -> {
            if (structure != null) {
                structure.setWaterVolume(value);
            }
        }));
        container.track(SyncableLong.create(() -> structure == null ? 0 : structure.getSteamVolume(), value -> {
            if (structure != null) {
                structure.setSteamVolume(value);
            }
        }));
        container.track(SyncableFluidStack.create(() -> structure == null ? FluidStack.EMPTY : structure.waterTank.getFluid(), value -> {
            if (structure != null) {
                structure.waterTank.setStack(value);
            }
        }));
        container.track(SyncableGasStack.create(() -> structure == null ? GasStack.EMPTY : structure.steamTank.getStack(), value -> {
            if (structure != null) {
                structure.steamTank.setStack(value);
            }
        }));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> {
            if (structure != null) {
                structure.lastEnvironmentLoss = value;
            }
        }));
        container.track(SyncableInt.create(this::getLastBoilRate, value -> {
            if (structure != null) {
                structure.lastBoilRate = value;
            }
        }));
        container.track(SyncableInt.create(this::getSuperheatingElements, value -> {
            if (structure != null) {
                structure.superheatingElements = value;
            }
        }));
        container.track(SyncableDouble.create(() -> structure == null ? 0 : structure.heatCapacitor.getHeat(), value -> {
            if (structure != null) {
                structure.heatCapacitor.setHeat(value);
            }
        }));
        container.track(SyncableDouble.create(() -> structure == null ? 0 : structure.heatCapacitor.getHeatCapacity(), value -> {
            if (structure != null) {
                structure.heatCapacitor.setHeatCapacity(value, false);
            }
        }));
        container.track(SyncableInt.create(this::getLastMaxBoil, value -> {
            if (structure != null) {
                structure.lastMaxBoil = value;
            }
        }));
    }
}