package mekanism.common.content.network.transmitter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.content.network.HeatNetwork;
import mekanism.common.lib.Color;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.upgrade.transmitter.ThermodynamicConductorUpgradeData;
import mekanism.common.upgrade.transmitter.TransmitterUpgradeData;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class ThermodynamicConductor extends Transmitter<IHeatHandler, HeatNetwork, ThermodynamicConductor> implements ITileHeatHandler,
      IUpgradeableTransmitter<ThermodynamicConductorUpgradeData> {

    public final ConductorTier tier;
    private double clientTemperature = HeatAPI.AMBIENT_TEMP;
    private final List<IHeatCapacitor> capacitors;
    public final BasicHeatCapacitor buffer;

    public ThermodynamicConductor(IBlockProvider blockProvider, TileEntityTransmitter tile) {
        super(tile, TransmissionType.HEAT);
        this.tier = Attribute.getTier(blockProvider.getBlock(), ConductorTier.class);
        buffer = BasicHeatCapacitor.create(tier.getHeatCapacity(), tier.getInverseConduction(), tier.getInverseConductionInsulation(), this);
        capacitors = Collections.singletonList(buffer);
    }

    @Override
    public AcceptorCache<IHeatHandler> getAcceptorCache() {
        //Cast it here to make things a bit easier, as we know the create is by default of type AcceptorCache
        return (AcceptorCache<IHeatHandler>) super.getAcceptorCache();
    }

    @Override
    public ConductorTier getTier() {
        return tier;
    }

    @Override
    public HeatNetwork createEmptyNetwork() {
        return new HeatNetwork();
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
    public boolean isValidAcceptor(TileEntity tile, Direction side) {
        return getAcceptorCache().isAcceptorAndListen(tile, side, Capabilities.HEAT_HANDLER_CAPABILITY);
    }

    @Nullable
    @Override
    public ThermodynamicConductorUpgradeData getUpgradeData() {
        return new ThermodynamicConductorUpgradeData(redstoneReactive, getConnectionTypesRaw(), buffer.getHeat());
    }

    @Override
    public boolean dataTypeMatches(@Nonnull TransmitterUpgradeData data) {
        return data instanceof ThermodynamicConductorUpgradeData;
    }

    @Override
    public void parseUpgradeData(@Nonnull ThermodynamicConductorUpgradeData data) {
        redstoneReactive = data.redstoneReactive;
        setConnectionTypesRaw(data.connectionTypes);
        buffer.setHeat(data.heat);
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT tag) {
        tag = super.write(tag);
        tag.put(NBTConstants.HEAT_CAPACITORS, DataHandlerUtils.writeContainers(getHeatCapacitors(null)));
        return tag;
    }

    @Override
    public void read(@Nonnull CompoundNBT tag) {
        super.read(tag);
        DataHandlerUtils.readContainers(getHeatCapacitors(null), tag.getList(NBTConstants.HEAT_CAPACITORS, NBT.TAG_COMPOUND));
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag(CompoundNBT updateTag) {
        updateTag = super.getReducedUpdateTag(updateTag);
        updateTag.putDouble(NBTConstants.TEMPERATURE, buffer.getHeat());
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setDoubleIfPresent(tag, NBTConstants.TEMPERATURE, buffer::setHeat);
    }

    public Color getBaseColor() {
        return tier.getBaseColor();
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return capacitors;
    }

    @Override
    public void onContentsChanged() {
        if (!isRemote()) {
            if (Math.abs(buffer.getTemperature() - clientTemperature) > (buffer.getTemperature() / 20)) {
                clientTemperature = buffer.getTemperature();
                getTransmitterTile().sendUpdatePacket();
            }
        }
        getTransmitterTile().markDirty(false);
    }

    @Nullable
    @Override
    public IHeatHandler getAdjacent(Direction side) {
        if (connectionMapContainsSide(getAllCurrentConnections(), side)) {
            //Note: We use the acceptor cache as the heat network is different and the transmitters count the other transmitters in the
            // network as valid acceptors
            return getAcceptorCache().getConnectedAcceptor(side).resolve().orElse(null);
        }
        return null;
    }
}