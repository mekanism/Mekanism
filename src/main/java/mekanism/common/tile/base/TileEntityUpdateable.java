package mekanism.common.tile.base;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.network.to_client.PacketUpdateTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.interfaces.ITileWrapper;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Extension of TileEntity that adds various helpers we use across the majority of our Tiles even those that are not an instance of TileEntityMekanism. Additionally, we
 * improve the performance of markDirty by not firing neighbor updates unless the markDirtyComparator method is overridden.
 */
public abstract class TileEntityUpdateable extends BlockEntity implements ITileWrapper {

    @Nullable
    private Coord4D cachedCoord;
    private boolean cacheCoord;

    public TileEntityUpdateable(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state) {
        super(type.get(), pos, state);
    }

    /**
     * Call this for tiles that we may call {@link #getTileCoord()} a fair amount on to cache the coord when position/world information changes.
     */
    protected void cacheCoord() {
        //Mark that we want to cache the coord and then update the coord if needed
        cacheCoord = true;
        updateCoord();
    }

    /**
     * Like getWorld(), but for when you _know_ world won't be null
     *
     * @return The world!
     */
    @Nonnull
    protected Level getWorldNN() {
        return Objects.requireNonNull(getLevel(), "getWorldNN called before world set");
    }

    public boolean isRemote() {
        return getWorldNN().isClientSide();
    }

    /**
     * Called when the tile is permanently removed
     *
     * @implNote We only need to handle logic that happens when removed and not unloaded as if it happens for both then setRemoved will handle it
     */
    public void blockRemoved() {
    }

    /**
     * Used for checking if we need to update comparators.
     *
     * @apiNote Only call on the server
     */
    public void markDirtyComparator() {
    }

    @Override
    public final void setChanged() {
        setChanged(true);
    }

    public final void markForSave() {
        setChanged(false);
    }

    protected void setChanged(boolean updateComparator) {
        //Copy of the base impl of markDirty in TileEntity, except only updates comparator state when something changed
        // and if our block supports having a comparator signal, instead of always doing it
        if (level != null) {
            WorldUtils.markChunkDirty(level, worldPosition);
            if (updateComparator && !isRemote()) {
                markDirtyComparator();
            }
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        //We don't want to do a full read from NBT so simply call the super's read method to let Forge do whatever
        // it wants, but don't treat this as if it was the full saved NBT data as not everything has to be synced to the client
        super.load(tag);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return getReducedUpdateTag();
    }

    /**
     * Similar to {@link #getUpdateTag()} but with reduced information for when we are doing our own syncing.
     */
    @Nonnull
    public CompoundTag getReducedUpdateTag() {
        //Add the base update tag information
        return super.getUpdateTag();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (isRemote() && net.getDirection() == PacketFlow.CLIENTBOUND) {
            //Handle the update tag when we are on the client
            handleUpdatePacket(pkt.getTag());
        }
    }

    public void handleUpdatePacket(@Nonnull CompoundTag tag) {
        handleUpdateTag(tag);
    }

    public void sendUpdatePacket() {
        sendUpdatePacket(this);
    }

    public void sendUpdatePacket(BlockEntity tracking) {
        if (isRemote()) {
            Mekanism.logger.warn("Update packet call requested from client side", new IllegalStateException());
        } else if (isRemoved()) {
            Mekanism.logger.warn("Update packet call requested for removed tile", new IllegalStateException());
        } else {
            //Note: We use our own update packet/channel to avoid chunk trashing and minecraft attempting to rerender
            // the entire chunk when most often we are just updating a TileEntityRenderer, so the chunk itself
            // does not need to and should not be redrawn
            Mekanism.packetHandler().sendToAllTracking(new PacketUpdateTile(this), tracking);
        }
    }

    @Override
    public Level getTileWorld() {
        return level;
    }

    @Override
    public BlockPos getTilePos() {
        return worldPosition;
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        updateCoord();
    }

    @Override
    public void setLevel(@Nonnull Level world) {
        super.setLevel(world);
        updateCoord();
    }

    private void updateCoord() {
        if (cacheCoord && level != null) {
            cachedCoord = new Coord4D(worldPosition, level);
        }
    }

    @Override
    public Coord4D getTileCoord() {
        return cacheCoord && cachedCoord != null ? cachedCoord : ITileWrapper.super.getTileCoord();
    }
}