package mekanism.client.model;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

public abstract class BaseBlockModelProvider extends BlockModelProvider {

    public BaseBlockModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @NotNull
    @Override
    public String getName() {
        return "Block model provider: " + modid;
    }

    public BlockModelBuilder sideBottomTop(String name, ResourceLocation parent, ResourceLocation texture) {
        return withExistingParent(name, parent)
              .texture("side", texture)
              .texture("bottom", texture)
              .texture("top", texture);
    }

    public boolean textureExists(ResourceLocation texture) {
        return existingFileHelper.exists(texture, PackType.CLIENT_RESOURCES, ".png", "textures");
    }
}