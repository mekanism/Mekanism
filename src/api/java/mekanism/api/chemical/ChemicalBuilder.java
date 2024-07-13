package mekanism.api.chemical;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class ChemicalBuilder<CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>> {

    private final Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap = new Object2ObjectOpenHashMap<>();
    private final ResourceLocation texture;
    private int tint = 0xFFFFFF;

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
        return self();
    }

    /**
     * Gets the attributes this chemical will have.
     */
    public Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> getAttributeMap() {
        return Collections.unmodifiableMap(attributeMap);
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
     * @param tint Color in RRGGBB format
     */
    public BUILDER tint(int tint) {
        this.tint = tint;
        return self();
    }

    /**
     * Gets the tint to apply to this chemical when rendering.
     *
     * @return Tint in RRGGBB format.
     */
    public int getTint() {
        return tint;
    }

    @SuppressWarnings("unchecked")
    private BUILDER self() {
        return (BUILDER) this;
    }
}