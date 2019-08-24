package mekanism.additions.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityGlowPanel extends TileEntity implements ITileNetwork {

    public Direction side = Direction.DOWN;

    public TileEntityGlowPanel() {
        super(AdditionsTileEntityTypes.GLOW_PANEL);
    }

    public void setOrientation(Direction newSide) {
        side = newSide;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        side = Direction.byIndex(dataStream.readInt());
        MekanismUtils.updateBlock(world, pos);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        //TODO: Multipart
        /*if (Mekanism.hooks.MCMPLoaded) {
            MultipartTileNetworkJoiner.addMultipartHeader(this, data, side);
        }*/
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return Capabilities.TILE_NETWORK_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }
}