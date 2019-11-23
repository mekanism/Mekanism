package mekanism.api.infuse;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalAttributes;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class InfuseTypeAttributes extends ChemicalAttributes<InfuseType, InfuseTypeAttributes> {

    protected InfuseTypeAttributes(ResourceLocation texture) {
        super(texture);
    }

    public static InfuseTypeAttributes builder() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "infuse_type/base"));
    }

    public static InfuseTypeAttributes builder(ResourceLocation texture) {
        return new InfuseTypeAttributes(texture);
    }
}