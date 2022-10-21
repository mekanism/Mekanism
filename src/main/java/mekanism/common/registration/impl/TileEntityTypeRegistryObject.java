package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class TileEntityTypeRegistryObject<BE extends BlockEntity> extends WrappedRegistryObject<BlockEntityType<BE>> {

    @Nullable
    private BlockEntityTicker<BE> clientTicker;
    @Nullable
    private BlockEntityTicker<BE> serverTicker;

    public TileEntityTypeRegistryObject(RegistryObject<BlockEntityType<BE>> registryObject) {
        super(registryObject);
    }

    //Internal use only, overwrite the registry object
    TileEntityTypeRegistryObject<BE> setRegistryObject(RegistryObject<BlockEntityType<BE>> registryObject) {
        this.registryObject = registryObject;
        return this;
    }

    //Internal use only
    TileEntityTypeRegistryObject<BE> clientTicker(BlockEntityTicker<BE> ticker) {
        clientTicker = ticker;
        return this;
    }

    //Internal use only
    TileEntityTypeRegistryObject<BE> serverTicker(BlockEntityTicker<BE> ticker) {
        serverTicker = ticker;
        return this;
    }

    @Nullable
    public BlockEntityTicker<BE> getTicker(boolean isClient) {
        return isClient ? clientTicker : serverTicker;
    }
}