package mekanism.api.chemical.infuse;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InfuseTypeBuilder extends ChemicalBuilder<InfuseType, InfuseTypeBuilder> {

    protected InfuseTypeBuilder(ResourceLocation texture) {
        super(texture);
    }

    /**
     * Creates a builder for registering an {@link InfuseType}, using our default {@link InfuseType} texture.
     *
     * @return A builder for creating an {@link InfuseType}.
     */
    public static InfuseTypeBuilder builder() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "infuse_type/base"));
    }

    /**
     * Creates a builder for registering an {@link InfuseType}, with a given texture.
     *
     * @param texture A {@link ResourceLocation} representing the texture this {@link InfuseType} will use.
     *
     * @return A builder for creating an {@link InfuseType}.
     */
    public static InfuseTypeBuilder builder(ResourceLocation texture) {
        return new InfuseTypeBuilder(Objects.requireNonNull(texture));
    }
}