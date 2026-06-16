package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.UUID;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.content.network.HeatNetwork;
import mekanism.common.lib.Color;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.ThermodynamicConductorUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ThermodynamicConductor extends Transmitter<IHeatHandler, HeatNetwork, ThermodynamicConductor> implements ITileHeatHandler, IContentsListener,
      IUpgradeableTransmitter<ThermodynamicConductorUpgradeData> {

    private final CachedAmbientTemperature ambientTemperature = new CachedAmbientTemperature(this::getLevel, this::getBlockPos);
    public final ConductorTier tier;
    //Default to negative one, so we know we need to calculate it when needed
    private double clientTemperature = -1;
    public final VariableHeatCapacitor buffer;

    public ThermodynamicConductor(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        this.tier = Attribute.getTierNN(blockProvider, ConductorTier.class);
        super(tile, TransmissionType.HEAT);
        buffer = VariableHeatCapacitor.create(tier.getHeatCapacity(), tier::getInverseConduction, tier::getInverseConductionInsulation, ambientTemperature, this);
    }

    @Override
    protected AcceptorCache<IHeatHandler> createAcceptorCache() {
        return new AcceptorCache<>(getTransmitterTile(), Capabilities.HEAT);
    }

    @Override
    public ConductorTier getTier() {
        return tier;
    }

    @Override
    public HeatNetwork createEmptyNetworkWithID(UUID networkID) {
        return new HeatNetwork(networkID);
    }

    @Override
    public HeatNetwork createNetworkByMerging(Collection<HeatNetwork> networks) {
        return new HeatNetwork(networks);
    }

    @Override
    public void takeShare(@Nullable TransactionContext transaction) {
    }

    @Override
    protected boolean isValidAcceptor(@Nullable BlockEntity tile, Direction side) {
        //Note: We intentionally do not call super here as other elements in the network are intentionally acceptors
        return getAcceptorCache().getConnectedAcceptor(side) != null;
    }

    @Override
    public ThermodynamicConductorUpgradeData getUpgradeData() {
        return new ThermodynamicConductorUpgradeData(redstoneReactive, getConnectionTypesRaw(), buffer.getHeat());
    }

    @Override
    public boolean dataTypeMatches(TransmitterUpgradeData data) {
        return data instanceof ThermodynamicConductorUpgradeData;
    }

    @Override
    public void parseUpgradeData(ThermodynamicConductorUpgradeData data, TransactionContext transaction) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        buffer.setHeat(data.heat, transaction);
    }

    @Override
    public void write(ValueOutput output) {
        super.write(output);
        ContainerType.HEAT.saveTo(output, buffer);
    }

    @Override
    public void read(ValueInput input) {
        super.read(input);
        ContainerType.HEAT.readFrom(input, buffer);
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        output.putDouble(SerializationConstants.HEAT_STORED, buffer.getHeat());
    }

    @Override
    public boolean handleUpdateTag(ValueInput input) {
        boolean refreshModelData = super.handleUpdateTag(input);
        buffer.setHeat(input.getDoubleOr(SerializationConstants.HEAT_STORED, buffer.getHeat()), null);
        return refreshModelData;
    }

    public Color getBaseColor() {
        return tier.getBaseColor();
    }

    @Override
    public IHeatCapacitor getHeatCapacitor(@Nullable Direction side) {
        return buffer;
    }

    public double getTemperature() {
        return buffer.getTemperature();
    }

    @Override
    public void onContentsChanged() {
        if (!isRemote()) {
            if (clientTemperature == -1) {
                clientTemperature = ambientTemperature.getAsDouble();
            }
            if (Math.abs(buffer.getTemperature() - clientTemperature) > (buffer.getTemperature() / 20)) {
                clientTemperature = buffer.getTemperature();
                getTransmitterTile().sendUpdatePacket();
            }
        }
        getTransmitterTile().setChanged();
    }

    @Override
    public double getAmbientTemperature(Direction side) {
        return ambientTemperature.getTemperature(side);
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(Direction side) {
        if (connectionMapContainsSide(getAllCurrentConnections(), side)) {
            //Note: We use the acceptor cache as the heat network is different and the transmitters count the other transmitters in the
            // network as valid acceptors, which means we don't have to differentiate between acceptors and other transmitters here
            return getAcceptorCache().getConnectedAcceptor(side);
        }
        return null;
    }

    @Override
    public boolean countsAsAdjacent(Direction side) {
        //Heat transmitter to heat transmitter, don't count as "adjacent transfer"
        return !hasTransmitterNetwork() || getTransmitterNetworkNN().getTransmitter(getBlockPos().relative(side)) != null;
    }
}