package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.common.block.BlockCardboardBox.BlockData;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

public class TileEntityCardboardBox extends TileEntity {

    public BlockData storedData;

    public TileEntityCardboardBox() {
        super(MekanismTileEntityTypes.CARDBOARD_BOX.getTileEntityType());
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        NBTUtils.setCompoundIfPresent(nbtTags, NBTConstants.DATA, nbt -> storedData = BlockData.read(nbt));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (storedData != null) {
            nbtTags.put(NBTConstants.DATA, storedData.write(new CompoundNBT()));
        }
        return nbtTags;
    }
}