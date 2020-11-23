package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.content.attribute.ICrTChemicalAttribute;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_CHEMICAL)
public abstract class CrTChemicalBuilder<CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>,
      ATTRIBUTE extends ICrTChemicalAttribute, CRT_BUILDER extends CrTChemicalBuilder<CHEMICAL, BUILDER, ATTRIBUTE, CRT_BUILDER>> {

    private final BUILDER builder;

    protected CrTChemicalBuilder(BUILDER builder) {
        this.builder = builder;
    }

    /**
     * Adds an attribute to the set of attributes this chemical has.
     *
     * @param attribute Attribute to add.
     */
    @ZenCodeType.Method
    public CRT_BUILDER with(ATTRIBUTE attribute) {
        getInternal().with(attribute.getInternal());
        return getThis();
    }

    /**
     * Sets the tint to apply to this chemical when rendering.
     *
     * @param color Color in RRGGBB format
     */
    @ZenCodeType.Method
    public CRT_BUILDER color(int color) {
        getInternal().color(color);
        return getThis();
    }

    /**
     * Marks that this chemical will be hidden in JEI, and not included in the preset of filled chemical tanks.
     */
    @ZenCodeType.Method
    public CRT_BUILDER hidden() {
        getInternal().hidden();
        return getThis();
    }

    /**
     * Create a chemical from this builder with the given name.
     *
     * @param name Registry name for the chemical.
     */
    @ZenCodeType.Method
    public void build(String name) {
        build(CrTUtils.rl(name));
    }

    /**
     * Create a chemical from this builder with the given name.
     *
     * @param registryName Registry name for the chemical.
     */
    protected abstract void build(ResourceLocation registryName);

    /**
     * Gets the internal {@link ChemicalBuilder}
     */
    protected BUILDER getInternal() {
        return builder;
    }

    @SuppressWarnings("unchecked")
    protected CRT_BUILDER getThis() {
        return (CRT_BUILDER) this;
    }
}