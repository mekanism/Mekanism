package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister(loaders = CrTConstants.CONTENT_LOADER)
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_GAS)
public class CrTGasBuilder extends CrTChemicalBuilder<Gas, GasBuilder, CrTGasBuilder> {

    /**
     * Creates a builder for registering a custom {@link Gas}.
     *
     * @param textureLocation If present the {@link ResourceLocation} representing the texture this {@link Gas} will use, otherwise defaults to our default {@link Gas}
     *                        texture.
     *
     * @return A builder for creating a custom {@link Gas}.
     *
     * @apiNote If a custom texture is used it is recommended to override to use {@link #colorRepresentation(int)} if this builder method is not being used in combination
     * with {@link #color(int)} due to the texture not needing tinting.
     */
    @ZenCodeType.Method
    public static CrTGasBuilder builder(@ZenCodeType.Optional ResourceLocation textureLocation) {
        return new CrTGasBuilder(textureLocation == null ? GasBuilder.builder() : GasBuilder.builder(textureLocation));
    }

    protected CrTGasBuilder(GasBuilder builder) {
        super(builder);
    }

    @Override
    protected void build(ResourceLocation registryName) {
        Gas gas;
        if (colorRepresentation == null) {
            gas = new Gas(getInternal());
        } else {
            int color = colorRepresentation;
            gas = new Gas(getInternal()) {
                @Override
                public int getColorRepresentation() {
                    return color;
                }
            };
        }
        CrTContentUtils.queueGasForRegistration(registryName, gas);
    }
}