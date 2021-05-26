package mekanism.common.integration.crafttweaker.tag;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_GAS_TAG_MANAGER)
public class CrTGasTagManager extends CrTChemicalTagManager<Gas> {

    public static final CrTGasTagManager INSTANCE = new CrTGasTagManager();

    private CrTGasTagManager() {
        super(ChemicalTags.GAS);
    }

    @Nonnull
    @Override
    public Class<Gas> getElementClass() {
        return Gas.class;
    }

    @Override
    public String getTagFolder() {
        return "gases";
    }
}