package mekanism.common.tile.base;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;

/**
 * Extension of TileEntity that adds various helpers we use across the majority of our Tiles even those that are not an instance of TileEntityMekanism. Additionally we
 * improve the performance of markDirty by not firing neighbor updates unless the markDirtyComparator method is overridden.
 */
public abstract class TileEntityUpdateable extends TileEntity {

    public TileEntityUpdateable(TileEntityType<?> type) {
        super(type);
    }

    /**
     * Like getWorld(), but for when you _know_ world won't be null
     *
     * @return The world!
     */
    @Nonnull
    protected World getWorldNN() {
        return Objects.requireNonNull(getWorld(), "getWorldNN called before world set");
    }

    public boolean isRemote() {
        return getWorldNN().isRemote();
    }

    /**
     * Used for checking if we need to update comparators. Note only called on the server
     */
    protected void markDirtyComparator() {
    }

    @Override
    public void markDirty() {
        //Copy of the base impl of markDirty in TileEntity, except only updates comparator state when something changed
        // and if our block supports having a comparator signal, instead of always doing it
        if (world != null) {
            //TODO: Do we even really need to be updating the cachedBlockState?
            cachedBlockState = world.getBlockState(pos);
            world.markChunkDirty(pos, this);
            if (!isRemote()) {
                markDirtyComparator();
            }
        }
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        //TODO: Do we want to sync more information the very first time, and less remaining times
        return new SUpdateTileEntityPacket(getPos(), 0, getUpdateTag());
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        //We don't want to do a full read from NBT so simply call the super's read method to let Forge do whatever
        // it wants, but don't treat this as if it was the full saved NBT data as not everything has to be synced to the client
        super.read(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        if (isRemote() && net.getDirection() == PacketDirection.CLIENTBOUND) {
            //Handle the update tag when we are on the client
            handleUpdateTag(pkt.getNbtCompound());
        }
    }

    protected void sendUpdatePacket() {
        if (world != null) {
            BlockState state = getBlockState();
            world.notifyBlockUpdate(getPos(), state, state, BlockFlags.DEFAULT);
        }
    }
}