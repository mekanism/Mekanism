package mekanism.client.render.obj;

import net.minecraft.client.resources.model.Material;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;

public class OpaqueModelConfiguration extends WrapperModelConfiguration {

    public OpaqueModelConfiguration(IGeometryBakingContext internal) {
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
    public boolean hasMaterial(@NotNull String name) {
        return internal.hasMaterial(adjustTextureName(name));
    }

    @NotNull
    @Override
    public Material getMaterial(@NotNull String name) {
        return internal.getMaterial(adjustTextureName(name));
    }
}