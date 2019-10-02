package mekanism.common.tile.transmitter;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IHeatTransfer;
import mekanism.api.TileNetworkList;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.ColourRGBA;
import mekanism.common.Mekanism;
import mekanism.common.block.states.TransmitterType;
import mekanism.common.block.transmitter.BlockThermodynamicConductor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.transmitters.grid.HeatNetwork;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityThermodynamicConductor extends TileEntityTransmitter<IHeatTransfer, HeatNetwork, Void> implements IHeatTransfer {

    public ConductorTier tier;

    public double temperature = 0;
    public double clientTemperature = 0;
    public double heatToAbsorb = 0;

    public TileEntityThermodynamicConductor(IBlockProvider blockProvider) {
        super(((IHasTileEntity<TileEntityThermodynamicConductor>) blockProvider.getBlock()).getTileType());
        this.tier = ((BlockThermodynamicConductor) blockProvider.getBlock()).getTier();
    }

    @Override
    public BaseTier getBaseTier() {
        return tier.getBaseTier();
    }

    @Override
    public void setBaseTier(BaseTier baseTier) {
        tier = ConductorTier.get(baseTier);
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
    public Void getBuffer() {
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
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        return CapabilityUtils.getCapabilityHelper(tile, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).matches(transfer -> transfer.canConnectHeat(side.getOpposite()));
    }

    @Override
    public TransmissionType getTransmissionType() {
        return TransmissionType.HEAT;
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        temperature = nbtTags.getDouble("temperature");
        if (nbtTags.contains("tier")) {
            tier = EnumUtils.CONDUCTOR_TIERS[nbtTags.getInt("tier")];
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble("temperature", temperature);
        nbtTags.putInt("tier", tier.ordinal());
        return nbtTags;
    }

    public void sendTemp() {
        Mekanism.packetHandler.sendUpdatePacket(this);
    }

    @Override
    public IHeatTransfer getCachedAcceptor(Direction side) {
        return CapabilityUtils.getCapabilityHelper(getCachedTile(side), Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).getValue();
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) throws Exception {
        tier = dataStream.readEnumValue(ConductorTier.class);
        super.handlePacketData(dataStream);
        temperature = dataStream.readDouble();
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(tier);
        super.getNetworkedData(data);
        data.add(temperature);
        return data;
    }

    public ColourRGBA getBaseColour() {
        return tier.getBaseColour();
    }

    @Override
    public double getTemp() {
        return temperature;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return tier.getInverseConduction();
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return tier.getInverseConductionInsulation();
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
        temperature += tier.getInverseHeatCapacity() * heatToAbsorb;
        heatToAbsorb = 0;
        if (Math.abs(temperature - clientTemperature) > (temperature / 20)) {
            clientTemperature = temperature;
            sendTemp();
        }
        return temperature;
    }

    @Override
    public boolean canConnectHeat(Direction side) {
        return true;
    }

    @Nullable
    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        if (connectionMapContainsSide(getAllCurrentConnections(), side)) {
            TileEntity adj = MekanismUtils.getTileEntity(world, getPos().offset(side));
            return CapabilityUtils.getCapabilityHelper(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).getValue();
        }
        return null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean upgrade(int tierOrdinal) {
        if (tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal() + 1) {
            tier = EnumUtils.CONDUCTOR_TIERS[tier.ordinal() + 1];
            markDirtyTransmitters();
            sendDesc = true;
            return true;
        }
        return false;
    }
}