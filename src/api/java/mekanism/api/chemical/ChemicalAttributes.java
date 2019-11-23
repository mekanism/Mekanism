package mekanism.api.chemical;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalAttributes<TYPE extends Chemical<TYPE>, ATTRIBUTES extends ChemicalAttributes<TYPE, ATTRIBUTES>> {

    private ResourceLocation texture;
    private int color = 0xFFFFFF;

    protected ChemicalAttributes(ResourceLocation texture) {
        this.texture = texture;
    }

    public ATTRIBUTES color(int color) {
        this.color = color;
        return (ATTRIBUTES) this;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public int getColor() {
        return color;
    }
}