package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileNetwork;
import mekanism.api.TileNetworkList;
import mekanism.common.block.property.PropertyColor;
import mekanism.common.block.states.BlockStateFacing;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.multipart.MultipartTileNetworkJoiner;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;

public class TileEntityGlowPanel extends TileEntity implements ITileNetwork {

    public EnumColor colour = EnumColor.WHITE;
    public EnumFacing side = EnumFacing.DOWN;

    public static int hash(IExtendedBlockState state) {
        int hash = 1;
        PropertyColor propColor = state.getValue(PropertyColor.INSTANCE);
        EnumColor color = propColor != null ? propColor.color : EnumColor.WHITE;
        hash = 31 * hash + color.ordinal();
        hash = 31 * hash + state.getValue(BlockStateFacing.facingProperty).ordinal();

        return hash;
    }

    public void setColour(EnumColor newColour) {
        colour = newColour;
    }

    public void setOrientation(EnumFacing newSide) {
        side = newSide;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        side = EnumFacing.byIndex(dataStream.readInt());
        colour = EnumColor.DYES[dataStream.readInt()];

        MekanismUtils.updateBlock(world, pos);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        if (Mekanism.hooks.MCMPLoaded) {
            MultipartTileNetworkJoiner.addMultipartHeader(this, data, side);
        }

        data.add(side.ordinal());
        data.add(colour.getMetaValue());

        return data;
    }

    @Override
    public void validate() {
        super.validate();

        if (world.isRemote) {
            Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(this)));
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("side", side.ordinal());
        nbt.setInteger("colour", colour.getMetaValue());

        return nbt;
    }

    @Nonnull
    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        side = EnumFacing.byIndex(nbt.getInteger("side"));
        colour = EnumColor.DYES[nbt.getInteger("colour")];
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == Capabilities.TILE_NETWORK_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return Capabilities.TILE_NETWORK_CAPABILITY.cast(this);
        }
        return super.getCapability(capability, facing);
    }
}
