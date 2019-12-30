package mekanism.client.render.obj;

import javax.annotation.Nonnull;
import net.minecraft.client.renderer.model.Material;
import net.minecraftforge.client.model.IModelConfiguration;

public class OpaqueModelConfiguration extends WrapperModelConfiguration {

    public OpaqueModelConfiguration(IModelConfiguration internal) {
        super(internal);
    }

    private String adjustTextureName(String name) {
        //Always opaque to ensure that we load the textures regardless
        if (name.equals("#side")) {
            return "#side_opaque";
        } else if (name.startsWith("#center")) {
            return "#center_opaque";
        }
        return name;
    }

    @Override
    public boolean isTexturePresent(@Nonnull String name) {
        return internal.isTexturePresent(adjustTextureName(name));
    }

    @Nonnull
    @Override
    public Material resolveTexture(@Nonnull String name) {
        return internal.resolveTexture(adjustTextureName(name));
    }
}