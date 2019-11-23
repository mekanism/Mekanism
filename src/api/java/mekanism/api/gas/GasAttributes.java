package mekanism.api.gas;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.ChemicalAttributes;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasAttributes extends ChemicalAttributes<Gas, GasAttributes> {

    private boolean hidden;

    protected GasAttributes(ResourceLocation texture) {
        super(texture);
    }

    public static GasAttributes builder() {
        //TODO: Rename the texture this points at
        return builder(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "block/liquid/liquid"));
    }

    public static GasAttributes builder(ResourceLocation texture) {
        return new GasAttributes(texture);
    }

    /**
     * Sets this gas' visibility state to hidden, this will make the gas not get displayed in JEI
     */
    public GasAttributes hidden() {
        hidden = true;
        return this;
    }

    public boolean isHidden() {
        return hidden;
    }
}