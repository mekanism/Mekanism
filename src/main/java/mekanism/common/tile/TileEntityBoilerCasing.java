package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.boiler.BoilerCache;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityBoilerCasing extends TileEntityMultiblock<SynchronizedBoilerData> implements IHeatTransfer {

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

            boolean newHot = structure.temperature >= SynchronizedBoilerData.BASE_BOIL_TEMP - 0.01F;
            if (newHot != structure.clientHot) {
                needsPacket = true;
                structure.clientHot = newHot;
                SynchronizedBoilerData.hotMap.put(structure.inventoryID, structure.clientHot);
            }

            double[] d = structure.simulateHeat();
            structure.applyTemperatureChange();
            structure.lastEnvironmentLoss = d[1];
            if (structure.temperature >= SynchronizedBoilerData.BASE_BOIL_TEMP && !structure.waterTank.isEmpty()) {
                int steamAmount = structure.steamTank.getStored();
                double heatAvailable = structure.getHeatAvailable();

                structure.lastMaxBoil = (int) Math.floor(heatAvailable / SynchronizedBoilerData.getHeatEnthalpy());

                int amountToBoil = Math.min(structure.lastMaxBoil, structure.waterTank.getFluidAmount());
                amountToBoil = Math.min(amountToBoil, structure.steamTank.getCapacity() - steamAmount);
                if (!structure.waterTank.isEmpty()) {
                    structure.waterTank.shrinkStack(amountToBoil, Action.EXECUTE);
                }
                if (structure.steamTank.isEmpty()) {
                    structure.steamTank.setStack(MekanismGases.STEAM.getGasStack(amountToBoil));
                } else {
                    structure.steamTank.growStack(amountToBoil, Action.EXECUTE);
                }

                structure.temperature -= (amountToBoil * SynchronizedBoilerData.getHeatEnthalpy()) / structure.locations.size();
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
            markDirty();
        }
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        return structure == null ? ActionResultType.PASS : openGui(player);
    }

    @Nonnull
    @Override
    protected SynchronizedBoilerData getNewStructure() {
        return new SynchronizedBoilerData(this);
    }

    @Override
    public BoilerCache getNewCache() {
        return new BoilerCache();
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
        return structure == null ? 0 : structure.temperature;
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

    @Override
    public double getTemp() {
        return 0;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return SynchronizedBoilerData.CASING_INVERSE_CONDUCTION_COEFFICIENT;
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return SynchronizedBoilerData.CASING_INSULATION_COEFFICIENT;
    }

    @Override
    public void transferHeatTo(double heat) {
        if (structure != null) {
            structure.heatToAbsorb += heat;
        }
    }

    @Override
    public double[] simulateHeat() {
        return new double[]{0, 0};
    }

    @Override
    public double applyTemperatureChange() {
        return 0;
    }

    //TODO: Decide if heat capability should be moved to valve only
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY && structure == null) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        if (structure != null && isRendering) {
            updateTag.putFloat(NBTConstants.SCALE, prevWaterScale);
            updateTag.putFloat(NBTConstants.SCALE_ALT, prevSteamScale);
            updateTag.putInt(NBTConstants.VOLUME, structure.getWaterVolume());
            updateTag.putInt(NBTConstants.LOWER_VOLUME, structure.getSteamVolume());
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
        if (clientHasStructure && isRendering) {
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevWaterScale = scale);
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT, scale -> prevSteamScale = scale);
            NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> structure.setWaterVolume(value));
            NBTUtils.setIntIfPresent(tag, NBTConstants.LOWER_VOLUME, value -> structure.setSteamVolume(value));
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
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.getSteamVolume(), value -> {
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
        container.track(SyncableDouble.create(this::getTemperature, value -> {
            if (structure != null) {
                structure.temperature = value;
            }
        }));
        container.track(SyncableInt.create(this::getLastMaxBoil, value -> {
            if (structure != null) {
                structure.lastMaxBoil = value;
            }
        }));
    }
}