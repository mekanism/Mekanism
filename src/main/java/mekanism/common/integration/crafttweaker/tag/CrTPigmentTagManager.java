package mekanism.common.integration.crafttweaker.tag;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTPigment;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTPigment;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_PIGMENT_TAG_MANAGER)
public class CrTPigmentTagManager extends CrTChemicalTagManager<Pigment, ICrTPigment> {

    public static final CrTPigmentTagManager INSTANCE = new CrTPigmentTagManager();

    private CrTPigmentTagManager() {
        super(ChemicalTags.PIGMENT);
    }

    @Nonnull
    @Override
    public Class<ICrTPigment> getElementClass() {
        return ICrTPigment.class;
    }

    @Override
    public String getTagFolder() {
        return "pigments";
    }

    @Override
    protected ICrTPigment fromChemical(Pigment pigment) {
        return new CrTPigment(pigment);
    }
}