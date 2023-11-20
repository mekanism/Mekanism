package mekanism.common.registration.impl;

import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import org.jetbrains.annotations.Nullable;

public class TileEntityTypeDeferredRegister extends MekanismDeferredRegister<BlockEntityType<?>> {

    public TileEntityTypeDeferredRegister(String modid) {
        //Note: We intentionally don't pass a more restrictive type for holder creation as we ignore the holder that gets created
        // in favor of one we create ourselves
        super(Registries.BLOCK_ENTITY_TYPE, modid);
    }

    public <BE extends TileEntityMekanism> TileEntityTypeRegistryObject<BE> register(BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory) {
        return this.<BE>builder(block, factory).clientTicker(TileEntityMekanism::tickClient).serverTicker(TileEntityMekanism::tickServer).build();
    }

    public <BE extends BlockEntity> BlockEntityTypeBuilder<BE> builder(BlockRegistryObject<?, ?> block, BlockEntitySupplier<? extends BE> factory) {
        return new BlockEntityTypeBuilder<>(block, factory);
    }

    public class BlockEntityTypeBuilder<BE extends BlockEntity> {

        private final BlockRegistryObject<?, ?> block;
        private final BlockEntityType.BlockEntitySupplier<? extends BE> factory;
        @Nullable
        private BlockEntityTicker<BE> clientTicker;
        @Nullable
        private BlockEntityTicker<BE> serverTicker;

        private BlockEntityTypeBuilder(BlockRegistryObject<?, ?> block, BlockEntityType.BlockEntitySupplier<? extends BE> factory) {
            this.block = block;
            this.factory = factory;
        }

        public BlockEntityTypeBuilder<BE> clientTicker(BlockEntityTicker<BE> ticker) {
            if (clientTicker != null) {
                throw new IllegalStateException("Client ticker may only be set once.");
            }
            this.clientTicker = ticker;
            return this;
        }

        public BlockEntityTypeBuilder<BE> serverTicker(BlockEntityTicker<BE> ticker) {
            if (serverTicker != null) {
                throw new IllegalStateException("Server ticker may only be set once.");
            }
            this.serverTicker = ticker;
            return this;
        }

        public BlockEntityTypeBuilder<BE> commonTicker(BlockEntityTicker<BE> ticker) {
            return clientTicker(ticker).serverTicker(ticker);
        }

        @SuppressWarnings("ConstantConditions")
        public TileEntityTypeRegistryObject<BE> build() {
            String name = block.getName();
            TileEntityTypeRegistryObject<BE> registryObject = new TileEntityTypeRegistryObject<>(new ResourceLocation(getNamespace(), name));
            registryObject.clientTicker(clientTicker).serverTicker(serverTicker);
            //Register the BE, but don't care about the returned holder as we already made the holder ourselves so that we could add extra data to it
            register(name, () -> BlockEntityType.Builder.<BE>of(factory, block.getBlock()).build(null));
            return registryObject;
        }
    }
}