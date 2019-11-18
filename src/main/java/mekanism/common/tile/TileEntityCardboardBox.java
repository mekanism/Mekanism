package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.common.block.BlockCardboardBox.BlockData;
import mekanism.common.tile.base.MekanismTileEntityTypes;
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
        if (nbtTags.contains("storedData")) {
            storedData = BlockData.read(nbtTags.getCompound("storedData"));
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (storedData != null) {
            nbtTags.put("storedData", storedData.write(new CompoundNBT()));
        }
        return nbtTags;
    }
}