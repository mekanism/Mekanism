package mekanism.additions.common.tile;

import java.util.ArrayList;
import java.util.List;
import mekanism.additions.common.AdditionsBlock;
import mekanism.additions.common.MekanismAdditions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class AdditionsTileEntityTypes {

    private static final List<TileEntityType<?>> types = new ArrayList<>();

    public static final TileEntityType<TileEntityGlowPanel> GLOW_PANEL = create("glow_panel", TileEntityType.Builder.create(TileEntityGlowPanel::new,
          AdditionsBlock.BLACK_GLOW_PANEL.getBlock(), AdditionsBlock.RED_GLOW_PANEL.getBlock(), AdditionsBlock.GREEN_GLOW_PANEL.getBlock(),
          AdditionsBlock.BROWN_GLOW_PANEL.getBlock(), AdditionsBlock.BLUE_GLOW_PANEL.getBlock(), AdditionsBlock.PURPLE_GLOW_PANEL.getBlock(),
          AdditionsBlock.CYAN_GLOW_PANEL.getBlock(), AdditionsBlock.LIGHT_GRAY_GLOW_PANEL.getBlock(), AdditionsBlock.GRAY_GLOW_PANEL.getBlock(),
          AdditionsBlock.PINK_GLOW_PANEL.getBlock(), AdditionsBlock.LIME_GLOW_PANEL.getBlock(), AdditionsBlock.YELLOW_GLOW_PANEL.getBlock(),
          AdditionsBlock.LIGHT_BLUE_GLOW_PANEL.getBlock(), AdditionsBlock.MAGENTA_GLOW_PANEL.getBlock(), AdditionsBlock.ORANGE_GLOW_PANEL.getBlock(),
          AdditionsBlock.WHITE_GLOW_PANEL.getBlock()));

    private static <T extends TileEntity> TileEntityType<T> create(String name, TileEntityType.Builder<T> builder) {
        return create(new ResourceLocation(MekanismAdditions.MODID, name), builder);
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