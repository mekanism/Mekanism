package mekanism.common.tile;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.TileNetworkList;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.boiler.BoilerCache;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

public class TileEntityBoilerCasing extends TileEntityMultiblock<SynchronizedBoilerData> implements IHeatTransfer {

    /**
     * A client-sided set of valves on this tank's structure that are currently active, used on the client for rendering fluids.
     */
    public Set<ValveData> valveViewing = new HashSet<>();

    /**
     * The capacity this tank has on the client-side.
     */
    public int clientWaterCapacity;
    public int clientSteamCapacity;

    public float prevWaterScale;

    public TileEntityBoilerCasing() {
        this(MekanismBlocks.BOILER_CASING);
    }

    public TileEntityBoilerCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (isRemote()) {
            if (structure != null && clientHasStructure && isRendering) {
                float targetScale = (float) structure.waterStored.getAmount() / clientWaterCapacity;
                if (Math.abs(prevWaterScale - targetScale) > 0.01) {
                    prevWaterScale = (9 * prevWaterScale + targetScale) / 10;
                }
            }
            if (!clientHasStructure || !isRendering) {
                for (ValveData data : valveViewing) {
                    TileEntityBoilerCasing tile = MekanismUtils.getTileEntity(TileEntityBoilerCasing.class, getWorld(), data.location.getPos());
                    if (tile != null) {
                        tile.clientHasStructure = false;
                    }
                }
                valveViewing.clear();
            }
        } else {
            if (structure != null) {
                if (isRendering) {
                    boolean needsValveUpdate = false;
                    for (ValveData data : structure.valves) {
                        if (data.activeTicks > 0) {
                            data.activeTicks--;
                        }
                        if (data.activeTicks > 0 != data.prevActive) {
                            needsValveUpdate = true;
                        }
                        data.prevActive = data.activeTicks > 0;
                    }

                    boolean needsHotUpdate = false;
                    boolean newHot = structure.temperature >= SynchronizedBoilerData.BASE_BOIL_TEMP - 0.01F;
                    if (newHot != structure.clientHot) {
                        needsHotUpdate = true;
                        structure.clientHot = newHot;
                    }

                    double[] d = structure.simulateHeat();
                    structure.applyTemperatureChange();
                    structure.lastEnvironmentLoss = d[1];
                    if (structure.temperature >= SynchronizedBoilerData.BASE_BOIL_TEMP && !structure.waterStored.isEmpty()) {
                        int steamAmount = structure.steamStored.getAmount();
                        double heatAvailable = structure.getHeatAvailable();

                        structure.lastMaxBoil = (int) Math.floor(heatAvailable / SynchronizedBoilerData.getHeatEnthalpy());

                        int amountToBoil = Math.min(structure.lastMaxBoil, structure.waterStored.getAmount());
                        amountToBoil = Math.min(amountToBoil, (structure.steamVolume * BoilerUpdateProtocol.STEAM_PER_TANK) - steamAmount);
                        structure.waterStored.setAmount(structure.waterStored.getAmount() - amountToBoil);
                        if (structure.steamStored.isEmpty()) {
                            structure.steamStored = MekanismFluids.STEAM.getFluidStack(amountToBoil);
                        } else {
                            structure.steamStored.setAmount(structure.steamStored.getAmount() + amountToBoil);
                        }

                        structure.temperature -= (amountToBoil * SynchronizedBoilerData.getHeatEnthalpy()) / structure.locations.size();
                        structure.lastBoilRate = amountToBoil;
                    } else {
                        structure.lastBoilRate = 0;
                        structure.lastMaxBoil = 0;
                    }
                    if (needsValveUpdate || structure.needsRenderUpdate() || needsHotUpdate) {
                        sendPacketToRenderer();
                    }
                    structure.prevWater = structure.waterStored.isEmpty() ? FluidStack.EMPTY : structure.waterStored.copy();
                    structure.prevSteam = structure.steamStored.isEmpty() ? FluidStack.EMPTY : structure.steamStored.copy();
                    MekanismUtils.saveChunk(this);
                }
            }
        }
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (structure == null) {
            return ActionResultType.PASS;
        }
        return openGui(player);
    }

    @Override
    protected SynchronizedBoilerData getNewStructure() {
        return new SynchronizedBoilerData();
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

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        if (structure != null) {
            data.add(structure.waterVolume * BoilerUpdateProtocol.WATER_PER_TANK);
            data.add(structure.steamVolume * BoilerUpdateProtocol.STEAM_PER_TANK);
            data.add(structure.lastEnvironmentLoss);
            data.add(structure.lastBoilRate);
            data.add(structure.superheatingElements);
            data.add(structure.temperature);
            data.add(structure.lastMaxBoil);

            TileUtils.addFluidStack(data, structure.waterStored);
            TileUtils.addFluidStack(data, structure.steamStored);

            structure.upperRenderLocation.write(data);

            if (isRendering) {
                data.add(structure.clientHot);
                Set<ValveData> toSend = new HashSet<>();
                for (ValveData valveData : structure.valves) {
                    if (valveData.activeTicks > 0) {
                        toSend.add(valveData);
                    }
                }
                data.add(toSend.size());
                for (ValveData valveData : toSend) {
                    valveData.location.write(data);
                    data.add(valveData.side.ordinal());
                }
            }
        }
        return data;
    }

    public int getScaledWaterLevel(int i) {
        if (clientWaterCapacity == 0 || structure.waterStored.isEmpty()) {
            return 0;
        }
        return structure.waterStored.getAmount() * i / clientWaterCapacity;
    }

    public int getScaledSteamLevel(int i) {
        if (clientSteamCapacity == 0 || structure.steamStored.isEmpty()) {
            return 0;
        }
        return structure.steamStored.getAmount() * i / clientSteamCapacity;
    }

    public double getLastEnvironmentLoss() {
        return structure != null ? structure.lastEnvironmentLoss : 0;
    }

    public double getTemperature() {
        return structure != null ? structure.temperature : 0;
    }

    public int getLastBoilRate() {
        return structure != null ? structure.lastBoilRate : 0;
    }

    public int getLastMaxBoil() {
        return structure != null ? structure.lastMaxBoil : 0;
    }

    public int getSuperheatingElements() {
        return structure != null ? structure.superheatingElements : 0;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);

        if (isRemote()) {
            if (clientHasStructure) {
                clientWaterCapacity = dataStream.readInt();
                clientSteamCapacity = dataStream.readInt();
                structure.lastEnvironmentLoss = dataStream.readDouble();
                structure.lastBoilRate = dataStream.readInt();
                structure.superheatingElements = dataStream.readInt();
                structure.temperature = dataStream.readDouble();
                structure.lastMaxBoil = dataStream.readInt();

                structure.waterStored = TileUtils.readFluidStack(dataStream);
                structure.steamStored = TileUtils.readFluidStack(dataStream);

                structure.upperRenderLocation = Coord4D.read(dataStream);

                if (isRendering) {
                    structure.clientHot = dataStream.readBoolean();
                    SynchronizedBoilerData.clientHotMap.put(structure.inventoryID, structure.clientHot);
                    int size = dataStream.readInt();
                    valveViewing.clear();
                    for (int i = 0; i < size; i++) {
                        ValveData data = new ValveData();
                        data.location = Coord4D.read(dataStream);
                        data.side = Direction.byIndex(dataStream.readInt());

                        valveViewing.add(data);

                        TileEntityBoilerCasing tile = MekanismUtils.getTileEntity(TileEntityBoilerCasing.class, getWorld(), data.location.getPos());
                        if (tile != null) {
                            tile.clientHasStructure = true;
                        }
                    }
                }
            }
        }
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
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return structure == null;
        }
        return super.isCapabilityDisabled(capability, side);
    }
}