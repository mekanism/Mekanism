package mekanism.common.tile;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.attribute.GasAttributes.HeatedCoolant;
import mekanism.api.heat.HeatAPI.HeatTransfer;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.container.sync.SyncableGasStack;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.multiblock.IValveHandler;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityBoilerCasing extends TileEntityMultiblock<BoilerMultiblockData> implements IValveHandler {

    public float prevWaterScale;
    public float prevSteamScale;

    public TileEntityBoilerCasing() {
        this(MekanismBlocks.BOILER_CASING);
    }

    public TileEntityBoilerCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && isRendering) {
            boolean needsPacket = needsValveUpdate();
            boolean newHot = structure.getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP - 0.01;
            if (newHot != structure.clientHot) {
                needsPacket = true;
                structure.clientHot = newHot;
                BoilerMultiblockData.hotMap.put(structure.inventoryID, structure.clientHot);
            }
            // external heat dissipation
            HeatTransfer transfer = structure.simulate();
            // update temperature
            structure.update(null);
            structure.lastEnvironmentLoss = transfer.getEnvironmentTransfer();
            // handle coolant heat transfer
            if (!structure.superheatedCoolantTank.isEmpty()) {
                HeatedCoolant coolantType = structure.superheatedCoolantTank.getStack().get(HeatedCoolant.class);
                if (coolantType != null) {
                    long toCool = Math.round(BoilerMultiblockData.COOLANT_COOLING_EFFICIENCY * structure.superheatedCoolantTank.getStored());
                    GasStack cooledCoolant = coolantType.getCooledGas().getGasStack(toCool);
                    toCool = Math.min(toCool, toCool - structure.cooledCoolantTank.insertGas(cooledCoolant, Action.EXECUTE).getAmount());

                    if (toCool > 0) {
                        double heatEnergy = toCool * coolantType.getThermalEnthalpy();
                        structure.heatCapacitor.handleHeat(heatEnergy);
                        structure.superheatedCoolantTank.shrinkStack(toCool, Action.EXECUTE);
                    }
                }
            }
            // handle water heat transfer
            if (structure.getTotalTemperature() >= HeatUtils.BASE_BOIL_TEMP && !structure.waterTank.isEmpty()) {
                double heatAvailable = structure.getHeatAvailable();
                structure.lastMaxBoil = (int) Math.floor(HeatUtils.getFluidThermalEfficiency() * heatAvailable / HeatUtils.getWaterThermalEnthalpy());

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

                structure.handleHeat(-amountToBoil * HeatUtils.getWaterThermalEnthalpy() / HeatUtils.getFluidThermalEfficiency());
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
    public BoilerMultiblockData getNewStructure() {
        return new BoilerMultiblockData(this);
    }

    @Override
    public BoilerUpdateProtocol getProtocol() {
        return new BoilerUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<BoilerMultiblockData> getManager() {
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

    @Override
    public Collection<ValveData> getValveData() {
        return structure != null ? structure.valves : null;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (structure != null && isRendering) {
            updateTag.putFloat(NBTConstants.SCALE, prevWaterScale);
            updateTag.putFloat(NBTConstants.SCALE_ALT, prevSteamScale);
            updateTag.putInt(NBTConstants.VOLUME, structure.getWaterVolume());
            updateTag.putInt(NBTConstants.LOWER_VOLUME, structure.getSteamVolume());
            updateTag.put(NBTConstants.FLUID_STORED, structure.waterTank.getFluid().writeToNBT(new CompoundNBT()));
            updateTag.put(NBTConstants.GAS_STORED, structure.steamTank.getStack().write(new CompoundNBT()));
            updateTag.put(NBTConstants.RENDER_Y, structure.upperRenderLocation.write(new CompoundNBT()));
            updateTag.putBoolean(NBTConstants.HOT, structure.clientHot);
            writeValves(updateTag);
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
            NBTUtils.setIntIfPresent(tag, NBTConstants.LOWER_VOLUME, value -> structure.setSteamVolume(value));
            NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, value -> structure.waterTank.setStack(value));
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> structure.steamTank.setStack(value));
            NBTUtils.setCoord4DIfPresent(tag, NBTConstants.RENDER_Y, value -> structure.upperRenderLocation = value);
            NBTUtils.setBooleanIfPresent(tag, NBTConstants.HOT, value -> structure.clientHot = value);
            readValves(tag);
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
        container.track(SyncableGasStack.create(() -> structure == null ? GasStack.EMPTY : structure.superheatedCoolantTank.getStack(), value -> {
            if (structure != null) {
                structure.superheatedCoolantTank.setStack(value);
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
        container.track(SyncableGasStack.create(() -> structure == null ? GasStack.EMPTY : structure.cooledCoolantTank.getStack(), value -> {
            if (structure != null) {
                structure.cooledCoolantTank.setStack(value);
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