package mekanism.common.tile.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.common.Mekanism;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_client.PacketUpdateTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.interfaces.ITileWrapper;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.ProblemReporter.PathElement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/// Extension of TileEntity that adds various helpers we use across the majority of our Tiles even those that are not an instance of TileEntityMekanism. Additionally, we
/// improve the performance of markDirty by not firing neighbor updates unless the markDirtyComparator method is overridden.
public abstract class TileEntityUpdateable extends BlockEntity implements ITileWrapper {

    @Nullable
    private GlobalPos cachedCoord;
    private boolean cacheCoord;
    private long lastSave;
    private final long worldPositionLong;
    @Nullable
    private PathElement cachedProblemPath = null;

    public TileEntityUpdateable(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state) {
        super(type.get(), pos, state);
        this.worldPositionLong = pos.asLong();
    }

    /// Collects all data component types that should be persisted to the dropped item. Override this for any conditionally applied component types.
    public List<DataComponentType<?>> getRemapEntries() {
        return new ArrayList<>(collectComponents().keySet());
    }

    /// Called when block is placed in world
    public void onAdded(Level level) {
    }

    /// Call this for tiles that we may call [#getTileGlobalPos()] a fair amount on to cache the coord when position/world information changes.
    protected void cacheCoord() {
        //Mark that we want to cache the coord and then update the coord if needed
        cacheCoord = true;
        updateCoord();
    }

    public long getGameTime() {
        //TODO - 26.1: Re-evaluate this impl
        return level == null ? 0 : level.getGameTime();
    }

    /// Like [Level#isClientSide()], but for when you _know_ world won't be null
    public boolean isRemote() {
        return Objects.requireNonNull(level, "isRemote called before world set").isClientSide();
    }

    /// Called when the tile is permanently removed
    ///
    /// @implNote We only need to handle logic that happens when removed and not unloaded as if it happens for both then setRemoved will handle it
    //TODO - 26.1: verify this works as intended - does the drop contain the contents?
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    }

    /// Used for checking if we need to update comparators.
    ///
    /// @apiNote Only call on the server
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
            if (updateComparator && !level.isClientSide()) {
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
    public final CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        //TODO - 26.1: Is this fine for how to create the problem reporter?
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath(), Mekanism.logger)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, provider);
            writeUpdatedTag(output);
            return output.buildResult();
        }
    }

    //todo - 26.1 - did we _need_ to change this to ValueOutput?
    protected void writeUpdatedTag(ValueOutput output) {
        writeReducedUpdatedTag(output);
    }

    /// Similar to [#getUpdateTag(HolderLookup.Provider)] but with reduced information for when we are doing our own syncing.
    public void writeReducedUpdatedTag(ValueOutput output) {
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        //Handle the update tag when we are on the client
        //TODO - 26.1: Do we need to check if it is empty in any way?
        /*CompoundTag tag = pkt.getTag();
        if (!tag.isEmpty()) {*/
        handleUpdateTag(input);
    }

    public void sendUpdatePacket() {
        sendUpdatePacket(this);
    }

    public void sendUpdatePacket(BlockEntity tracking) {
        Level level = tracking.getLevel();
        if (level == null) {
            Mekanism.logger.warn("Update packet call requested for a tile without a level", new IllegalStateException());
        } else if (level.isClientSide()) {
            Mekanism.logger.warn("Update packet call requested from client side", new IllegalStateException());
        } else if (isRemoved()) {
            Mekanism.logger.warn("Update packet call requested for removed tile", new IllegalStateException());
        } else if (PacketUtils.hasPlayersTracking((ServerLevel) level, tracking.getBlockPos())) {
            //Note: We use our own update packet/channel to avoid chunk trashing and minecraft attempting to rerender
            // the entire chunk when most often we are just updating a TileEntityRenderer, so the chunk itself
            // does not need to and should not be redrawn
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath(), Mekanism.logger)) {
                TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
                writeReducedUpdatedTag(output);
                PacketUtils.sendToAllTracking(new PacketUpdateTile(getBlockPos(), output.buildResult()), tracking);
            }
        }
    }

    protected void updateModelData() {
        requestModelDataUpdate();
        WorldUtils.updateBlock(level, getBlockPos(), getBlockState());
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        updateCoord();
    }

    @Override
    public void setLevel(Level world) {
        super.setLevel(world);
        updateCoord();
        //TODO - 26.1: Do we need to clear the BlockCapabilityCaches we are storing if the level changes? Probably
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
    public PathElement problemPath() {
        if (cachedProblemPath != null) {
            return cachedProblemPath;
        }
        return cachedProblemPath = super.problemPath();
    }
}