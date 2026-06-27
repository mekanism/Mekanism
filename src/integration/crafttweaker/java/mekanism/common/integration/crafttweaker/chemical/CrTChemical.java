package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.List;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import net.minecraft.resources.Identifier;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@TaggableElement("mekanism:chemical")
@NativeTypeRegistration(value = Chemical.class, zenCodeName = CrTConstants.CLASS_CHEMICAL)
public class CrTChemical {

    //TODO - 26.2: Rethink how we expose chemical attributes to CrT. We definitely don't want to deal with adding,
    // but being able to query them might be useful? See if CrT has builtin support for data map stuff
    private CrTChemical() {
    }

    /// Gets the registry name of the element represented by this chemical.
    ///
    /// @return Registry name.
    @ZenCodeType.Method
    @ZenCodeType.Getter("registryName")
    public static Identifier getRegistryName(Chemical _this) {
        return CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .lookupOrThrow(MekanismRegistries.Keys.CHEMICAL)
              .getKey(_this);
    }

    /// Get the tint for rendering the chemical
    ///
    /// @return int representation of color in ARGB format
    @ZenCodeType.Method
    @ZenCodeType.Getter("tint")
    public static int getTint(Chemical _this) {
        return _this.tint();
    }

    /// Get the color representation used for displaying in things like durability bars of chemical tanks.
    ///
    /// @return int representation of color in ARGB format
    @ZenCodeType.Method
    @ZenCodeType.Getter("colorRepresentation")
    public static int getColorRepresentation(Chemical _this) {
        return _this.colorRepresentation();
    }

    /// Creates a new [ICrTChemicalStack] with the given amount of chemical.
    ///
    /// @param amount The size of the stack to create.
    ///
    /// @return a new (immutable) [ICrTChemicalStack]
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.MUL)
    public static ICrTChemicalStack makeStack(Chemical _this, int amount) {
        return new CrTChemicalStack(new ChemicalStack(CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .lookupOrThrow(MekanismRegistries.Keys.CHEMICAL)
              .wrapAsHolder(_this), amount));
    }

    /// Gets the tags that this chemical is a part of.
    ///
    /// @return All the tags this chemical is a part of.
    @ZenCodeType.Method
    @ZenCodeType.Getter("tags")
    public static List<KnownTag<Chemical>> getTags(Chemical _this) {
        return CrTUtils.chemicalTags().getTagsFor(_this);
    }
}