package mekanism.common.tile.prefab;

import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;

//TODO - V11: Evaluate making neighbor updates to this (and other "non internal" internal multiblocks like induction cells) cause the multiblock to unform
public class TileEntityInternalMultiblock extends TileEntityMekanism {

    protected UUID multiblockUUID;

    public TileEntityInternalMultiblock(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    public void setMultiblock(UUID id) {
        multiblockUUID = id;
    }

    public UUID getMultiblock() {
        return multiblockUUID;
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