package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.multipart.MultipartTileNetworkJoiner;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityGlowPanel extends TileEntity implements ITileNetwork {

    public Direction side = Direction.DOWN;

    public void setOrientation(Direction newSide) {
        side = newSide;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        side = Direction.byIndex(dataStream.readInt());
        MekanismUtils.updateBlock(world, pos);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        if (Mekanism.hooks.MCMPLoaded) {
            MultipartTileNetworkJoiner.addMultipartHeader(this, data, side);
        }
        data.add(side.ordinal());
        return data;
    }

    @Override
    public void validate() {
        super.validate();
        if (world.isRemote) {
            Mekanism.packetHandler.sendToServer(new PacketDataRequest(Coord4D.get(this)));
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);
        nbt.putInt("side", side.ordinal());
        return nbt;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);
        side = Direction.byIndex(nbt.getInt("side"));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction facing) {
        return capability == Capabilities.TILE_NETWORK_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return Capabilities.TILE_NETWORK_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, facing);
    }
}