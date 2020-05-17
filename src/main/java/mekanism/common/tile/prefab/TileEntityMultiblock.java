package mekanism.common.tile.prefab;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.IConfigurable;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.resolver.basic.BasicCapabilityResolver;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.mesh.IMeshNode;
import mekanism.common.lib.mesh.Structure;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.UpdateProtocol;
import mekanism.common.lib.multiblock.UpdateProtocol.FormationResult;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public abstract class TileEntityMultiblock<T extends MultiblockData> extends TileEntityMekanism implements IMultiblock<T>, IConfigurable, IMeshNode {

    /**
     * The multiblock data for this structure.
     */
    private T multiblock = getNewStructure();

    private Structure structure = Structure.INVALID;

    /**
     * This multiblock's previous "has structure" state.
     */
    private boolean prevStructure;

    /**
     * Whether or not this multiblock segment is rendering the structure.
     */
    public boolean isRendering;

    /**
     * This multiblock segment's cached data
     */
    protected MultiblockCache<T> cachedData = getManager().getNewCache();

    /**
     * This multiblock segment's cached inventory ID
     */
    @Nullable
    protected UUID cachedID = null;

    /**
     * If we've already run a protocol update that touches this block on this tick.
     */
    private boolean protocolUpdateThisTick;

    private UpdateType updateRequested;

    private final Map<BlockPos, BlockState> cachedNeighbors = new HashMap<>();

    public TileEntityMultiblock(IBlockProvider blockProvider) {
        super(blockProvider);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
    }

    @Override
    public void removeMultiblock() {
        multiblock.remove(getWorld());
        multiblock = getNewStructure();
        invalidateCachedCapabilities();
    }

    @Override
    public T getMultiblock() {
        return multiblock;
    }

    @Override
    public void setMultiblock(T multiblock) {
        this.multiblock = multiblock;
    }

    @Override
    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    @Override
    public Structure getStructure() {
        return structure;
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (!getMultiblock().isFormed() && !playersUsing.isEmpty()) {
            for (PlayerEntity player : new ObjectOpenHashSet<>(playersUsing)) {
                player.closeScreen();
            }
        }
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (ticker == 1 && cachedNeighbors.isEmpty()) {
            createNeighborCache();
        }
        if (ticker >= 1) {
            structure.tick(this);
        }
        if (!multiblock.isFormed()) {
            playersUsing.forEach(PlayerEntity::closeScreen);

            if (cachedID != null) {
                getManager().updateCache(this);
            }
            if (ticker == 3) {
                updateRequested = UpdateType.INITIAL;
            }
            if (prevStructure) {
                structureChanged();
                prevStructure = false;
            }
            isRendering = false;
        } else {
            if (!prevStructure) {
                structureChanged();
                prevStructure = true;
            }
            multiblock.didUpdateThisTick = false;
            if (multiblock.inventoryID != null) {
                cachedData.sync(multiblock);
                cachedID = multiblock.inventoryID;
                getManager().updateCache(this);
                if (isRendering) {
                    if (multiblock.tick(world)) {
                        sendUpdatePacket();
                    }
                    // mark the chunk dirty each tick to make sure we save
                    markDirty(false);
                }
            }
        }

        if (ticker >= 5 && updateRequested != null) {
            runUpdate();
        }
        protocolUpdateThisTick = false;
    }

    private void structureChanged() {
        if (multiblock.isFormed() && !multiblock.hasRenderer) {
            multiblock.hasRenderer = true;
            isRendering = true;
            //Force update the structure's comparator level as it may be incorrect due to not having a capacity while unformed
            multiblock.forceUpdateComparatorLevel();
            //If we are the block that is rendering the structure make sure to tell all the valves to update their comparator levels
            multiblock.notifyAllUpdateComparator(world);
        }
        for (Direction side : EnumUtils.DIRECTIONS) {
            BlockPos pos = getPos().offset(side);
            if (!multiblock.isFormed() || (!multiblock.locations.contains(pos) && !multiblock.internalLocations.contains(pos))) {
                TileEntity tile = MekanismUtils.getTileEntity(world, pos);
                if (!world.isAirBlock(pos) && (tile == null || tile.getClass() != getClass()) && !(tile instanceof IStructuralMultiblock || tile instanceof IMultiblock)) {
                    MekanismUtils.notifyNeighborofChange(world, pos, getPos());
                }
            }
        }
        sendUpdatePacket();
        if (!multiblock.isFormed()) {
            //If we have no structure just mark the comparator as dirty for each block,
            // this will only perform neighbor updates if the block supports comparators
            markDirtyComparator();
        }
    }

    @Override
    public void markUpdated() {
        protocolUpdateThisTick = true;
    }

    @Override
    public boolean updatedThisTick() {
        return protocolUpdateThisTick;
    }

    @Override
    public void requestUpdate(BlockPos neighborPos, UpdateType type) {
        if (!isRemote() && (type == UpdateType.FORCE || shouldUpdate(neighborPos))) {
            updateRequested = type;
        }
    }

    private void runUpdate() {
        if (!protocolUpdateThisTick && (!multiblock.isFormed() || !multiblock.didUpdateThisTick)) {
            if (multiblock.isFormed() && multiblock.inventoryID != null) {
                // update the cache before we destroy the multiblock
                cachedData.sync(multiblock);
                cachedID = multiblock.inventoryID;
                getManager().updateCache(this);
            }
            getProtocol().doUpdate(updateRequested);
            if (multiblock.isFormed()) {
                multiblock.didUpdateThisTick = true;
            }
        }
        updateRequested = null;
    }

    @Override
    public Map<BlockPos, BlockState> getNeighborCache() {
        return cachedNeighbors;
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (player.isSneaking() || !multiblock.isFormed()) {
            return ActionResultType.PASS;
        }
        return openGui(player);
    }

    @Nonnull
    public abstract T getNewStructure();

    public abstract UpdateProtocol<T> getProtocol();

    @Override
    public void remove() {
        super.remove();
        unload();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        unload();
    }

    private void unload() {
        if (!world.isRemote()) {
            structure.invalidate();
            if (cachedID != null) {
                getManager().invalidate(this);
            }
        }
    }

    @Override
    public void resetCache() {
        this.onChunkUnloaded();
        cachedID = null;
        cachedData = getManager().getNewCache();
    }

    @Override
    public UUID getCacheID() {
        return cachedID;
    }

    @Override
    public MultiblockCache<T> getCache() {
        return cachedData;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putBoolean(NBTConstants.RENDERING, isRendering);
        updateTag.putBoolean(NBTConstants.HAS_STRUCTURE, multiblock.isFormed());
        if (multiblock.isFormed() && isRendering) {
            updateTag.putInt(NBTConstants.HEIGHT, multiblock.height);
            updateTag.putInt(NBTConstants.WIDTH, multiblock.width);
            updateTag.putInt(NBTConstants.LENGTH, multiblock.length);
            if (multiblock.renderLocation != null) {
                updateTag.put(NBTConstants.RENDER_LOCATION, NBTUtil.writeBlockPos(multiblock.renderLocation));
            }
            if (multiblock.inventoryID != null) {
                updateTag.putUniqueId(NBTConstants.INVENTORY_ID, multiblock.inventoryID);
            }
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.RENDERING, value -> isRendering = value);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.HAS_STRUCTURE, value -> multiblock.setFormedForce(value));
        if (isRendering) {
            if (multiblock.isFormed()) {
                NBTUtils.setIntIfPresent(tag, NBTConstants.HEIGHT, value -> multiblock.height = value);
                NBTUtils.setIntIfPresent(tag, NBTConstants.WIDTH, value -> multiblock.width = value);
                NBTUtils.setIntIfPresent(tag, NBTConstants.LENGTH, value -> multiblock.length = value);
                NBTUtils.setBlockPosIfPresent(tag, NBTConstants.RENDER_LOCATION, value -> multiblock.renderLocation = value);
                if (tag.hasUniqueId(NBTConstants.INVENTORY_ID)) {
                    multiblock.inventoryID = tag.getUniqueId(NBTConstants.INVENTORY_ID);
                } else {
                    multiblock.inventoryID = null;
                }
                if (multiblock.renderLocation != null && !prevStructure) {
                    Mekanism.proxy.doMultiblockSparkle(this, multiblock.renderLocation, multiblock.length - 1, multiblock.width - 1, multiblock.height - 1);
                }
            } else {
                // this will consecutively be set on the server
                isRendering = false;
            }
        }
        prevStructure = multiblock.isFormed();
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (!multiblock.isFormed() && nbtTags.hasUniqueId(NBTConstants.INVENTORY_ID)) {
            cachedID = nbtTags.getUniqueId(NBTConstants.INVENTORY_ID);
            cachedData.load(nbtTags);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (cachedID != null) {
            nbtTags.putUniqueId(NBTConstants.INVENTORY_ID, cachedID);
            if (multiblock.isFormed()) {
                cachedData.sync(multiblock);
            }
            cachedData.save(nbtTags);
        }
        return nbtTags;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        SyncMapper.setup(container, getMultiblock().getClass(), this::getMultiblock);
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (getMultiblock().isFormed() && isRendering && multiblock.renderLocation != null) {
            //TODO: Eventually we may want to look into caching this
            BlockPos corner1 = multiblock.renderLocation;
            //height - 2 up, but then we go up one further to take into account that block
            BlockPos corner2 = corner1.east(multiblock.length + 1).south(multiblock.width + 1).up(multiblock.height - 1);
            //Note: We do basically the full dimensions as it still is a lot smaller than always rendering it, and makes sure no matter
            // how the specific multiblock wants to render, that it is being viewed
            return new AxisAlignedBB(corner1, corner2);
        }
        return super.getRenderBoundingBox();
    }

    @Override
    public boolean persistInventory() {
        return false;
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        return side -> multiblock.getInventorySlots(side);
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (!getWorld().isRemote() && !multiblock.isFormed()) {
            FormationResult result = getProtocol().doUpdate(UpdateType.NORMAL);
            if (!result.isFormed() && result.getResultText() != null) {
                player.sendMessage(result.getResultText());
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public ActionResultType onSneakRightClick(PlayerEntity player, Direction side) {
        return ActionResultType.PASS;
    }
}