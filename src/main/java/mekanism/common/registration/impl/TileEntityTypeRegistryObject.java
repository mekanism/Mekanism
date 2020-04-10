package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public class TileEntityTypeRegistryObject<TILE extends TileEntity> extends WrappedRegistryObject<TileEntityType<TILE>> {

    public TileEntityTypeRegistryObject(RegistryObject<TileEntityType<TILE>> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public TileEntityType<TILE> getTileEntityType() {
        return get();
    }
}