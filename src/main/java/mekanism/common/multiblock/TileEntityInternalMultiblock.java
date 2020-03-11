package mekanism.common.multiblock;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityInternalMultiblock extends TileEntityMekanism {

    //TODO: Make this actually be a UUID?
    protected String multiblockUUID;

    public TileEntityInternalMultiblock(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    public void setMultiblock(String id) {
        multiblockUUID = id;
    }

    public String getMultiblock() {
        return multiblockUUID;
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        if (multiblockUUID != null) {
            updateTag.putString(NBTConstants.INVENTORY_ID, multiblockUUID);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (tag.contains(NBTConstants.INVENTORY_ID, NBT.TAG_STRING)) {
            multiblockUUID = tag.getString(NBTConstants.INVENTORY_ID);
        } else {
            multiblockUUID = null;
        }
    }
}