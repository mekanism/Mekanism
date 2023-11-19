package mekanism.common.registration.impl;

import mekanism.common.registration.MekanismDeferredHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

public class TileEntityTypeRegistryObject<BE extends BlockEntity> extends MekanismDeferredHolder<BlockEntityType<?>, BlockEntityType<BE>> {

    @Nullable
    private BlockEntityTicker<BE> clientTicker;
    @Nullable
    private BlockEntityTicker<BE> serverTicker;

    public TileEntityTypeRegistryObject(ResourceLocation key) {
        this(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, key));
    }

    public TileEntityTypeRegistryObject(ResourceKey<BlockEntityType<?>> key) {
        super(key);
    }

    @Internal
    TileEntityTypeRegistryObject<BE> clientTicker(BlockEntityTicker<BE> ticker) {
        clientTicker = ticker;
        return this;
    }

    @Internal
    TileEntityTypeRegistryObject<BE> serverTicker(BlockEntityTicker<BE> ticker) {
        serverTicker = ticker;
        return this;
    }

    @Nullable
    public BlockEntityTicker<BE> getTicker(boolean isClient) {
        return isClient ? clientTicker : serverTicker;
    }
}