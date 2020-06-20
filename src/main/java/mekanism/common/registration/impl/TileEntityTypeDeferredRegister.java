package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityTypeDeferredRegister extends WrappedDeferredRegister<TileEntityType<?>> {

    public TileEntityTypeDeferredRegister(String modid) {
        super(modid, ForgeRegistries.TILE_ENTITIES);
    }

    @SuppressWarnings("ConstantConditions")
    public <TILE extends TileEntity> TileEntityTypeRegistryObject<TILE> register(BlockRegistryObject<?, ?> block, Supplier<? extends TILE> factory) {
        //Note: There is no data fixer type as forge does not currently have a way exposing data fixers to mods yet
        return register(block.getInternalRegistryName(), () -> TileEntityType.Builder.<TILE>create(factory, block.getBlock()).build(null),
              TileEntityTypeRegistryObject::new);
    }
}