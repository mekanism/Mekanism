package mekanism.common.tile.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.Chunk3D;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.PacketUpdateTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.interfaces.ITileWrapper;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of TileEntity that adds various helpers we use across the majority of our Tiles even those that are not an instance of TileEntityMekanism. Additionally, we
 * improve the performance of markDirty by not firing neighbor updates unless the markDirtyComparator method is overridden.
 */
public abstract class TileEntityUpdateable extends BlockEntity implements ITileWrapper {

    @Nullable
    private GlobalPos cachedCoord;
    private boolean cacheCoord;
    private long lastSave;
    private final long worldPositionLong;

    public TileEntityUpdateable(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state) {
        super(type.get(), pos, state);
        this.worldPositionLong = pos.asLong();
    }

    /**
     * Collects all data component types that should be persisted to the dropped item. Override this for any conditionally applied component types.
     */
    public List<DataComponentType<?>> getRemapEntries() {
        return new ArrayList<>(collectComponents().keySet());
    }

    /**
     * Called when block is placed in world
     */
    public void onAdded() {
    }

    /**
     * Call this for tiles that we may call {@link #getTileGlobalPos()} a fair amount on to cache the coord when position/world information changes.
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
    @NotNull
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
            long time = level.getGameTime();
            if (lastSave != time) {
                //Only mark the chunk as dirty at most once per tick
                WorldUtils.markChunkDirty(level, worldPosition);
                lastSave = time;
            }
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
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        //We don't want to do a full read from NBT so simply call the super's read method to let Neo do whatever
        // it wants, but don't treat this as if it was the full saved NBT data as not everything has to be synced to the client
        super.loadAdditional(tag, provider);
        //Copy of logic from BlockEntity#loadWithComponents which we can't just call directly as we don't want to call sub-implementations of loadAdditional
        BlockEntity.ComponentHelper.COMPONENTS_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag)
              .resultOrPartial(p_337987_ -> Mekanism.logger.warn("Failed to load components: {}", p_337987_))
              .ifPresent(this::setComponents);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
        return getReducedUpdateTag(provider);
    }

    /**
     * Similar to {@link #getUpdateTag(HolderLookup.Provider)} but with reduced information for when we are doing our own syncing.
     */
    @NotNull
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        //Add the base update tag information
        return super.getUpdateTag(provider);
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, @NotNull HolderLookup.Provider provider) {
        //Handle the update tag when we are on the client
        CompoundTag tag = pkt.getTag();
        if (!tag.isEmpty()) {
            handleUpdateTag(tag, provider);
        }
    }

    public void sendUpdatePacket() {
        sendUpdatePacket(this);
    }

    public void sendUpdatePacket(BlockEntity tracking) {
        if (isRemote()) {
            Mekanism.logger.warn("Update packet call requested from client side", new IllegalStateException());
        } else if (isRemoved()) {
            Mekanism.logger.warn("Update packet call requested for removed tile", new IllegalStateException());
        } else if (PacketUtils.hasPlayersTracking((ServerLevel) tracking.getLevel(), tracking.getBlockPos())) {
            //Note: We use our own update packet/channel to avoid chunk trashing and minecraft attempting to rerender
            // the entire chunk when most often we are just updating a TileEntityRenderer, so the chunk itself
            // does not need to and should not be redrawn
            PacketUtils.sendToAllTracking(new PacketUpdateTile(this), tracking);
        }
    }

    protected void updateModelData() {
        requestModelDataUpdate();
        WorldUtils.updateBlock(getLevel(), getBlockPos(), getBlockState());
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        updateCoord();
    }

    @Override
    public void setLevel(@NotNull Level world) {
        super.setLevel(world);
        updateCoord();
        //TODO: Do we need to clear the BlockCapabilityCaches we are storing if the level changes?
        // The level changing is not really an expected thing to have happen, but might be worth
        // considering if we run into mods that the caches are causing problems for
    }

    private void updateCoord() {
        if (cacheCoord && level != null) {
            cachedCoord = GlobalPos.of(level.dimension(), worldPosition);
        }
    }

    @Override
    public GlobalPos getTileGlobalPos() {
        return cacheCoord && cachedCoord != null ? cachedCoord : ITileWrapper.super.getTileGlobalPos();
    }

    public long getWorldPositionLong() {
        return worldPositionLong;
    }

    @Override
    public Chunk3D getTileChunk() {
        if (cacheCoord && cachedCoord != null) {
            return new Chunk3D(cachedCoord);
        }
        BlockPos pos = this.getBlockPos();
        return new Chunk3D(this.getLevel().dimension(), SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }
}