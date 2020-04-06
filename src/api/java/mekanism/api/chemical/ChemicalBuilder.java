package mekanism.api.chemical;

import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalBuilder<TYPE extends Chemical<TYPE>, BUILDER extends ChemicalBuilder<TYPE, BUILDER>> {

    private Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap = new Object2ObjectOpenHashMap<>();
    private ResourceLocation texture;
    private int color = 0xFFFFFF;

    protected ChemicalBuilder(ResourceLocation texture) {
        this.texture = texture;
    }

    public BUILDER with(ChemicalAttribute attribute) {
        attributeMap.put(attribute.getClass(), attribute);
        return getThis();
    }

    public Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> getAttributeMap() {
        return attributeMap;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public BUILDER color(int color) {
        this.color = color;
        return getThis();
    }

    public int getColor() {
        return color;
    }

    @SuppressWarnings("unchecked")
    public BUILDER getThis() {
        return (BUILDER) this;
    }
}