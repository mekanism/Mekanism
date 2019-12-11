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

    public <TILE extends TileEntity> TileEntityTypeRegistryObject<TILE> register(BlockRegistryObject<?, ?> block, Supplier<? extends TILE> factory) {
        //fixerType = DataFixesManager.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getVersion().getWorldVersion())).getChoiceType(TypeReferences.BLOCK_ENTITY, registryName.getPath());
        //TODO: I don't believe we have a data fixer type for our stuff so it is technically null not the above thing which is taken from TileEntityTypes#register
        // Note: If above is needed, we should add the try catch that TileEntityTypes#register includes
        return register(block.getInternalRegistryName(), () -> TileEntityType.Builder.<TILE>create(factory, block.getBlock()).build(null), TileEntityTypeRegistryObject::new);
    }
}