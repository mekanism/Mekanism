package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.capabilities.heat.VariableHeatCapacitor;
import mekanism.common.content.network.HeatNetwork;
import mekanism.common.lib.Color;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AbstractAcceptorCache;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.ThermodynamicConductorUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThermodynamicConductor extends Transmitter<IHeatHandler, HeatNetwork, ThermodynamicConductor> implements ITileHeatHandler,
      IUpgradeableTransmitter<ThermodynamicConductorUpgradeData> {

    private final CachedAmbientTemperature ambientTemperature = new CachedAmbientTemperature(this::getLevel, this::getBlockPos);
    public final ConductorTier tier;
    //Default to negative one, so we know we need to calculate it when needed
    private double clientTemperature = -1;
    private final List<IHeatCapacitor> capacitors;
    public final VariableHeatCapacitor buffer;

    public ThermodynamicConductor(Holder<Block> blockProvider, TileEntityTransmitter tile) {
        super(tile, TransmissionType.HEAT);
        this.tier = Attribute.getTier(blockProvider, ConductorTier.class);
        buffer = VariableHeatCapacitor.create(tier.getHeatCapacity(), tier::getInverseConduction, tier::getInverseConductionInsulation, ambientTemperature, this);
        capacitors = Collections.singletonList(buffer);
    }

    @Override
    protected AbstractAcceptorCache<IHeatHandler, ?> createAcceptorCache() {
        return new AcceptorCache<>(getTransmitterTile(), Capabilities.HEAT);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AcceptorCache<IHeatHandler> getAcceptorCache() {
        return (AcceptorCache<IHeatHandler>) super.getAcceptorCache();
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
    public void takeShare() {
    }

    @Override
    protected boolean isValidAcceptor(@Nullable BlockEntity tile, Direction side) {
        //Note: We intentionally do not call super here as other elements in the network are intentionally acceptors
        return getAcceptorCache().getConnectedAcceptor(side) != null;
    }

    @Nullable
    @Override
    public ThermodynamicConductorUpgradeData getUpgradeData() {
        return new ThermodynamicConductorUpgradeData(redstoneReactive, getConnectionTypesRaw(), buffer.getHeat());
    }

    @Override
    public boolean dataTypeMatches(@NotNull TransmitterUpgradeData data) {
        return data instanceof ThermodynamicConductorUpgradeData;
    }

    @Override
    public void parseUpgradeData(@NotNull ThermodynamicConductorUpgradeData data) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        buffer.setHeat(data.heat);
    }

    @NotNull
    @Override
    public CompoundTag write(HolderLookup.Provider provider, @NotNull CompoundTag tag) {
        super.write(provider, tag);
        ContainerType.HEAT.saveTo(provider, tag, getHeatCapacitors(null));
        return tag;
    }

    @Override
    public void read(HolderLookup.Provider provider, @NotNull CompoundTag tag) {
        super.read(provider, tag);
        ContainerType.HEAT.readFrom(provider, tag, getHeatCapacitors(null));
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider, CompoundTag updateTag) {
        updateTag = super.getReducedUpdateTag(provider, updateTag);
        updateTag.putDouble(SerializationConstants.TEMPERATURE, buffer.getHeat());
        return updateTag;
    }

    @Override
    public boolean handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        boolean refreshModelData= super.handleUpdateTag(tag, provider);
        NBTUtils.setDoubleIfPresent(tag, SerializationConstants.TEMPERATURE, buffer::setHeat);
        return refreshModelData;
    }

    public Color getBaseColor() {
        return tier.getBaseColor();
    }

    @NotNull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return capacitors;
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
    public double getAmbientTemperature(@NotNull Direction side) {
        return ambientTemperature.getTemperature(side);
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(@NotNull Direction side) {
        if (connectionMapContainsSide(getAllCurrentConnections(), side)) {
            //Note: We use the acceptor cache as the heat network is different and the transmitters count the other transmitters in the
            // network as valid acceptors, which means we don't have to differentiate between acceptors and other transmitters here
            return getAcceptorCache().getConnectedAcceptor(side);
        }
        return null;
    }

    @Override
    public double incrementAdjacentTransfer(double currentAdjacentTransfer, double tempToTransfer, @NotNull Direction side) {
        if (tempToTransfer > 0 && hasTransmitterNetwork()) {
            HeatNetwork transmitterNetwork = getTransmitterNetwork();
            ThermodynamicConductor adjacent = transmitterNetwork.getTransmitter(getBlockPos().relative(side));
            if (adjacent != null) {
                //Heat transmitter to heat transmitter, don't count as "adjacent transfer"
                return currentAdjacentTransfer;
            }
        }
        return ITileHeatHandler.super.incrementAdjacentTransfer(currentAdjacentTransfer, tempToTransfer, side);
    }
}