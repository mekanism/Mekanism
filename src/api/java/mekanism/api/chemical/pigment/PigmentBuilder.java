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

    public static PigmentBuilder builder() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "pigment/base"));
    }

    public static PigmentBuilder builder(ResourceLocation texture) {
        return new PigmentBuilder(Objects.requireNonNull(texture));
    }
}