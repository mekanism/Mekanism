package mekanism.common.tile.transmitter;

import io.netty.buffer.ByteBuf;
import java.util.Collection;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.Range4D;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.ColourRGBA;
import mekanism.common.Mekanism;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.ConductorTier;
import mekanism.api.TileNetworkList;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.transmitters.grid.HeatNetwork;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.HeatUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityThermodynamicConductor extends TileEntityTransmitter<IHeatTransfer, HeatNetwork> implements
      IHeatTransfer {

    public Tier.ConductorTier tier = Tier.ConductorTier.BASIC;

    public double temperature = 0;
    public double clientTemperature = 0;
    public double heatToAbsorb = 0;

    @Override
    public BaseTier getBaseTier() {
        return tier.getBaseTier();
    }

    @Override
    public void setBaseTier(BaseTier baseTier) {
        tier = Tier.ConductorTier.get(baseTier);
    }

    @Override
    public HeatNetwork createNewNetwork() {
        return new HeatNetwork();
    }

    @Override
    public HeatNetwork createNetworkByMerging(Collection<HeatNetwork> networks) {
        return new HeatNetwork(networks);
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public Object getBuffer() {
        return null;
    }

    @Override
    public void takeShare() {
    }

    @Override
    public void updateShare() {
    }

    @Override
    public TransmitterType getTransmitterType() {
        return TransmitterType.THERMODYNAMIC_CONDUCTOR;
    }

    @Override
    public boolean isValidAcceptor(TileEntity tile, EnumFacing side) {
        if (CapabilityUtils.hasCapability(tile, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite())) {
            IHeatTransfer transfer = CapabilityUtils
                  .getCapability(tile, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
            return transfer.canConnectHeat(side.getOpposite());
        }

        return false;
    }

    @Override
    public TransmissionType getTransmissionType() {
        return TransmissionType.HEAT;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        temperature = nbtTags.getDouble("temperature");
        if (nbtTags.hasKey("tier")) {
            tier = Tier.ConductorTier.values()[nbtTags.getInteger("tier")];
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setDouble("temperature", temperature);
        nbtTags.setInteger("tier", tier.ordinal());

        return nbtTags;
    }

    public void sendTemp() {
        Coord4D coord = new Coord4D(getPos(), getWorld());
        Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(coord, getNetworkedData(new TileNetworkList())),
              new Range4D(coord));
    }

    @Override
    public IHeatTransfer getCachedAcceptor(EnumFacing side) {
        TileEntity tile = getCachedTile(side);

        if (CapabilityUtils.hasCapability(tile, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite())) {
            return CapabilityUtils.getCapability(tile, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
        }

        return null;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) throws Exception {
        tier = ConductorTier.values()[dataStream.readInt()];

        super.handlePacketData(dataStream);

        temperature = dataStream.readDouble();
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(tier.ordinal());

        super.getNetworkedData(data);

        data.add(temperature);

        return data;
    }

    public ColourRGBA getBaseColour() {
        return tier.baseColour;
    }

    @Override
    public double getTemp() {
        return temperature;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return tier.inverseConduction;
    }

    @Override
    public double getInsulationCoefficient(EnumFacing side) {
        return tier.inverseConductionInsulation;
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
        temperature += tier.inverseHeatCapacity * heatToAbsorb;
        heatToAbsorb = 0;

        if (Math.abs(temperature - clientTemperature) > (temperature / 20)) {
            clientTemperature = temperature;
            sendTemp();
        }

        return temperature;
    }

    @Override
    public boolean canConnectHeat(EnumFacing side) {
        return true;
    }

    @Override
    public IHeatTransfer getAdjacent(EnumFacing side) {
        if (connectionMapContainsSide(getAllCurrentConnections(), side)) {
            TileEntity adj = getWorld().getTileEntity(getPos().offset(side));

            if (CapabilityUtils.hasCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite())) {
                return CapabilityUtils.getCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
            }
        }

        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.HEAT_TRANSFER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean upgrade(int tierOrdinal) {
        if (tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal() + 1) {
            tier = ConductorTier.values()[tier.ordinal() + 1];

            markDirtyTransmitters();
            sendDesc = true;

            return true;
        }

        return false;
    }
}
