package mekanism.common.capabilities.basic;

import java.util.Collection;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.interfaces.ILogisticalTransporter;
import mekanism.common.transmitters.grid.InventoryNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by ben on 03/05/16.
 */
public class DefaultLogisticalTransporter implements ILogisticalTransporter {

    public static void register() {
        CapabilityManager.INSTANCE.register(ILogisticalTransporter.class, new NullStorage<>(), DefaultLogisticalTransporter::new);
    }

    @Override
    public TransitResponse insert(TileEntity outputter, TransitRequest request, EnumColor color, boolean doEmit, int min) {
        return request.getEmptyResponse();
    }

    @Override
    public TransitResponse insertRR(TileEntityLogisticalSorter outputter, TransitRequest request, EnumColor color, boolean doEmit, int min) {
        return request.getEmptyResponse();
    }

    @Override
    public void entityEntering(TransporterStack stack, int progress) {
    }

    @Override
    public EnumColor getColor() {
        return null;
    }

    @Override
    public void setColor(EnumColor c) {
    }

    @Override
    public boolean canEmitTo(TileEntity tile, Direction side) {
        return false;
    }

    @Override
    public boolean canReceiveFrom(TileEntity tile, Direction side) {
        return false;
    }

    @Override
    public double getCost() {
        return 0;
    }

    @Override
    public boolean canConnectMutual(Direction side, @Nullable TileEntity cachedTile) {
        return false;
    }

    @Override
    public boolean canConnect(Direction side) {
        return false;
    }

    @Override
    public boolean hasTransmitterNetwork() {
        return false;
    }

    @Override
    public InventoryNetwork getTransmitterNetwork() {
        return null;
    }

    @Override
    public void setTransmitterNetwork(InventoryNetwork network) {
    }

    @Override
    public int getTransmitterNetworkSize() {
        return 0;
    }

    @Override
    public int getTransmitterNetworkAcceptorSize() {
        return 0;
    }

    @Override
    public ITextComponent getTransmitterNetworkNeeded() {
        return null;
    }

    @Override
    public ITextComponent getTransmitterNetworkFlow() {
        return null;
    }

    @Override
    public ITextComponent getTransmitterNetworkBuffer() {
        return null;
    }

    @Override
    public long getTransmitterNetworkCapacity() {
        return 0;
    }

    @Nonnull
    @Override
    public FloatingLong getCapacityAsFloatingLong() {
        return FloatingLong.ZERO;
    }

    @Override
    public long getCapacity() {
        return 0;
    }

    @Override
    public World world() {
        return null;
    }

    @Override
    public Coord4D coord() {
        return null;
    }

    @Override
    public Coord4D getAdjacentConnectableTransmitterCoord(Direction side) {
        return null;
    }

    @Override
    public TileEntity getAcceptor(Direction side) {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean isOrphan() {
        return false;
    }

    @Override
    public void setOrphan(boolean orphaned) {
    }

    @Override
    public InventoryNetwork createEmptyNetwork() {
        return null;
    }

    @Override
    public InventoryNetwork createEmptyNetworkWithID(UUID networkID) {
        return null;
    }

    @Override
    public InventoryNetwork mergeNetworks(Collection<InventoryNetwork> toMerge) {
        return null;
    }

    @Override
    public InventoryNetwork getExternalNetwork(Coord4D from) {
        return null;
    }

    @Override
    public void takeShare() {
    }

    @Override
    public Void releaseShare() {
        return null;
    }

    @Override
    public Void getShare() {
        return null;
    }

    @Override
    public void setRequestsUpdate() {
    }

    @Override
    public TransmissionType getTransmissionType() {
        return null;
    }

    @Override
    public boolean isCompatibleWith(IGridTransmitter<TileEntity, InventoryNetwork, Void> other) {
        return false;
    }
}