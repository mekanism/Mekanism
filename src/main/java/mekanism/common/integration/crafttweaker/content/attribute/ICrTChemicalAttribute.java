package mekanism.common.integration.crafttweaker.content.attribute;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_CHEMICAL)
public interface ICrTChemicalAttribute {

    /**
     * Mod devs should use this to get the actual ChemicalAttribute
     *
     * @return The actual ChemicalAttribute
     */
    ChemicalAttribute getInternal();

    /**
     * Marker interface for {@link mekanism.api.chemical.gas.Gas} attributes to make it so that people don't accidentally add attributes to different chemical types that
     * may not be expecting them, or have a sense of how to handle them.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_GAS)
    interface ICrTGasAttribute extends ICrTChemicalAttribute {
    }

    /**
     * Marker interface for {@link mekanism.api.chemical.infuse.InfuseType} attributes to make it so that people don't accidentally add attributes to different chemical
     * types that may not be expecting them, or have a sense of how to handle them.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_INFUSE_TYPE)
    interface ICrTInfuseTypeAttribute extends ICrTChemicalAttribute {
    }

    /**
     * Marker interface for {@link mekanism.api.chemical.pigment.Pigment} attributes to make it so that people don't accidentally add attributes to different chemical
     * types that may not be expecting them, or have a sense of how to handle them.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_PIGMENT)
    interface ICrTPigmentAttribute extends ICrTChemicalAttribute {
    }

    /**
     * Marker interface for {@link mekanism.api.chemical.slurry.Slurry} attributes to make it so that people don't accidentally add attributes to different chemical types
     * that may not be expecting them, or have a sense of how to handle them.
     */
    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_ATTRIBUTE_SLURRY)
    interface ICrTSlurryAttribute extends ICrTChemicalAttribute {
    }
}