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
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockData;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.multiblock.UpdateProtocol.FormationResult;
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

public abstract class TileEntityMultiblock<T extends MultiblockData> extends TileEntityMekanism implements IMultiblock<T>, IConfigurable {

    /**
     * The multiblock data for this structure.
     */
    private T structure = getNewStructure();

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

    private Map<BlockPos, BlockState> cachedNeighbors = new HashMap<>();

    public TileEntityMultiblock(IBlockProvider blockProvider) {
        super(blockProvider);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
    }

    @Override
    public void removeMultiblock() {
        structure.remove(getWorld());
        structure = getNewStructure();
        invalidateCachedCapabilities();
    }

    @Override
    public T getMultiblock() {
        return structure;
    }

    @Override
    public void setMultiblock(T multiblock) {
        structure = multiblock;
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
        if (!structure.isFormed()) {
            playersUsing.forEach(p -> p.closeScreen());

            if (cachedID != null) {
                getManager().updateCache(this, false);
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
            structure.didUpdateThisTick = false;
            if (isRendering && structure.inventoryID != null) {
                cachedData.sync(structure);
                cachedID = structure.inventoryID;
                getManager().updateCache(this, false);
                if (structure.tick(world)) {
                    sendUpdatePacket();
                }
                // mark the chunk dirty each tick to make sure we save
                markDirty(false);
            }
        }

        if (ticker >= 5 && updateRequested != null) {
            runUpdate();
        }
        protocolUpdateThisTick = false;
    }

    private void structureChanged() {
        if (structure.isFormed() && !structure.hasRenderer) {
            structure.hasRenderer = true;
            isRendering = true;
            //Force update the structure's comparator level as it may be incorrect due to not having a capacity while unformed
            structure.forceUpdateComparatorLevel();
            //If we are the block that is rendering the structure make sure to tell all the valves to update their comparator levels
            structure.notifyAllUpdateComparator(world);
        }
        for (Direction side : EnumUtils.DIRECTIONS) {
            BlockPos pos = getPos().offset(side);
            if (!structure.isFormed() || (!structure.locations.contains(pos) && !structure.internalLocations.contains(pos))) {
                TileEntity tile = MekanismUtils.getTileEntity(world, pos);
                if (!world.isAirBlock(pos) && (tile == null || tile.getClass() != getClass()) && !(tile instanceof IStructuralMultiblock || tile instanceof IMultiblock)) {
                    MekanismUtils.notifyNeighborofChange(world, pos, getPos());
                }
            }
        }
        sendUpdatePacket();
        if (!structure.isFormed()) {
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
        if (!protocolUpdateThisTick && (!structure.isFormed() || !structure.didUpdateThisTick)) {
            if (structure.isFormed() && structure.inventoryID != null) {
                // update the cache before we destroy the multiblock
                cachedData.sync(structure);
                cachedID = structure.inventoryID;
                getManager().updateCache(this, true);
            }
            getProtocol().doUpdate(updateRequested);
            if (structure.isFormed()) {
                structure.didUpdateThisTick = true;
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
        if (player.isSneaking() || !structure.isFormed()) {
            return ActionResultType.PASS;
        }
        return openGui(player);
    }

    @Nonnull
    public abstract T getNewStructure();

    public abstract UpdateProtocol<T> getProtocol();

    @Override
    public void resetCache() {
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
        updateTag.putBoolean(NBTConstants.HAS_STRUCTURE, structure.isFormed());
        if (structure.isFormed() && isRendering) {
            updateTag.putInt(NBTConstants.HEIGHT, structure.height);
            updateTag.putInt(NBTConstants.WIDTH, structure.width);
            updateTag.putInt(NBTConstants.LENGTH, structure.length);
            if (structure.renderLocation != null) {
                updateTag.put(NBTConstants.RENDER_LOCATION, NBTUtil.writeBlockPos(structure.renderLocation));
            }
            if (structure.inventoryID != null) {
                updateTag.putUniqueId(NBTConstants.INVENTORY_ID, structure.inventoryID);
            }
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.RENDERING, value -> isRendering = value);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.HAS_STRUCTURE, value -> structure.setFormedForce(value));
        if (isRendering) {
            if (structure.isFormed()) {
                NBTUtils.setIntIfPresent(tag, NBTConstants.HEIGHT, value -> structure.height = value);
                NBTUtils.setIntIfPresent(tag, NBTConstants.WIDTH, value -> structure.width = value);
                NBTUtils.setIntIfPresent(tag, NBTConstants.LENGTH, value -> structure.length = value);
                NBTUtils.setBlockPosIfPresent(tag, NBTConstants.RENDER_LOCATION, value -> structure.renderLocation = value);
                if (tag.hasUniqueId(NBTConstants.INVENTORY_ID)) {
                    structure.inventoryID = tag.getUniqueId(NBTConstants.INVENTORY_ID);
                } else {
                    structure.inventoryID = null;
                }
                if (structure.renderLocation != null && !prevStructure) {
                    Mekanism.proxy.doMultiblockSparkle(this, structure.renderLocation, structure.length - 1, structure.width - 1, structure.height - 1);
                }
            } else {
                // this will consecutively be set on the server
                isRendering = false;
            }
        }
        prevStructure = structure.isFormed();
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (!structure.isFormed() && nbtTags.hasUniqueId(NBTConstants.INVENTORY_ID)) {
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
            if (structure.isFormed()) {
                cachedData.sync(structure);
            }
            cachedData.save(nbtTags);
        }
        return nbtTags;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        SyncMapper.setup(container, getMultiblock().getClass(), () -> getMultiblock());
    }

    @Nonnull
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (getMultiblock().isFormed() && isRendering && structure.renderLocation != null) {
            //TODO: Eventually we may want to look into caching this
            BlockPos corner1 = structure.renderLocation;
            //height - 2 up, but then we go up one further to take into account that block
            BlockPos corner2 = corner1.east(structure.length + 1).south(structure.width + 1).up(structure.height - 1);
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
        return side -> structure.getInventorySlots(side);
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (!getWorld().isRemote() && !structure.isFormed()) {
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