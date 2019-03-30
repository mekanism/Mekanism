package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.Range4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.boiler.BoilerCache;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.content.tank.SynchronizedTankData.ValveData;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityBoilerCasing extends TileEntityMultiblock<SynchronizedBoilerData> implements IHeatTransfer {

    protected static final int[] INV_SLOTS = {0,1};

    /**
     * A client-sided set of valves on this tank's structure that are currently active, used on the client for rendering
     * fluids.
     */
    public Set<ValveData> valveViewing = new HashSet<>();

    /**
     * The capacity this tank has on the client-side.
     */
    public int clientWaterCapacity;
    public int clientSteamCapacity;

    public float prevWaterScale;

    public TileEntityBoilerCasing() {
        this("BoilerCasing");
    }

    public TileEntityBoilerCasing(String name) {
        super(name);
        inventory = NonNullList.withSize(INV_SLOTS.length, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote) {
            if (structure != null && clientHasStructure && isRendering) {
                float targetScale =
                      (float) (structure.waterStored != null ? structure.waterStored.amount : 0) / clientWaterCapacity;

                if (Math.abs(prevWaterScale - targetScale) > 0.01) {
                    prevWaterScale = (9 * prevWaterScale + targetScale) / 10;
                }
            }

            if (!clientHasStructure || !isRendering) {
                for (ValveData data : valveViewing) {
                    TileEntityBoilerCasing tileEntity = (TileEntityBoilerCasing) data.location.getTileEntity(world);

                    if (tileEntity != null) {
                        tileEntity.clientHasStructure = false;
                    }
                }

                valveViewing.clear();
            }
        }

        if (!world.isRemote) {
            if (structure != null) {
                if (structure.waterStored != null && structure.waterStored.amount <= 0) {
                    structure.waterStored = null;
                    markDirty();
                }

                if (structure.steamStored != null && structure.steamStored.amount <= 0) {
                    structure.steamStored = null;
                    markDirty();
                }

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

                    if (structure.temperature >= SynchronizedBoilerData.BASE_BOIL_TEMP
                          && structure.waterStored != null) {
                        int steamAmount = structure.steamStored != null ? structure.steamStored.amount : 0;
                        double heatAvailable = structure.getHeatAvailable();

                        structure.lastMaxBoil = (int) Math
                              .floor(heatAvailable / SynchronizedBoilerData.getHeatEnthalpy());

                        int amountToBoil = Math.min(structure.lastMaxBoil, structure.waterStored.amount);
                        amountToBoil = Math.min(amountToBoil,
                              (structure.steamVolume * BoilerUpdateProtocol.STEAM_PER_TANK) - steamAmount);
                        structure.waterStored.amount -= amountToBoil;

                        if (structure.steamStored == null) {
                            structure.steamStored = new FluidStack(FluidRegistry.getFluid("steam"), amountToBoil);
                        } else {
                            structure.steamStored.amount += amountToBoil;
                        }

                        structure.temperature -=
                              (amountToBoil * SynchronizedBoilerData.getHeatEnthalpy()) / structure.locations.size();
                        structure.lastBoilRate = amountToBoil;
                    } else {
                        structure.lastBoilRate = 0;
                        structure.lastMaxBoil = 0;
                    }

                    if (needsValveUpdate || structure.needsRenderUpdate() || needsHotUpdate) {
                        sendPacketToRenderer();
                    }

                    structure.prevWater = structure.waterStored != null ? structure.waterStored.copy() : null;
                    structure.prevSteam = structure.steamStored != null ? structure.steamStored.copy() : null;

                    MekanismUtils.saveChunk(this);
                }
            }
        }
    }

    @Override
    public boolean onActivate(EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (!player.isSneaking() && structure != null) {
            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));
            player.openGui(Mekanism.instance, 54, world, getPos().getX(), getPos().getY(), getPos().getZ());

            return true;
        }

        return false;
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
        if (clientWaterCapacity == 0 || structure.waterStored == null) {
            return 0;
        }

        return structure.waterStored.amount * i / clientWaterCapacity;
    }

    public int getScaledSteamLevel(int i) {
        if (clientSteamCapacity == 0 || structure.steamStored == null) {
            return 0;
        }

        return structure.steamStored.amount * i / clientSteamCapacity;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
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
                        data.side = EnumFacing.byIndex(dataStream.readInt());

                        valveViewing.add(data);

                        TileEntityBoilerCasing tileEntity = (TileEntityBoilerCasing) data.location.getTileEntity(world);

                        if (tileEntity != null) {
                            tileEntity.clientHasStructure = true;
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
    public double getInsulationCoefficient(EnumFacing side) {
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

    @Override
    public boolean canConnectHeat(EnumFacing side) {
        return structure != null;
    }

    @Override
    public IHeatTransfer getAdjacent(EnumFacing side) {
        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.HEAT_TRANSFER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return (T) this;
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Nonnull
    @Override
    public String getName() {
        return LangUtils.localize("gui.thermoelectricBoiler");
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return InventoryUtils.EMPTY;
    }
}
