package mekanism.common.tile.prefab;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.lib.multiblock.IInternalMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityInternalMultiblock extends TileEntityMekanism implements IInternalMultiblock {

    @Nullable
    private MultiblockData multiblock;
    private UUID multiblockUUID;

    public TileEntityInternalMultiblock(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public void setMultiblock(@Nullable MultiblockData multiblock) {
        this.multiblock = multiblock;
        setMultiblock(multiblock == null ? null : multiblock.inventoryID);
    }

    private void setMultiblock(UUID id) {
        UUID old = multiblockUUID;
        multiblockUUID = id;
        if (!Objects.equals(old, id)) {
            multiblockChanged(old);
        }
    }

    protected void multiblockChanged(@Nullable UUID old) {
        if (!isRemote()) {
            sendUpdatePacket();
        }
    }

    @Nullable
    @Override
    public UUID getMultiblockUUID() {
        return multiblockUUID;
    }

    @Nullable
    @Override
    public MultiblockData getMultiblock() {
        return multiblock;
    }

    @Override
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        //TODO - V11: Make this properly support changing blocks inside the structure when they aren't touching any part of the multiblocks
        //Note: We handle when an internal multiblock is removed that isn't touching anything in BlockMekanism#onRemove
        if (!isRemote() && multiblock != null) {
            //If the neighbor change happened to a block inside a multiblock, and it isn't a block that is part of the multiblock
            if (level.isEmptyBlock(neighborPos) || !multiblock.isKnownLocation(neighborPos)) {
                //And we are not already an internal part of the structure, or we are changing an internal part to air
                // then we mark the structure as needing to be re-validated
                //Note: This isn't a super accurate check as if a node gets replaced by command or mod with say dirt
                // it won't know to invalidate it but oh well. (See java docs on internalLocations for more caveats)
                multiblock.recheckStructure = true;
            }
        }
    }

    @Override
    public void blockRemoved() {
        super.blockRemoved();
        //If an internal multiblock is being removed then mark the multiblock it was in as needing to recheck the structure
        if (!isRemote() && hasFormedMultiblock() && multiblock != null) {
            //Multiblock shouldn't be null but validate it just in case
            multiblock.recheckStructure = true;
        }
    }

    @Nonnull
    @Override
    public CompoundTag getReducedUpdateTag() {
        CompoundTag updateTag = super.getReducedUpdateTag();
        if (multiblockUUID != null) {
            updateTag.putUUID(NBTConstants.INVENTORY_ID, multiblockUUID);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundTag tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setUUIDIfPresentElse(tag, NBTConstants.INVENTORY_ID, this::setMultiblock, () -> multiblockUUID = null);
    }
}