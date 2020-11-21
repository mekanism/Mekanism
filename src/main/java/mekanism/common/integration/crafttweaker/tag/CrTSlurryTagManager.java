package mekanism.common.integration.crafttweaker.tag;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical.CrTSlurry;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical.ICrTSlurry;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_SLURRY_TAG_MANAGER)
public class CrTSlurryTagManager extends CrTChemicalTagManager<Slurry, ICrTSlurry> {

    public static final CrTSlurryTagManager INSTANCE = new CrTSlurryTagManager();

    private CrTSlurryTagManager() {
        super(ChemicalTags.SLURRY);
    }

    @Nonnull
    @Override
    public Class<ICrTSlurry> getElementClass() {
        return ICrTSlurry.class;
    }

    @Override
    public String getTagFolder() {
        return "slurries";
    }

    @Override
    protected ICrTSlurry fromChemical(Slurry slurry) {
        return new CrTSlurry(slurry);
    }
}