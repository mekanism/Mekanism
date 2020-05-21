package mekanism.common.tile.prefab;

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
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
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
import net.minecraft.world.World;

public abstract class TileEntityMultiblock<T extends MultiblockData> extends TileEntityMekanism implements IMultiblock<T>, IConfigurable {

    private Structure structure = Structure.INVALID;

    private T defaultMultiblock = createMultiblock();

    /**
     * This multiblock's previous "has structure" state.
     */
    private boolean prevStructure;

    /**
     * Whether or not this multiblock segment is rendering the structure.
     */
    public boolean isMaster;

    /**
     * This multiblock segment's cached data
     */
    protected MultiblockCache<T> cachedData;

    /**
     * This multiblock segment's cached inventory ID
     */
    @Nullable
    protected UUID cachedID = null;

    // start at 100 to make sure we run the animation
    private long unformedTicks = 100;

    public TileEntityMultiblock(IBlockProvider blockProvider) {
        super(blockProvider);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE_CAPABILITY, this));
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
    public T getDefaultData() {
        return defaultMultiblock;
    }

    @Override
    protected void onUpdateClient() {
        super.onUpdateClient();
        if (!getMultiblock().isFormed()) {
            unformedTicks++;
            if (!playersUsing.isEmpty()) {
                for (PlayerEntity player : new ObjectOpenHashSet<>(playersUsing)) {
                    player.closeScreen();
                }
            }
        } else {
            unformedTicks = 0;
        }
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (ticker >= 1)
            structure.tick(this);
        if (!getMultiblock().isFormed()) {
            playersUsing.forEach(PlayerEntity::closeScreen);

            if (cachedID != null) {
                getManager().updateCache(this);
            }
            if (prevStructure) {
                structureChanged();
                prevStructure = false;
            }
            isMaster = false;
        } else {
            if (!prevStructure) {
                structureChanged();
                prevStructure = true;
            }
            if (getMultiblock().inventoryID != null) {
                cachedID = getMultiblock().inventoryID;
                getManager().updateCache(this);
                if (isMaster) {
                    if (getMultiblock().tick(world)) {
                        sendUpdatePacket();
                    }
                    // mark the chunk dirty each tick to make sure we save
                    markDirty(false);
                }
            }
        }
    }

    private void structureChanged() {
        invalidateCachedCapabilities();
        if (getMultiblock().isFormed() && !getMultiblock().hasMaster && canBeMaster()) {
            getMultiblock().hasMaster = true;
            isMaster = true;
            //Force update the structure's comparator level as it may be incorrect due to not having a capacity while unformed
            getMultiblock().forceUpdateComparatorLevel();
            //If we are the block that is rendering the structure make sure to tell all the valves to update their comparator levels
            getMultiblock().notifyAllUpdateComparator(world);
        }
        for (Direction side : EnumUtils.DIRECTIONS) {
            BlockPos pos = getPos().offset(side);
            if (!getMultiblock().isFormed() || (!getMultiblock().locations.contains(pos) && !getMultiblock().internalLocations.contains(pos))) {
                TileEntity tile = MekanismUtils.getTileEntity(world, pos);
                if (!world.isAirBlock(pos) && (tile == null || tile.getClass() != getClass()) && !(tile instanceof IStructuralMultiblock || tile instanceof IMultiblock)) {
                    MekanismUtils.notifyNeighborofChange(world, pos, getPos());
                }
            }
        }
        sendUpdatePacket();
        if (!getMultiblock().isFormed()) {
            //If we have no structure just mark the comparator as dirty for each block,
            // this will only perform neighbor updates if the block supports comparators
            markDirtyComparator();
        }
    }

    protected boolean canBeMaster() {
        return true;
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (player.isSneaking() || !getMultiblock().isFormed()) {
            return ActionResultType.PASS;
        }
        return openGui(player);
    }

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
            structure.invalidate(world);
            if (cachedID != null) {
                getManager().invalidate(this);
            }
        }
    }

    @Override
    public void resetCache() {
        cachedID = null;
        cachedData = null;
    }

    @Override
    public UUID getCacheID() {
        return cachedID;
    }

    @Override
    public MultiblockCache<T> getCache() {
        return cachedData;
    }

    @Override
    public void setCache(MultiblockCache<T> cache) {
        this.cachedData = cache;
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putBoolean(NBTConstants.RENDERING, isMaster);
        updateTag.putBoolean(NBTConstants.HAS_STRUCTURE, getMultiblock().isFormed());
        if (getMultiblock().isFormed() && isMaster) {
            updateTag.putInt(NBTConstants.HEIGHT, getMultiblock().height);
            updateTag.putInt(NBTConstants.WIDTH, getMultiblock().width);
            updateTag.putInt(NBTConstants.LENGTH, getMultiblock().length);
            if (getMultiblock().renderLocation != null) {
                updateTag.put(NBTConstants.RENDER_LOCATION, NBTUtil.writeBlockPos(getMultiblock().renderLocation));
            }
            if (getMultiblock().inventoryID != null) {
                updateTag.putUniqueId(NBTConstants.INVENTORY_ID, getMultiblock().inventoryID);
            }
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.RENDERING, value -> isMaster = value);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.HAS_STRUCTURE, value -> getMultiblock().setFormedForce(value));
        if (isMaster) {
            if (getMultiblock().isFormed()) {
                NBTUtils.setIntIfPresent(tag, NBTConstants.HEIGHT, value -> getMultiblock().height = value);
                NBTUtils.setIntIfPresent(tag, NBTConstants.WIDTH, value -> getMultiblock().width = value);
                NBTUtils.setIntIfPresent(tag, NBTConstants.LENGTH, value -> getMultiblock().length = value);
                NBTUtils.setBlockPosIfPresent(tag, NBTConstants.RENDER_LOCATION, value -> getMultiblock().renderLocation = value);
                if (tag.hasUniqueId(NBTConstants.INVENTORY_ID)) {
                    getMultiblock().inventoryID = tag.getUniqueId(NBTConstants.INVENTORY_ID);
                } else {
                    getMultiblock().inventoryID = null;
                }
                if (getMultiblock().renderLocation != null && !prevStructure && unformedTicks >= 10) {
                    Mekanism.proxy.doMultiblockSparkle(this, getMultiblock().renderLocation, getMultiblock().length - 1, getMultiblock().width - 1, getMultiblock().height - 1);
                }
            } else {
                // this will consecutively be set on the server
                isMaster = false;
            }
        }
        prevStructure = getMultiblock().isFormed();
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (!getMultiblock().isFormed() && nbtTags.hasUniqueId(NBTConstants.INVENTORY_ID)) {
            cachedID = nbtTags.getUniqueId(NBTConstants.INVENTORY_ID);
            if (nbtTags.contains(NBTConstants.CACHE)) {
                cachedData = getManager().getNewCache();
                cachedData.load(nbtTags.getCompound(NBTConstants.CACHE));
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (cachedID != null) {
            nbtTags.putUniqueId(NBTConstants.INVENTORY_ID, cachedID);
            if (cachedData != null) {
                // sync one last time if this is the master
                if (getMultiblock().isFormed()) {
                    cachedData.sync(getMultiblock());
                }
                CompoundNBT cacheTags = new CompoundNBT();
                cachedData.save(cacheTags);
                nbtTags.put(NBTConstants.CACHE, cacheTags);

            }
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
        if (getMultiblock().isFormed() && isMaster && getMultiblock().renderLocation != null) {
            //TODO: Eventually we may want to look into caching this
            BlockPos corner1 = getMultiblock().renderLocation;
            //height - 2 up, but then we go up one further to take into account that block
            BlockPos corner2 = corner1.east(getMultiblock().length + 1).south(getMultiblock().width + 1).up(getMultiblock().height - 1);
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

    @Override
    public BlockPos getTilePos() {
        return getPos();
    }

    @Override
    public World getTileWorld() {
        return getWorld();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        return side -> getMultiblock().getInventorySlots(side);
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (!getWorld().isRemote() && !getMultiblock().isFormed()) {
            FormationResult result = getStructure().runUpdate(this);
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