package mekanism.common.integration.crafttweaker.tag;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_PIGMENT_TAG_MANAGER)
public class CrTPigmentTagManager extends CrTChemicalTagManager<Pigment> {

    public static final CrTPigmentTagManager INSTANCE = new CrTPigmentTagManager();

    private CrTPigmentTagManager() {
        super(ChemicalTags.PIGMENT);
    }

    @Nonnull
    @Override
    public Class<Pigment> getElementClass() {
        return Pigment.class;
    }

    @Override
    public String getTagFolder() {
        return "pigments";
    }
}