package mekanism.common.tile.prefab;

import java.util.HashSet;
import java.util.UUID;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.text.EnumColor;
import mekanism.client.SparkleAnimation;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.BoundMethodHolder;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.MethodRestriction;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * This multiblock segment's cached inventory ID
     */
    @Nullable
    private UUID cachedID = null;

    // start at 100 to make sure we run the animation
    private long unformedTicks = 5L * SharedConstants.TICKS_PER_SECOND;

    public TileEntityMultiblock(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
        cacheCoord();
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
                for (Player player : new HashSet<>(playersUsing)) {
                    player.closeContainer();
                }
            }
        } else {
            unformedTicks = 0;
        }
    }

    @Override
    protected boolean onUpdateServer() {
        boolean needsPacket = super.onUpdateServer();
        if (ticker >= 3) {
            structure.tick(this, ticker % MekanismUtils.TICKS_PER_HALF_SECOND == 0);
        }
        T multiblock = getMultiblock();
        if (isMaster() && multiblock.isFormed() && multiblock.recheckStructure) {
            multiblock.recheckStructure = false;
            getStructure().doImmediateUpdate(this, ticker % MekanismUtils.TICKS_PER_HALF_SECOND == 0);
            T newMultiblock = getMultiblock();
            if (newMultiblock != multiblock && !newMultiblock.isFormed()) {
                //force it to sync if it just unformed
                getManager().handleDirtyMultiblock(multiblock);
            }
            multiblock = newMultiblock;
        }
        if (multiblock.isFormed()) {
            if (!prevStructure) {
                structureChanged(multiblock);
                prevStructure = true;
                needsPacket = true;
            }
            if (multiblock.inventoryID != null) {
                UUID oldCachedID = cachedID;
                cachedID = multiblock.inventoryID;
                if (oldCachedID != cachedID) {
                    markForSave();
                }
                if (isMaster()) {
                    if (multiblock.tick(level)) {
                        needsPacket = true;
                    }
                    getManager().markTicked(multiblock);
                }
            }
        } else {
            if (!playersUsing.isEmpty()) {
                playersUsing.forEach(Player::closeContainer);
            }
            if (prevStructure) {
                structureChanged(multiblock);
                prevStructure = false;
                needsPacket = true;
            }
            isMaster = false;
        }
        needsPacket |= onUpdateServer(multiblock);
        return needsPacket;
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
        invalidateCapabilitiesFull();
        if (multiblock.isFormed() && !multiblock.hasMaster && canBeMaster()) {
            multiblock.hasMaster = true;
            isMaster = true;
            //Force update the structure's comparator level as it may be incorrect due to not having a capacity while unformed
            multiblock.forceUpdateComparatorLevel();
            //If we are the block that is rendering the structure make sure to tell all the valves to update their comparator levels
            multiblock.notifyAllUpdateComparator(level);
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockPos pos = getBlockPos();
        for (Direction side : EnumUtils.DIRECTIONS) {
            mutable.setWithOffset(pos, side);
            if (!multiblock.isFormed() || !multiblock.isKnownLocation(mutable)) {
                BlockEntity tile = WorldUtils.getTileEntity(level, mutable);
                if (!level.isEmptyBlock(mutable) && (tile == null || tile.getClass() != getClass()) && !(tile instanceof IStructuralMultiblock || tile instanceof IMultiblock)) {
                    WorldUtils.notifyNeighborOfChange(level, mutable, pos);
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
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        //Comparators are handled via the multiblock, no special listeners are needed
        return false;
    }

    @Override
    public boolean canBeMaster() {
        return true;
    }

    @Override
    public ItemInteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
        if (player.isShiftKeyDown() || !getMultiblock().isFormed()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        InteractionResult result = openGui(player);
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            case FAIL -> ItemInteractionResult.FAIL;
        };
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!isRemote()) {
            structure.invalidate(level);
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
    }

    @Nullable
    @Override
    public UUID getCacheID() {
        return cachedID;
    }

    @Override
    public boolean isMaster() {
        return isMaster;
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putBoolean(SerializationConstants.RENDERING, isMaster());
        T multiblock = getMultiblock();
        updateTag.putBoolean(SerializationConstants.HAS_STRUCTURE, multiblock.isFormed());
        if (multiblock.isFormed() && isMaster()) {
            multiblock.writeUpdateTag(updateTag, provider);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setBooleanIfPresent(tag, SerializationConstants.RENDERING, value -> isMaster = value);
        T multiblock = getMultiblock();
        NBTUtils.setBooleanIfPresent(tag, SerializationConstants.HAS_STRUCTURE, multiblock::setFormedForce);
        if (isMaster()) {
            if (multiblock.isFormed()) {
                multiblock.readUpdateTag(tag, provider);
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
            //Note: Do not change this from LocalPlayer to Player, or it will cause class loading issues on the server
            // due to trying to validate if the value is actually a Player
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && worldPosition.distSqr(player.blockPosition()) <= 1_600) {
                if (MekanismConfig.client.enableMultiblockFormationParticles.get()) {
                    new SparkleAnimation(this, multiblock.renderLocation, multiblock.length() - 1, multiblock.width() - 1, multiblock.height() - 1).run();
                } else {
                    player.displayClientMessage(MekanismLang.MULTIBLOCK_FORMED_CHAT.translateColored(EnumColor.INDIGO), true);
                }
            }
        }
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag nbt, @NotNull HolderLookup.Provider provider) {
        super.loadAdditional(nbt, provider);
        if (!getMultiblock().isFormed()) {
            NBTUtils.setUUIDIfPresent(nbt, SerializationConstants.INVENTORY_ID, id -> cachedID = id);
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag nbtTags, @NotNull HolderLookup.Provider provider) {
        super.saveAdditional(nbtTags, provider);
        if (cachedID != null) {
            //Note: We don't bother validating here the cache still exists as it is irrelevant and unused until attempting to form the multiblock
            // at which point it will gracefully handle multiblock tiles with stale ids and clear them
            nbtTags.putUUID(SerializationConstants.INVENTORY_ID, cachedID);
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        SyncMapper.INSTANCE.setup(container, getMultiblock().getClass(), this::getMultiblock);
    }

    @Override
    public boolean persists(ContainerType<?, ?, ?> type) {
        if (type == ContainerType.ITEM) {
            return false;
        }
        return super.persists(type);
    }

    @NotNull
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
                player.sendSystemMessage(result.getResultText());
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
    public void getComputerMethods(BoundMethodHolder holder) {
        super.getComputerMethods(holder);
        if (exposesMultiblockToComputer()) {
            T multiblock = getMultiblock();
            if (multiblock.isFormed()) {
                //Only expose the multiblock's methods if we are formed, when the formation state changes
                // our capabilities are invalidated, so should end up getting rechecked and this called by
                // the various computer integration mods, and allow us to only expose the multiblock's methods
                // as even existing if the multiblock is complete
                FactoryRegistry.bindTo(holder, multiblock);
            }
        }
    }

    @ComputerMethod(restriction = MethodRestriction.MULTIBLOCK)
    boolean isFormed() {
        return getMultiblock().isFormed();
    }
    //End methods IComputerTile
}