package mekanism.api.providers;

import javax.annotation.Nonnull;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

//Note: Doesn't extend the other providers as we only use it as a slight helper
public interface ITileEntityTypeProvider<TILE extends TileEntity> {

    @Nonnull
    TileEntityType<TILE> getTileEntityType();
}