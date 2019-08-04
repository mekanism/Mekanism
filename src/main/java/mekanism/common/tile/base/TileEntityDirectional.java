package mekanism.common.tile.base;

import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.base.IBlockProvider;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class TileEntityDirectional extends TileEntityMekanism implements ITileDirectional {

    /**
     * The direction this block is facing.
     */
    @Nonnull
    public EnumFacing facing = EnumFacing.NORTH;

    public TileEntityDirectional(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Nonnull
    @Override
    public EnumFacing getDirection() {
        return facing;
    }

    @Override
    public void setFacing(@Nonnull EnumFacing direction) {
        if (canSetFacing(direction)) {
            EnumFacing previousDirection = getDirection();
            facing = direction;
            if (!world.isRemote && previousDirection != getDirection()) {
                Mekanism.packetHandler.sendUpdatePacket(this);
                markDirty();
            }
        }
    }

    /**
     * Whether or not this block's orientation can be changed to a specific direction. True by default.
     *
     * @param facing - facing to check
     *
     * @return if the block's orientation can be changed
     */
    @Override
    public boolean canSetFacing(@Nonnull EnumFacing facing) {
        return true;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            EnumFacing previousDirection = getDirection();
            facing = EnumFacing.byIndex(dataStream.readInt());
            if (previousDirection != getDirection()) {
                MekanismUtils.updateBlock(world, getPos());
                world.notifyNeighborsOfStateChange(getPos(), world.getBlockState(getPos()).getBlock(), true);
            }
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(getDirection().ordinal());
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
        nbtTags.setInteger("facing", getDirection().ordinal());
        return nbtTags;
    }

    @Override
    public WrenchResult tryWrench(IBlockState state, EntityPlayer player, EnumHand hand, Supplier<RayTraceResult> rayTraceSupplier) {
        WrenchResult result = super.tryWrench(state, player, hand, rayTraceSupplier);
        if (result == WrenchResult.SUCCESS) {
            setFacing(getDirection().rotateY());
            world.notifyNeighborsOfStateChange(pos, getBlockType(), true);
        }
        return result;
    }
}