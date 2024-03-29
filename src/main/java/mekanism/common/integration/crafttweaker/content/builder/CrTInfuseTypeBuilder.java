package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister(loaders = CrTConstants.CONTENT_LOADER)
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_INFUSE_TYPE)
public class CrTInfuseTypeBuilder extends CrTChemicalBuilder<InfuseType, InfuseTypeBuilder, CrTInfuseTypeBuilder> {

    /**
     * Creates a builder for registering a custom {@link InfuseType}.
     *
     * @param textureLocation If present the {@link ResourceLocation} representing the texture this {@link InfuseType} will use, otherwise defaults to our default
     *                        {@link InfuseType} texture.
     *
     * @return A builder for creating a custom {@link InfuseType}.
     *
     * @apiNote If a custom texture is used it is recommended to override to use {@link #colorRepresentation(int)} if this builder method is not being used in combination
     * with {@link #tint(int)} due to the texture not needing tinting.
     */
    @ZenCodeType.Method
    public static CrTInfuseTypeBuilder builder(@ZenCodeType.Optional ResourceLocation textureLocation) {
        return new CrTInfuseTypeBuilder(textureLocation == null ? InfuseTypeBuilder.builder() : InfuseTypeBuilder.builder(textureLocation));
    }

    protected CrTInfuseTypeBuilder(InfuseTypeBuilder builder) {
        super(builder);
    }

    @Override
    protected void build(ResourceLocation registryName) {
        InfuseType infuseType = ChemicalUtil.infuseType(getInternal(), colorRepresentation);
        CrTContentUtils.queueInfuseTypeForRegistration(registryName, infuseType);
    }
}