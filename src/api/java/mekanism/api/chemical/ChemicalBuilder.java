package mekanism.api.chemical;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import net.minecraft.util.ResourceLocation;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ChemicalBuilder<CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>> {

    private final Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap = new Object2ObjectOpenHashMap<>();
    private final ResourceLocation texture;
    private int color = 0xFFFFFF;
    private boolean hidden;

    protected ChemicalBuilder(ResourceLocation texture) {
        this.texture = texture;
    }

    /**
     * Adds a {@link ChemicalAttribute} to the set of attributes this chemical has.
     *
     * @param attribute Attribute to add.
     */
    public BUILDER with(ChemicalAttribute attribute) {
        attributeMap.put(attribute.getClass(), attribute);
        return getThis();
    }

    /**
     * Gets the attributes this chemical will have.
     */
    public Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> getAttributeMap() {
        //TODO - 10.1: Look into returning unmodifiable view/immutable copy of this and the same for block attributes
        return attributeMap;
    }

    /**
     * Gets the {@link ResourceLocation} representing the texture this chemical will use.
     */
    public ResourceLocation getTexture() {
        return texture;
    }

    /**
     * Sets the tint to apply to this chemical when rendering.
     *
     * @param color Color in RRGGBB format
     */
    public BUILDER color(int color) {
        this.color = color;
        return getThis();
    }

    /**
     * Marks that this chemical will be hidden in JEI, and not included in the preset of filled chemical tanks.
     */
    public BUILDER hidden() {
        this.hidden = true;
        return getThis();
    }

    /**
     * Gets the tint to apply to this chemical when rendering.
     *
     * @return Tint in RRGGBB format.
     */
    public int getColor() {
        return color;
    }

    /**
     * Checks if this chemical should be hidden from JEI and not included in the preset of filled chemical tanks.
     *
     * @return {@code true} if it should be hidden.
     */
    public boolean isHidden() {
        return hidden;
    }

    @SuppressWarnings("unchecked")
    protected BUILDER getThis() {
        return (BUILDER) this;
    }
}