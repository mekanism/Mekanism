package mekanism.common.tile.base;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of TileEntity that adds various helpers we use across the majority of our Tiles even those that are not an instance of TileEntityMekanism. Additionally, we
 * improve the performance of markDirty by not firing neighbor updates unless the markDirtyComparator method is overridden.
 */
public abstract class TileEntityUpdateable extends BlockEntity implements ITileWrapper {

    @Nullable
    private BiMap<AttachmentType<? extends INBTSerializable<?>>, String> syncableAttachmentTypes;
    @Nullable
    private GlobalPos cachedCoord;
    private boolean cacheCoord;
    private long lastSave;

    public TileEntityUpdateable(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state) {
        super(type.get(), pos, state);
    }

    /**
     * Call this to mark specific attachments as syncing when on this block
     */
    protected <SERIALIZABLE extends INBTSerializable<?>> void syncAttachmentType(DeferredHolder<AttachmentType<?>, AttachmentType<SERIALIZABLE>> holder) {
        if (syncableAttachmentTypes == null) {
            syncableAttachmentTypes = HashBiMap.create();
        }
        syncableAttachmentTypes.put(holder.value(), holder.getId().toString());
    }

    public void readFromStack(ItemStack stack) {
    }

    public void writeToStack(ItemStack stack) {
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
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        //We don't want to do a full read from NBT so simply call the super's read method to let Forge do whatever
        // it wants, but don't treat this as if it was the full saved NBT data as not everything has to be synced to the client
        super.load(tag);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        return getReducedUpdateTag();
    }

    /**
     * Similar to {@link #getUpdateTag()} but with reduced information for when we are doing our own syncing.
     */
    @NotNull
    public CompoundTag getReducedUpdateTag() {
        //Add the base update tag information
        CompoundTag updateTag = super.getUpdateTag();
        if (syncableAttachmentTypes != null) {
            CompoundTag serializedAttachments;
            if (updateTag.contains(ATTACHMENTS_NBT_KEY, Tag.TAG_COMPOUND)) {
                //Maybe someone mixed in and is already syncing some attachment, so we don't want to overwrite it
                serializedAttachments = updateTag.getCompound(ATTACHMENTS_NBT_KEY);
            } else {
                serializedAttachments = new CompoundTag();
            }
            //Serialize our subset of attachments that we know need to be sync'd
            syncableAttachmentTypes.forEach((type, name) -> getExistingData(type)
                  .map(INBTSerializable::serializeNBT)
                  .ifPresent(serialized -> serializedAttachments.put(name, serialized)));
            if (!serializedAttachments.isEmpty()) {
                updateTag.put(ATTACHMENTS_NBT_KEY, serializedAttachments);
            }
        }
        return updateTag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (isRemote() && net.getDirection() == PacketFlow.CLIENTBOUND) {
            //Handle the update tag when we are on the client
            CompoundTag tag = pkt.getTag();
            if (tag != null) {
                handleUpdatePacket(tag);
            }
        }
    }

    public void handleUpdatePacket(@NotNull CompoundTag tag) {
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
            PacketUtils.sendToAllTracking(new PacketUpdateTile(this), tracking);
        }
    }

    protected void updateModelData() {
        requestModelDataUpdate();
        WorldUtils.updateBlock(getLevel(), getBlockPos(), getBlockState());
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        updateCoord();
    }

    @Override
    public void setLevel(@NotNull Level world) {
        super.setLevel(world);
        updateCoord();
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

    @Override
    public Chunk3D getTileChunk() {
        if (cacheCoord && cachedCoord != null) {
            return new Chunk3D(cachedCoord);
        }
        BlockPos pos = this.getBlockPos();
        return new Chunk3D(this.getLevel().dimension(), SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }
}