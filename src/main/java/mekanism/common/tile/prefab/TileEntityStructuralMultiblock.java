package mekanism.common.tile.prefab;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Map;
import mekanism.api.IConfigurable;
import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityStructuralMultiblock extends TileEntityMekanism implements IStructuralMultiblock, IConfigurable {

    //Note: We never expect this to really get past two (and at an absolute max of nine if the player intentionally does weird things),
    // we just use an array map as it has better performance, and on chunk unload or server restart it will get back to being a capacity
    // of two
    private final Map<MultiblockManager<?>, Structure> structures = new Reference2ObjectArrayMap<>(2);
    private final Structure invalidStructure = Structure.INVALID;
    private final MultiblockData defaultMultiblock = new MultiblockData(this);
    /**
     * Used to mark that the multiblock is currently being removed/unloaded, so we can skip updating the backing maps.
     */
    private boolean removing;
    private boolean hasFormedMultiblock = false;
    private boolean canAccessGui = false;

    public TileEntityStructuralMultiblock(Holder<Block> provider, BlockPos pos, BlockState state) {
        super(provider, pos, state);
    }

    @Override
    public MultiblockData getDefaultData() {
        return defaultMultiblock;
    }

    @Override
    public void setStructure(MultiblockManager<?> manager, Structure structure) {
        structures.put(manager, structure);
    }

    @Override
    public void multiblockUnformed(Structure structure) {
        if (!removing) {
            //Don't try to remove it from the tile when the tile is the thing being removed
            if (hasFormedMultiblock || canAccessGui) {
                MultiblockManager<?> manager = structure.getManager();
                if (structures.get(manager) == structure) {
                    boolean hasFormed = false;
                    boolean canAccess = false;
                    for (Structure struct : structures.values()) {
                        if (struct != structure) {
                            MultiblockData multiblock = getMultiblockData(struct);
                            if (multiblock != null && multiblock.isFormed()) {
                                hasFormed = true;
                                canAccess |= multiblock.allowsStructuralGuiAccess(this);
                            }
                        }
                    }
                    updateFormedMultiblock(hasFormed, canAccess);
                }
            }
        }
    }

    @Override
    public void multiblockFormed(MultiblockData multiblock) {
        //Note: We pass the existing value of canAccessGui, as then we will validate when interacting, and only allow interacting with a specific sub one
        updateFormedMultiblock(true, canAccessGui || multiblock.allowsStructuralGuiAccess(this));
    }

    private void updateFormedMultiblock(boolean hasFormed, boolean canAccess) {
        if (hasFormedMultiblock != hasFormed || canAccessGui != canAccess) {
            hasFormedMultiblock = hasFormed;
            canAccessGui = canAccess;
            sendUpdatePacket();
        }
    }

    @Override
    public Structure getStructure(MultiblockManager<?> manager) {
        return structures.getOrDefault(manager, invalidStructure);
    }

    @Override
    public boolean hasStructure(Structure structure) {
        return structures.get(structure.getManager()) == structure;
    }

    @Override
    public boolean hasFormedMultiblock() {
        return hasFormedMultiblock;
    }

    @Override
    public boolean structuralGuiAccessAllowed() {
        return hasFormedMultiblock && canAccessGui;
    }

    @Override
    public Map<MultiblockManager<?>, Structure> getStructureMap() {
        return structures;
    }

    @Nullable
    private MultiblockData getMultiblockData(Structure structure) {
        //Like the getMultiblockData(MultiblockManager) method except can assume the structure is indeed in our structures map,
        // so we can slightly short circuit lookup, and return null if there is no data
        MultiblockData data = structure.getMultiblockData();
        if (data != null && data.isFormed()) {
            return data;
        }
        return null;
    }

    @Override
    public void onAdded() {
        super.onAdded();
        //Ensure placing a structural multiblock tries to form the connected multiblock
        invalidStructure.tick(this, true);
    }

    @Override
    public ItemInteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
        if (!structuralGuiAccessAllowed()) {
            //If we don't have any structures that allow gui access, just short circuit and pass
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        for (Structure structure : structures.values()) {
            //If we already have an interaction that has been handled with one of our multiblocks just pass
            // in 99% of cases we will not have a second iteration of the loop
            IMultiblock<?> master = structure.getController();
            if (master != null) {
                MultiblockData data = getMultiblockData(structure);
                if (data != null && data.isFormed() && data.allowsStructuralGuiAccess(this)) {
                    // make sure this block is on the structure first
                    if (data.getBounds().getRelativeLocation(getBlockPos()).isWall()) {
                        ItemInteractionResult result = master.onActivate(player, hand, stack);
                        if (result.result() != InteractionResult.PASS) {
                            return result;
                        }
                    }
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        if (!isRemote()) {
            //TODO - V11: Make this properly support changing blocks inside the structure when they aren't touching any part of the multiblocks
            for (Structure s : structures.values()) {
                //For each structure this structural multiblock is a part of
                if (s.getController() != null) {
                    MultiblockData multiblock = getMultiblockData(s);
                    if (multiblock != null && multiblock.isPositionInsideBounds(s, neighborPos)) {
                        if (level.isEmptyBlock(neighborPos) || !multiblock.internalLocations.contains(neighborPos)) {
                            //And we are not already an internal part of the structure, or we are changing an internal part to air
                            // then we mark the structure as needing to be re-validated
                            //Note: This isn't a super accurate check as if a node gets replaced by command or mod with say dirt
                            // it won't know to invalidate it but oh well. (See java docs on internalLocations for more caveats)
                            s.markForUpdate(level, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult onRightClick(Player player) {
        if (isRemote()) {
            return InteractionResult.PASS;
        }
        for (Structure structure : structures.values()) {
            if (structure.getController() != null) {
                MultiblockData multiblock = getMultiblockData(structure);
                if (multiblock == null || !multiblock.isFormed()) {
                    FormationResult result = structure.runUpdate(this);
                    if (!result.isFormed() && result.getResultText() != null) {
                        player.sendSystemMessage(result.getResultText());
                        return InteractionResult.sidedSuccess(isRemote());
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onSneakRightClick(Player player) {
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.putBoolean(SerializationConstants.FORMED, hasFormedMultiblock);
        updateTag.putBoolean(SerializationConstants.GUI, canAccessGui);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        hasFormedMultiblock = tag.getBoolean(SerializationConstants.FORMED);
        canAccessGui = tag.getBoolean(SerializationConstants.GUI);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!isRemote()) {
            removing = true;
            for (Structure s : structures.values()) {
                s.invalidate(level);
            }
        }
    }

    @Override
    public boolean shouldDumpRadiation() {
        //We handle dumping radiation separately for multiblocks
        return false;
    }
}