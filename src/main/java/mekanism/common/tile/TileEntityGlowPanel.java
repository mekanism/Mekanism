package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileNetwork;
import mekanism.common.block.BlockGlowPanel;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.integration.multipart.MultipartTileNetworkJoiner;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityGlowPanel extends TileEntity implements ITileNetwork {

    private EnumColor color = EnumColor.WHITE;
    public EnumFacing side = EnumFacing.DOWN;

    public static int hash(IBlockState state) {
        int hash = 1;
        if (state.getBlock() instanceof BlockGlowPanel) {
            //Hash the color
            hash = 31 * hash + ((BlockGlowPanel) state.getBlock()).getColor().ordinal();
        }
        hash = 31 * hash + state.getValue(BlockStateHelper.facingProperty).ordinal();
        return hash;
    }

    public void setColor(EnumColor newColor) {
        color = newColor;
    }

    public void setOrientation(EnumFacing newSide) {
        side = newSide;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        side = EnumFacing.byIndex(dataStream.readInt());
        color = EnumColor.DYES[dataStream.readInt()];
        MekanismUtils.updateBlock(world, pos);
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        if (Mekanism.hooks.MCMPLoaded) {
            MultipartTileNetworkJoiner.addMultipartHeader(this, data, side);
        }
        data.add(side.ordinal());
        data.add(color.getMetaValue());
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
        //TODO: Potentially rename nbt to color in port to 1.14
        nbt.setInteger("colour", color.getMetaValue());
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
        color = EnumColor.DYES[nbt.getInteger("colour")];
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