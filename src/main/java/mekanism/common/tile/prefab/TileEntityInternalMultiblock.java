package mekanism.common.tile.prefab;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.common.lib.multiblock.IInternalMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityInternalMultiblock extends TileEntityMekanism implements IInternalMultiblock {

    @Nullable
    private MultiblockData multiblock;
    private UUID multiblockUUID;

    public TileEntityInternalMultiblock(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
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
            //Check if the neighborPos is really a neighbor of this block for bad mods giving non-neighbors
            int dX = Math.abs(neighborPos.getX() - worldPosition.getX());
            int dY = Math.abs(neighborPos.getY() - worldPosition.getY());
            int dZ = Math.abs(neighborPos.getZ() - worldPosition.getZ());
            if ((dX + dY + dZ) > 1) {
                return;
            }
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

    @Override
    public void writeReducedUpdatedTag(@NotNull ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        output.storeNullable(SerializationConstants.INVENTORY_ID, UUIDUtil.CODEC, multiblockUUID);
    }

    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
        super.handleUpdateTag(input);
        //TODO - 1.21.11: Re-evaluate uses of Optional#ifPresentOrElse, and for optionals returned from ValueInput if we can make any of the ifPresent cases not be capturing that currently might be
        input.read(SerializationConstants.INVENTORY_ID, UUIDUtil.CODEC).ifPresentOrElse(this::setMultiblock, () -> multiblockUUID = null);
    }
}