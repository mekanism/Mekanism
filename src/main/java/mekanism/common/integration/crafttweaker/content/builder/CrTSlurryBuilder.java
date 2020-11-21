package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.util.MCResourceLocation;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.integration.crafttweaker.content.attribute.ICrTChemicalAttribute.ICrTSlurryAttribute;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_SLURRY)
public class CrTSlurryBuilder extends CrTChemicalBuilder<Slurry, SlurryBuilder, ICrTSlurryAttribute, CrTSlurryBuilder> {

    @ZenCodeType.Method
    public static CrTSlurryBuilder clean() {
        return new CrTSlurryBuilder(SlurryBuilder.clean());
    }

    @ZenCodeType.Method
    public static CrTSlurryBuilder dirty() {
        return new CrTSlurryBuilder(SlurryBuilder.dirty());
    }

    @ZenCodeType.Method
    public static CrTSlurryBuilder builder(MCResourceLocation textureLocation) {
        return new CrTSlurryBuilder(SlurryBuilder.builder(textureLocation.getInternal()));
    }

    protected CrTSlurryBuilder(SlurryBuilder builder) {
        super(builder);
    }

    @ZenCodeType.Method
    public CrTSlurryBuilder ore(MCResourceLocation oreTagLocation) {
        //Note: We only expose the resource location based ore setter as tags don't exist yet
        getInternal().ore(oreTagLocation.getInternal());
        return this;
    }

    @Override
    protected void build(ResourceLocation registryName) {
        CrTContentUtils.queueSlurryForRegistration(registryName, getInternal());
    }
}