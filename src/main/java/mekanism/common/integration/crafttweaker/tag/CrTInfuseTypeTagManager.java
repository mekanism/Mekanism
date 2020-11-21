package mekanism.common.integration.crafttweaker.tag;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTInfuseType;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTInfuseType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_INFUSE_TYPE_TAG_MANAGER)
public class CrTInfuseTypeTagManager extends CrTChemicalTagManager<InfuseType, ICrTInfuseType> {

    public static final CrTInfuseTypeTagManager INSTANCE = new CrTInfuseTypeTagManager();

    private CrTInfuseTypeTagManager() {
        super(ChemicalTags.INFUSE_TYPE);
    }

    @Nonnull
    @Override
    public Class<ICrTInfuseType> getElementClass() {
        return ICrTInfuseType.class;
    }

    @Override
    public String getTagFolder() {
        return "infuse_types";
    }

    @Override
    protected ICrTInfuseType fromChemical(InfuseType gas) {
        return new CrTInfuseType(gas);
    }
}