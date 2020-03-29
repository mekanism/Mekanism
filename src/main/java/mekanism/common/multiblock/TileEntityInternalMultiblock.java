package mekanism.common.multiblock;

import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityInternalMultiblock extends TileEntityMekanism {

    protected UUID multiblockUUID;

    public TileEntityInternalMultiblock(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    public void setMultiblock(UUID id) {
        multiblockUUID = id;
    }

    public UUID getMultiblock() {
        return multiblockUUID;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (multiblockUUID != null) {
            updateTag.putUniqueId(NBTConstants.INVENTORY_ID, multiblockUUID);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (tag.hasUniqueId(NBTConstants.INVENTORY_ID)) {
            setMultiblock(tag.getUniqueId(NBTConstants.INVENTORY_ID));
        } else {
            multiblockUUID = null;
        }
    }
}