package mekanism.common.integration.crafttweaker.content.attribute;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

//TODO: Do we want to add a way to create custom chemical attributes? Not sure how much of a reason there would be to
@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_CHEMICAL)
public interface ICrTChemicalAttribute {

    /**
     * Mod devs should use this to get the actual ChemicalAttribute
     *
     * @return The actual ChemicalAttribute
     */
    ChemicalAttribute getInternal();

    //TODO: When documenting the below, basically state they are marker interfaces so that we only allow applying
    // attributes to the same types of things they were meant to be applied to (even if strictly speaking there
    // is nothing in place to force on mek's end that they only get added to that type. We may decide to add
    // something eventually to, or not; in theory things should handle them properly, its more just some of them
    // make no sense currently on specific chemicals)
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_GAS)
    interface ICrTGasAttribute extends ICrTChemicalAttribute {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_INFUSE_TYPE)
    interface ICrTInfuseTypeAttribute extends ICrTChemicalAttribute {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_PIGMENT)
    interface ICrTPigmentAttribute extends ICrTChemicalAttribute {
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_SLURRY)
    interface ICrTSlurryAttribute extends ICrTChemicalAttribute {
    }
}