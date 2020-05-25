package mekanism.api.chemical.gas;

import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasBuilder extends ChemicalBuilder<Gas, GasBuilder> {

    protected GasBuilder(ResourceLocation texture) {
        super(texture);
    }

    public static GasBuilder builder() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "liquid/liquid"));
    }

    public static GasBuilder builder(ResourceLocation texture) {
        return new GasBuilder(Objects.requireNonNull(texture));
    }
}