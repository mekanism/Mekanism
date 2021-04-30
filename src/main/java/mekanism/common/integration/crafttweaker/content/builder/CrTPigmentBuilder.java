package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.integration.crafttweaker.content.attribute.ICrTChemicalAttribute.ICrTPigmentAttribute;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_PIGMENT)
public class CrTPigmentBuilder extends CrTChemicalBuilder<Pigment, PigmentBuilder, ICrTPigmentAttribute, CrTPigmentBuilder> {

    /**
     * Creates a builder for registering a custom {@link Pigment}.
     *
     * @param textureLocation If present the {@link ResourceLocation} representing the texture this {@link Pigment} will use, otherwise defaults to our default {@link
     *                        Pigment} texture.
     *
     * @return A builder for creating a custom {@link Pigment}.
     *
     * @apiNote If a custom texture is used it is recommended to override to use {@link #colorRepresentation(int)} if this builder method is not being used in combination
     * with {@link #color(int)} due to the texture not needing tinting.
     */
    @ZenCodeType.Method
    public static CrTPigmentBuilder builder(@ZenCodeType.Optional ResourceLocation textureLocation) {
        return new CrTPigmentBuilder(textureLocation == null ? PigmentBuilder.builder() : PigmentBuilder.builder(textureLocation));
    }

    protected CrTPigmentBuilder(PigmentBuilder builder) {
        super(builder);
    }

    @Override
    protected void build(ResourceLocation registryName) {
        Pigment pigment;
        if (colorRepresentation == null) {
            pigment = new Pigment(getInternal());
        } else {
            int color = colorRepresentation;
            pigment = new Pigment(getInternal()) {
                @Override
                public int getColorRepresentation() {
                    return color;
                }
            };
        }
        CrTContentUtils.queuePigmentForRegistration(registryName, pigment);
    }
}