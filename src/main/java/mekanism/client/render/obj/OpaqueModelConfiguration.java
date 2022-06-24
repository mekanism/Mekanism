package mekanism.client.render.obj;

import net.minecraft.client.resources.model.Material;
import net.minecraftforge.client.model.IModelConfiguration;
import org.jetbrains.annotations.NotNull;

public class OpaqueModelConfiguration extends WrapperModelConfiguration {

    public OpaqueModelConfiguration(IModelConfiguration internal) {
        super(internal);
    }

    private String adjustTextureName(String name) {
        //Always opaque to ensure that we load the textures regardless
        if (name.startsWith("#side")) {
            return name + "_opaque";
        } else if (name.startsWith("#center")) {
            return name.contains("glass") ? "#center_glass_opaque" : "#center_opaque";
        }
        return name;
    }

    @Override
    public boolean isTexturePresent(@NotNull String name) {
        return internal.isTexturePresent(adjustTextureName(name));
    }

    @NotNull
    @Override
    public Material resolveTexture(@NotNull String name) {
        return internal.resolveTexture(adjustTextureName(name));
    }
}