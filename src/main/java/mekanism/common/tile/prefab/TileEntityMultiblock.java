package mekanism.common.tile.prefab;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.text.EnumColor;
import mekanism.client.SparkleAnimation;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.BoundComputerMethod;
import mekanism.common.integration.computer.ComputerMethodMapper;
import mekanism.common.integration.computer.ComputerMethodMapper.MethodRestriction;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class TileEntityMultiblock<T extends MultiblockData> extends TileEntityMekanism implements IMultiblock<T>, IConfigurable {

    private Structure structure = Structure.INVALID;

    private final T defaultMultiblock = createMultiblock();

    /**
     * This multiblock's previous "has structure" state.
     */
    private boolean prevStructure;

    /**
     * Whether this multiblock segment is rendering the structure.
     */
    private boolean isMaster;

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

    public TileEntityMultiblock(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        cacheCoord();
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
                for (Player player : new ObjectOpenHashSet<>(playersUsing)) {
                    player.closeContainer();
                }
            }
        } else {
            unformedTicks = 0;
        }
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        boolean needsPacket = false;
        if (ticker >= 3) {
            structure.tick(this, ticker % 10 == 0);
        }
        T multiblock = getMultiblock();
        if (isMaster() && multiblock.isFormed() && multiblock.recheckStructure) {
            multiblock.recheckStructure = false;
            getStructure().doImmediateUpdate(this, ticker % 10 == 0);
            multiblock = getMultiblock();
        }
        if (multiblock.isFormed()) {
            if (!prevStructure) {
                structureChanged(multiblock);
                prevStructure = true;
                needsPacket = true;
            }
            if (multiblock.inventoryID != null) {
                cachedID = multiblock.inventoryID;
                getManager().updateCache(this, multiblock);
                if (isMaster()) {
                    if (multiblock.tick(level)) {
                        needsPacket = true;
                    }
                    if (multiblock.isDirty()) {
                        //If the multiblock is dirty mark the chunk as dirty to ensure that we save and then reset the fact the multiblock is dirty
                        markForSave();
                        multiblock.resetDirty();
                    }
                }
            }
        } else {
            playersUsing.forEach(Player::closeContainer);
            if (cachedID != null) {
                getManager().updateCache(this, multiblock);
            }
            if (prevStructure) {
                structureChanged(multiblock);
                prevStructure = false;
                needsPacket = true;
            }
            isMaster = false;
        }
        needsPacket |= onUpdateServer(multiblock);
        if (needsPacket) {
            sendUpdatePacket();
        }
    }

    /**
     * @return if we need an update packet
     */
    protected boolean onUpdateServer(T multiblock) {
        return false;
    }

    @Override
    public void resetForFormed() {
        //TODO: Note, this seems to work fine as is, but there is a chance that we also need
        // to be updating the cache using the old multiblock to allow for it to save properly
        //Clear this multiblock being master, and also mark it as we don't have a structure
        // as this method is only called when we have a formed multiblock so we want to just
        // treat it as us unforming if formed and then reforming
        isMaster = false;
        prevStructure = false;
    }

    protected void structureChanged(T multiblock) {
        invalidateCachedCapabilities();
        if (multiblock.isFormed() && !multiblock.hasMaster && canBeMaster()) {
            multiblock.hasMaster = true;
            isMaster = true;
            //Force update the structure's comparator level as it may be incorrect due to not having a capacity while unformed
            multiblock.forceUpdateComparatorLevel();
            //If we are the block that is rendering the structure make sure to tell all the valves to update their comparator levels
            multiblock.notifyAllUpdateComparator(level);
        }
        for (Direction side : EnumUtils.DIRECTIONS) {
            BlockPos pos = getBlockPos().relative(side);
            if (!multiblock.isFormed() || !multiblock.isKnownLocation(pos)) {
                BlockEntity tile = WorldUtils.getTileEntity(level, pos);
                if (!level.isEmptyBlock(pos) && (tile == null || tile.getClass() != getClass()) && !(tile instanceof IStructuralMultiblock || tile instanceof IMultiblock)) {
                    WorldUtils.notifyNeighborOfChange(level, pos, getBlockPos());
                }
            }
        }
        if (!multiblock.isFormed()) {
            //If we have no structure just mark the comparator as dirty for each block,
            // this will only perform neighbor updates if the block supports comparators
            markDirtyComparator();
        }
    }

    @Override
    protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
        //Comparators are handled via the multiblock, no special listeners are needed
        return false;
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }

    @Override
    public InteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
        if (player.isShiftKeyDown() || !getMultiblock().isFormed()) {
            return InteractionResult.PASS;
        }
        return openGui(player);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!isRemote()) {
            structure.invalidate(level);
            if (cachedID != null) {
                getManager().invalidate(this);
            }
        }
    }

    @Override
    public boolean shouldDumpRadiation() {
        //We handle dumping radiation separately for multiblocks
        return false;
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
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        updateTag.putBoolean(NBTConstants.RENDERING, isMaster());
        T multiblock = getMultiblock();
        updateTag.putBoolean(NBTConstants.HAS_STRUCTURE, multiblock.isFormed());
        if (multiblock.isFormed() && isMaster()) {
            multiblock.writeUpdateTag(updateTag);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.RENDERING, value -> isMaster = value);
        T multiblock = getMultiblock();
        NBTUtils.setBooleanIfPresent(tag, NBTConstants.HAS_STRUCTURE, multiblock::setFormedForce);
        if (isMaster()) {
            if (multiblock.isFormed()) {
                multiblock.readUpdateTag(tag);
                doMultiblockSparkle(multiblock);
            } else {
                // this will consecutively be set on the server
                isMaster = false;
            }
        }
        prevStructure = multiblock.isFormed();
    }

    /**
     * Only call on the client
     */
    private void doMultiblockSparkle(T multiblock) {
        if (isRemote() && multiblock.renderLocation != null && !prevStructure && unformedTicks >= 5) {
            //If player is within 40 blocks (1,600 = 40^2), show the status message/sparkles
            //Note: Do not change this from ClientPlayerEntity to PlayerEntity, or it will cause class loading issues on the server
            // due to trying to validate if the value is actually a PlayerEntity
            LocalPlayer player = Minecraft.getInstance().player;
            if (worldPosition.distSqr(player.blockPosition()) <= 1_600) {
                if (MekanismConfig.client.enableMultiblockFormationParticles.get()) {
                    new SparkleAnimation(this, multiblock.renderLocation, multiblock.length() - 1, multiblock.width() - 1, multiblock.height() - 1).run();
                } else {
                    player.displayClientMessage(MekanismLang.MULTIBLOCK_FORMED_CHAT.translateColored(EnumColor.INDIGO), true);
                }
            }
        }
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        if (!getMultiblock().isFormed()) {
            NBTUtils.setUUIDIfPresent(nbt, NBTConstants.INVENTORY_ID, id -> {
                cachedID = id;
                if (nbt.contains(NBTConstants.CACHE, Tag.TAG_COMPOUND)) {
                    cachedData = getManager().createCache();
                    cachedData.load(nbt.getCompound(NBTConstants.CACHE));
                }
            });
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        if (cachedID != null) {
            nbtTags.putUUID(NBTConstants.INVENTORY_ID, cachedID);
            if (cachedData != null) {
                // sync one last time if this is the master
                T multiblock = getMultiblock();
                if (multiblock.isFormed()) {
                    cachedData.sync(multiblock);
                }
                CompoundTag cacheTags = new CompoundTag();
                cachedData.save(cacheTags);
                nbtTags.put(NBTConstants.CACHE, cacheTags);

            }
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        SyncMapper.INSTANCE.setup(container, getMultiblock().getClass(), this::getMultiblock);
    }

    @Nonnull
    @Override
    public AABB getRenderBoundingBox() {
        if (isMaster()) {
            T multiblock = getMultiblock();
            if (multiblock.isFormed() && multiblock.getBounds() != null) {
                //TODO: Eventually we may want to look into caching this
                //Note: We do basically the full dimensions as it still is a lot smaller than always rendering it, and makes sure no matter
                // how the specific multiblock wants to render, that it is being viewed
                return new AABB(multiblock.getMinPos(), multiblock.getMaxPos().offset(1, 1, 1));
            }
        }
        return super.getRenderBoundingBox();
    }

    @Override
    public boolean persistInventory() {
        return false;
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        return side -> getMultiblock().getInventorySlots(side);
    }

    @Override
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        //TODO - V11: Make this properly support changing blocks inside the structure when they aren't touching any part of the multiblocks
        if (!isRemote()) {
            T multiblock = getMultiblock();
            if (multiblock.isPositionInsideBounds(getStructure(), neighborPos)) {
                //If the neighbor change happened from inside the bounds of the multiblock,
                if (level.isEmptyBlock(neighborPos) || !multiblock.internalLocations.contains(neighborPos)) {
                    //And we are not already an internal part of the structure, or we are changing an internal part to air
                    // then we mark the structure as needing to be re-validated
                    //Note: This isn't a super accurate check as if a node gets replaced by command or mod with say dirt
                    // it won't know to invalidate it but oh well. (See java docs on internalLocations for more caveats)
                    getStructure().markForUpdate(level, true);
                }
            }
        }
    }

    @Override
    public InteractionResult onRightClick(Player player) {
        if (!isRemote() && !getMultiblock().isFormed()) {
            FormationResult result = getStructure().runUpdate(this);
            if (!result.isFormed() && result.getResultText() != null) {
                player.sendMessage(result.getResultText(), Util.NIL_UUID);
                return InteractionResult.sidedSuccess(isRemote());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        return InteractionResult.PASS;
    }

    //Methods relating to IComputerTile
    public boolean exposesMultiblockToComputer() {
        return true;
    }

    @Override
    public boolean isComputerCapabilityPersistent() {
        //We are not persistent regardless of if our tile has support, unless we don't expose the multiblock itself to the computer
        return !exposesMultiblockToComputer() && super.isComputerCapabilityPersistent();
    }

    @Override
    public void getComputerMethods(Map<String, BoundComputerMethod> methods) {
        super.getComputerMethods(methods);
        if (exposesMultiblockToComputer()) {
            T multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                //Only expose the multiblock's methods if we are formed, when the formation state changes
                // our capabilities are invalidated, so should end up getting rechecked and this called by
                // the various computer integration mods, and allow us to only expose the multiblock's methods
                // as even existing if the multiblock is complete
                ComputerMethodMapper.INSTANCE.getAndBindToHandler(multiblock, methods);
            }
        }
    }

    @ComputerMethod(restriction = MethodRestriction.MULTIBLOCK)
    private boolean isFormed() {
        return getMultiblock().isFormed();
    }
    //End methods IComputerTile
}