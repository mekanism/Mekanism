package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.util.MCResourceLocation;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.integration.crafttweaker.content.attribute.ICrTChemicalAttribute.ICrTInfuseTypeAttribute;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_INFUSE_TYPE)
public class CrTInfuseTypeBuilder extends CrTChemicalBuilder<InfuseType, InfuseTypeBuilder, ICrTInfuseTypeAttribute, CrTInfuseTypeBuilder> {

    @ZenCodeType.Method
    public static CrTInfuseTypeBuilder builder(@ZenCodeType.Optional MCResourceLocation textureLocation) {
        return new CrTInfuseTypeBuilder(textureLocation == null ? InfuseTypeBuilder.builder() : InfuseTypeBuilder.builder(textureLocation.getInternal()));
    }

    protected CrTInfuseTypeBuilder(InfuseTypeBuilder builder) {
        super(builder);
    }

    @Override
    protected void build(ResourceLocation registryName) {
        CrTContentUtils.queueInfuseTypeForRegistration(registryName, getInternal());
    }
}