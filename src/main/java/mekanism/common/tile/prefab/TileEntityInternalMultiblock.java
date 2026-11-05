package mekanism.common.tile.prefab;

import java.util.Objects;
import java.util.UUID;
import mekanism.api.SerializationConstants;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidRelative;
import mekanism.common.lib.multiblock.IInternalMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class TileEntityInternalMultiblock extends TileEntityMekanism implements IInternalMultiblock {

    @Nullable
    private MultiblockData multiblock;
    @Nullable
    private UUID multiblockUUID;

    public TileEntityInternalMultiblock(Holder<Block> blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Override
    public void setMultiblock(LevelReader level, @Nullable MultiblockData multiblock) {
        this.multiblock = multiblock;
        setMultiblock(level, multiblock == null ? null : multiblock.inventoryID);
    }

    private void setMultiblock(@Nullable LevelReader level, @Nullable UUID id) {
        UUID old = multiblockUUID;
        multiblockUUID = id;
        if (level != null && !Objects.equals(old, id)) {
            multiblockChanged(level, old);
        }
    }

    protected void multiblockChanged(LevelReader level, @Nullable UUID old) {
        if (!level.isClientSide()) {
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
    public void neighborChanged(LevelReader level, @Nullable Orientation orientation) {
        super.neighborChanged(level, orientation);
        //TODO - V11: Make this properly support changing blocks inside the structure when they aren't touching any part of the multiblocks
        //Note: We handle when an internal multiblock is removed that isn't touching anything in BlockMekanism#onRemove
        if (!level.isClientSide() && multiblock != null) {
            VoxelCuboid bounds = multiblock.getBounds();
            for (Direction direction : EnumUtils.DIRECTIONS) {
                BlockPos neighborPos = worldPosition.relative(direction);
                CuboidRelative relativeLocation = bounds.getRelativeLocation(neighborPos);
                //Note: Don't bother with structure checks, as for special cases like the tops of evap towers, this will just get caught by the  top edge
                if (relativeLocation == CuboidRelative.INSIDE) {
                    //If the neighbor change happened to a block inside a multiblock, and it isn't a block that is part of the multiblock
                    if (level.isEmptyBlock(neighborPos) == multiblock.isKnownLocation(neighborPos)) {
                        //And we are not already an internal part of the structure, or we are changing an internal part to air
                        // then we mark the structure as needing to be re-validated
                        //Note: This isn't a super accurate check as if a node gets replaced by command or mod with say dirt
                        // it won't know to invalidate it but oh well. (See java docs on internalLocations for more caveats)
                        multiblock.recheckStructure = true;
                    }
                }
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        //If an internal multiblock is being removed then mark the multiblock it was in as needing to recheck the structure
        if (level != null && !level.isClientSide() && hasFormedMultiblock() && multiblock != null) {
            //Multiblock shouldn't be null but validate it just in case
            multiblock.recheckStructure = true;
        }
    }

    @Override
    public void writeReducedUpdatedTag(ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        output.storeNullable(SerializationConstants.INVENTORY_ID, UUIDUtil.CODEC, multiblockUUID);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
        setMultiblock(level, input.read(SerializationConstants.INVENTORY_ID, UUIDUtil.CODEC).orElse(null));
    }
}