package mekanism.api.chemical.pigment;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PigmentBuilder extends ChemicalBuilder<Pigment, PigmentBuilder> {

    protected PigmentBuilder(ResourceLocation texture) {
        super(texture);
    }

    /**
     * Creates a builder for registering a {@link Pigment}, using our default {@link Pigment} texture.
     *
     * @return A builder for creating a {@link Pigment}.
     */
    public static PigmentBuilder builder() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "pigment/base"));
    }

    /**
     * Creates a builder for registering a {@link Pigment}, with a given texture.
     *
     * @param texture A {@link ResourceLocation} representing the texture this {@link Pigment} will use.
     *
     * @return A builder for creating a {@link Pigment}.
     */
    public static PigmentBuilder builder(ResourceLocation texture) {
        return new PigmentBuilder(Objects.requireNonNull(texture));
    }
}