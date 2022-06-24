package mekanism.api.chemical.gas;

import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class GasBuilder extends ChemicalBuilder<Gas, GasBuilder> {

    protected GasBuilder(ResourceLocation texture) {
        super(texture);
    }

    /**
     * Creates a builder for registering a {@link Gas}, using our default {@link Gas} texture.
     *
     * @return A builder for creating a {@link Gas}.
     */
    public static GasBuilder builder() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "liquid/liquid"));
    }

    /**
     * Creates a builder for registering a {@link Gas}, with a given texture.
     *
     * @param texture A {@link ResourceLocation} representing the texture this {@link Gas} will use.
     *
     * @return A builder for creating a {@link Gas}.
     *
     * @apiNote The texture will be automatically stitched to the block texture atlas.
     * <br>
     * It is recommended to override {@link Gas#getColorRepresentation()} if this builder method is not used in combination with {@link #color(int)} due to the texture
     * not needing tinting.
     */
    public static GasBuilder builder(ResourceLocation texture) {
        return new GasBuilder(Objects.requireNonNull(texture));
    }
}