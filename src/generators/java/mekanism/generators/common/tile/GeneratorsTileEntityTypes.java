package mekanism.generators.common.tile;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import mekanism.common.base.IBlockProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class GeneratorsTileEntityTypes {

    private static final List<TileEntityType<?>> types = new ArrayList<>();



    private static <T extends TileEntity> TileEntityType<T> create(IBlockProvider provider, TileEntityType.Builder<T> builder) {
        return create(provider.getRegistryName(), builder);
    }

    private static <T extends TileEntity> TileEntityType<T> create(String name, TileEntityType.Builder<T> builder) {
        return create(new ResourceLocation(Mekanism.MODID, name), builder);
    }

    private static <T extends TileEntity> TileEntityType<T> create(ResourceLocation registryName, TileEntityType.Builder<T> builder) {
        //fixerType = DataFixesManager.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getVersion().getWorldVersion())).getChoiceType(TypeReferences.BLOCK_ENTITY, registryName.getPath());
        //TODO: I don't believe we have a data fixer type for our stuff so it is technically null not the above thing which is taken from TileEntityTypes#register
        // Note: If above is needed, we should add the try catch that TileEntityTypes#register includes
        TileEntityType<T> type = builder.build(null);
        type.setRegistryName(registryName);
        types.add(type);
        return type;
    }

    public static void registerTileEntities(IForgeRegistry<TileEntityType<?>> registry) {
        types.forEach(registry::register);
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }
}