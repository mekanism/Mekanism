package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.item.MCItemDefinition;
import com.blamejared.crafttweaker.impl.tag.MCTag;
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

    /**
     * Creates a builder for registering a custom {@link Slurry}, using our default clean {@link Slurry} texture.
     *
     * @return A builder for creating a custom {@link Slurry}.
     */
    @ZenCodeType.Method
    public static CrTSlurryBuilder clean() {
        return new CrTSlurryBuilder(SlurryBuilder.clean());
    }

    /**
     * Creates a builder for registering a custom {@link Slurry}, using our default dirty {@link Slurry} texture.
     *
     * @return A builder for creating a custom {@link Slurry}.
     */
    @ZenCodeType.Method
    public static CrTSlurryBuilder dirty() {
        return new CrTSlurryBuilder(SlurryBuilder.dirty());
    }

    /**
     * Creates a builder for registering a custom {@link Slurry}.
     *
     * @param textureLocation A {@link MCResourceLocation} representing the texture this {@link Slurry} will use.
     *
     * @return A builder for creating a custom {@link Slurry}.
     */
    @ZenCodeType.Method
    public static CrTSlurryBuilder builder(MCResourceLocation textureLocation) {
        return new CrTSlurryBuilder(SlurryBuilder.builder(textureLocation.getInternal()));
    }

    protected CrTSlurryBuilder(SlurryBuilder builder) {
        super(builder);
    }

    /**
     * Sets the tag that represents the ore that goes with this {@link Slurry}.
     *
     * @param oreTagLocation {@link MCResourceLocation} of the item tag representing the ore.
     */
    @ZenCodeType.Method
    public CrTSlurryBuilder ore(MCResourceLocation oreTagLocation) {
        return ore(oreTagLocation.getInternal());
    }

    /**
     * Sets the tag that represents the ore that goes with this {@link Slurry}.
     *
     * @param oreTag Tag representing the ore.
     *
     * @implNote We add the tag by its internal id instead of getting the internal tag as the internal tag would currently be {@code null} when this gets called, as the
     * collection is empty, and the goal of this method is to let the slurry have an {@link net.minecraft.tags.ITag.INamedTag}
     */
    @ZenCodeType.Method
    public CrTSlurryBuilder ore(MCTag<MCItemDefinition> oreTag) {
        return ore(oreTag.getIdInternal());
    }

    private CrTSlurryBuilder ore(ResourceLocation oreTagLocation) {
        getInternal().ore(oreTagLocation);
        return this;
    }

    @Override
    protected void build(ResourceLocation registryName) {
        CrTContentUtils.queueSlurryForRegistration(registryName, getInternal());
    }
}