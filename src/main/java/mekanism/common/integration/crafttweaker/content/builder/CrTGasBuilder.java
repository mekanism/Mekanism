package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.util.MCResourceLocation;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.integration.crafttweaker.content.attribute.ICrTChemicalAttribute.ICrTGasAttribute;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_GAS)
public class CrTGasBuilder extends CrTChemicalBuilder<Gas, GasBuilder, ICrTGasAttribute, CrTGasBuilder> {

    /**
     * Creates a builder for registering a custom {@link Gas}.
     *
     * @param textureLocation If present the {@link MCResourceLocation} representing the texture this {@link Gas} will use, otherwise defaults to our default {@link Gas}
     *                        texture.
     *
     * @return A builder for creating a custom {@link Gas}.
     */
    @ZenCodeType.Method
    public static CrTGasBuilder builder(@ZenCodeType.Optional MCResourceLocation textureLocation) {
        return new CrTGasBuilder(textureLocation == null ? GasBuilder.builder() : GasBuilder.builder(textureLocation.getInternal()));
    }

    protected CrTGasBuilder(GasBuilder builder) {
        super(builder);
    }

    @Override
    protected void build(ResourceLocation registryName) {
        CrTContentUtils.queueGasForRegistration(registryName, getInternal());
    }
}