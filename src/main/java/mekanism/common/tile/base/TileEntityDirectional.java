package mekanism.common.tile.base;

import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class TileEntityDirectional extends TileEntityMekanism {

    /**
     * The direction this block is facing.
     */
    public EnumFacing facing = EnumFacing.NORTH;

    public EnumFacing clientFacing = facing;

    public void setFacing(@Nonnull EnumFacing direction) {
        if (canSetFacing(direction)) {
            facing = direction;
        }
        if (facing != clientFacing && !world.isRemote) {
            Mekanism.packetHandler.sendUpdatePacket(this);
            markDirty();
            clientFacing = facing;
        }
    }

    /**
     * Whether or not this block's orientation can be changed to a specific direction. True by default.
     *
     * @param facing - facing to check
     *
     * @return if the block's orientation can be changed
     */
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        //TODO: This shouldn't be needed because the blockstate knows what directions it can go
        return true;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            facing = EnumFacing.byIndex(dataStream.readInt());
            if (clientFacing != facing) {
                MekanismUtils.updateBlock(world, getPos());
                world.notifyNeighborsOfStateChange(getPos(), world.getBlockState(getPos()).getBlock(), true);
                clientFacing = facing;
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(facing == null ? -1 : facing.ordinal());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);
        if (nbtTags.hasKey("facing")) {
            facing = EnumFacing.byIndex(nbtTags.getInteger("facing"));
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);
        if (facing != null) {
            nbtTags.setInteger("facing", facing.ordinal());
        }
        return nbtTags;
    }

    @Override
    public WrenchResult tryWrench(IBlockState state, EntityPlayer player, EnumHand hand, Supplier<RayTraceResult> rayTraceSupplier) {
        WrenchResult result = super.tryWrench(state, player, hand, rayTraceSupplier);
        if (result == WrenchResult.SUCCESS) {
            setFacing(facing.rotateY());
            world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
        }
        return result;
    }
}