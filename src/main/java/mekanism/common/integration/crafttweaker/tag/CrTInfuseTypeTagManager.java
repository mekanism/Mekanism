package mekanism.common.integration.crafttweaker.tag;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_INFUSE_TYPE_TAG_MANAGER)
public class CrTInfuseTypeTagManager extends CrTChemicalTagManager<InfuseType> {

    public static final CrTInfuseTypeTagManager INSTANCE = new CrTInfuseTypeTagManager();

    private CrTInfuseTypeTagManager() {
        super(ChemicalTags.INFUSE_TYPE);
    }

    @Nonnull
    @Override
    public Class<InfuseType> getElementClass() {
        return InfuseType.class;
    }

    @Override
    public String getTagFolder() {
        return "infuse_types";
    }
}