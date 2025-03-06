package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.Collection;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@TaggableElement("mekanism:chemical")
@NativeTypeRegistration(value = Chemical.class, zenCodeName = CrTConstants.CLASS_CHEMICAL)
public class CrTChemical {

    private CrTChemical() {
    }

    /**
     * Gets whether this chemical is the empty instance.
     *
     * @return {@code true} if this chemical is the empty instance, {@code false} otherwise.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("empty")
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static boolean isEmptyType(Chemical _this) {
        return _this.isEmptyType();
    }

    /**
     * Gets the registry name of the element represented by this chemical.
     *
     * @return Registry name.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("registryName")
    public static ResourceLocation getRegistryName(Chemical _this) {
        return MekanismAPI.CHEMICAL_REGISTRY.getKey(_this);
    }

    /**
     * Get the tint for rendering the chemical
     *
     * @return int representation of color in RRGGBB format
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("tint")
    public static int getTint(Chemical _this) {
        return _this.getTint();
    }

    /**
     * Get the color representation used for displaying in things like durability bars of chemical tanks.
     *
     * @return int representation of color in RRGGBB format
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("colorRepresentation")
    public static int getColorRepresentation(Chemical _this) {
        return _this.getColorRepresentation();
    }

    /**
     * Gets all attribute instances associated with this chemical type.
     *
     * @return collection of attribute instances.
     */
    @ZenCodeType.Method
    @SuppressWarnings("removal")
    @ZenCodeType.Getter("attributes")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static Collection<ChemicalAttribute> getAttributes(Chemical _this) {
        return _this.getAttributes();
    }

    /**
     * Adds an attribute to this chemical's attribute map. Will overwrite any existing attribute with the same type.
     *
     * @param attribute attribute to add to this chemical
     */
    @ZenCodeType.Method
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public static void addAttribute(Chemical _this, ChemicalAttribute attribute) {
        _this.addAttribute(attribute);
    }

    /**
     * Creates a new {@link ICrTChemicalStack} with the given amount of chemical.
     *
     * @param amount The size of the stack to create.
     *
     * @return a new (immutable) {@link ICrTChemicalStack}
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public static ICrTChemicalStack makeStack(Chemical _this, long amount) {
        return new CrTChemicalStack(new ChemicalStack(MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(_this), amount));
    }

    /**
     * Gets the tags that this chemical is a part of.
     *
     * @return All the tags this chemical is a part of.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("tags")
    public static List<KnownTag<Chemical>> getTags(Chemical _this) {
        return CrTUtils.chemicalTags().getTagsFor(_this);
    }
}