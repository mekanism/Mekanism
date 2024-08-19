package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.Collection;
import java.util.List;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
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
    public static boolean isEmptyType(Chemical _this) {
        return _this.isEmptyType();
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
    @ZenCodeType.Getter("attributes")
    public static Collection<ChemicalAttribute> getAttributes(Chemical _this) {
        return _this.getAttributes();
    }

    /**
     * Adds an attribute to this chemical's attribute map. Will overwrite any existing attribute with the same type.
     *
     * @param attribute attribute to add to this chemical
     */
    @ZenCodeType.Method
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
        return new CrTChemicalStack(_this.getStack(amount));
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