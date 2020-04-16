package mekanism.api.chemical.gas;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasBuilder extends ChemicalBuilder<Gas, GasBuilder> {

    private boolean hidden;

    protected GasBuilder(ResourceLocation texture) {
        super(texture);
    }

    public static GasBuilder builder() {
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "liquid/liquid"));
    }

    public static GasBuilder builder(ResourceLocation texture) {
        return new GasBuilder(texture);
    }

    /**
     * Sets this gas' visibility state to hidden, this will make the gas not get displayed in JEI
     */
    public GasBuilder hidden() {
        hidden = true;
        return this;
    }

    public boolean isHidden() {
        return hidden;
    }
}