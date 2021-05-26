package mekanism.common.integration.crafttweaker.tag;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.crafttweaker.CrTConstants;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_SLURRY_TAG_MANAGER)
public class CrTSlurryTagManager extends CrTChemicalTagManager<Slurry> {

    public static final CrTSlurryTagManager INSTANCE = new CrTSlurryTagManager();

    private CrTSlurryTagManager() {
        super(ChemicalTags.SLURRY);
    }

    @Nonnull
    @Override
    public Class<Slurry> getElementClass() {
        return Slurry.class;
    }

    @Override
    public String getTagFolder() {
        return "slurries";
    }
}