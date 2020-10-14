package mekanism.common.tile.prefab;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public abstract class TileEntityMultiblock<T extends MultiblockData> extends TileEntityMekanism implements IMultiblock<T>, IConfigurable {

    private Structure structure = Structure.INVALID;

    private final T defaultMultiblock = createMultiblock();

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
        if (ticker >= 3) {
            structure.tick(this);
        }
        T multiblock = getMultiblock();
        if (multiblock.isFormed()) {
            if (!prevStructure) {
                structureChanged();
                prevStructure = true;
            }
            if (multiblock.inventoryID != null) {
                cachedID = multiblock.inventoryID;
                getManager().updateCache(this, multiblock);
                if (isMaster) {
                    if (multiblock.tick(world)) {
                        sendUpdatePacket();
                    }
                    // mark the chunk dirty each tick to make sure we save
                    markDirty(false);
                }
            }
        } else {
            playersUsing.forEach(PlayerEntity::closeScreen);
            if (cachedID != null) {
                getManager().updateCache(this, multiblock);
            }
            if (prevStructure) {
                structureChanged();
                prevStructure = false;
            }
            isMaster = false;
        }
        onUpdateServer(multiblock);
    }

    protected void onUpdateServer(T multiblock) {
    }

    private void structureChanged() {
        invalidateCachedCapabilities();
        T multiblock = getMultiblock();
        if (multiblock.isFormed() && !multiblock.hasMaster && canBeMaster()) {
            multiblock.hasMaster = true;
            isMaster = true;
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
                    MekanismUtils.notifyNeighborOfChange(world, pos, getPos());
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
    public boolean canBeMaster() {
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
    protected void dumpRadiation() {
        //NO-OP we handle dumping radiation separately for multiblocks
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        unload();
    }

    private void unload() {
        if (!isRemote()) {
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
        T multiblock = getMultiblock();
        updateTag.putBoolean(NBTConstants.HAS_STRUCTURE, multiblock.isFormed());
        if (multiblock.isFormed() && isMaster) {
            multiblock.writeUpdateTag(updateTag);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.RENDERING, value -> isMaster = value);
        T multiblock = getMultiblock();
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.HAS_STRUCTURE, multiblock::setFormedForce);
        if (isMaster) {
            if (multiblock.isFormed()) {
                multiblock.readUpdateTag(tag);
                if (multiblock.renderLocation != null && !prevStructure && unformedTicks >= 5) {
                    Mekanism.proxy.doMultiblockSparkle(this, multiblock.renderLocation, multiblock.length() - 1, multiblock.width() - 1, multiblock.height() - 1);
                }
            } else {
                // this will consecutively be set on the server
                isMaster = false;
            }
        }
        prevStructure = multiblock.isFormed();
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbtTags) {
        super.read(state, nbtTags);
        if (!getMultiblock().isFormed()) {
            NBTUtils.setUUIDIfPresent(nbtTags, NBTConstants.INVENTORY_ID, id -> {
                cachedID = id;
                if (nbtTags.contains(NBTConstants.CACHE, NBT.TAG_COMPOUND)) {
                    cachedData = getManager().createCache();
                    cachedData.load(nbtTags.getCompound(NBTConstants.CACHE));
                }
            });
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(@Nonnull CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (cachedID != null) {
            nbtTags.putUniqueId(NBTConstants.INVENTORY_ID, cachedID);
            if (cachedData != null) {
                // sync one last time if this is the master
                T multiblock = getMultiblock();
                if (multiblock.isFormed()) {
                    cachedData.sync(multiblock);
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
        if (isMaster) {
            T multiblock = getMultiblock();
            if (multiblock.isFormed() && multiblock.getBounds() != null) {
                //TODO: Eventually we may want to look into caching this
                //Note: We do basically the full dimensions as it still is a lot smaller than always rendering it, and makes sure no matter
                // how the specific multiblock wants to render, that it is being viewed
                return new AxisAlignedBB(multiblock.getMinPos(), multiblock.getMaxPos().add(1, 1, 1));
            }
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
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        //TODO - V11: Make this properly support removing blocks from the "inside" and rechecking the structure
        // For now we "ignore" this case as the structure can be rechecked manually with a configurator
        // and checking on every neighbor changed when we don't have a multiblock (so don't know its bounds)
        // would not be very performant
        if (!isRemote()) {
            T multiblock = getMultiblock();
            if (multiblock.isPositionInsideBounds(getStructure(), neighborPos)) {
                //If the neighbor change happened from inside the bounds of the multiblock,
                if (!multiblock.innerNodes.contains(neighborPos) || world.isAirBlock(neighborPos)) {
                    //And we are not already an internal part of the structure, or we are changing an internal part to air
                    // then we mark the structure as needing to be re-validated
                    //Note: This isn't a super accurate check as if a node gets replaced by command or mod with say dirt
                    // it won't know to invalidate it but oh well. (See java docs on innerNode for more caveats)
                    getStructure().markForUpdate(world, true);
                }
            }
        }
    }

    @Override
    public ActionResultType onRightClick(PlayerEntity player, Direction side) {
        if (!isRemote() && !getMultiblock().isFormed()) {
            FormationResult result = getStructure().runUpdate(this);
            if (!result.isFormed() && result.getResultText() != null) {
                player.sendMessage(result.getResultText(), Util.DUMMY_UUID);
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