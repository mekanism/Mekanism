package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.attribute.ICrTChemicalAttribute;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_CHEMICAL)
public abstract class CrTChemicalBuilder<CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>,
      ATTRIBUTE extends ICrTChemicalAttribute, CRT_BUILDER extends CrTChemicalBuilder<CHEMICAL, BUILDER, ATTRIBUTE, CRT_BUILDER>> {

    private final BUILDER builder;

    protected CrTChemicalBuilder(BUILDER builder) {
        this.builder = builder;
    }

    @ZenCodeType.Method
    public CRT_BUILDER with(ATTRIBUTE attribute) {
        getInternal().with(attribute.getInternal());
        return getThis();
    }

    @ZenCodeType.Method
    public CRT_BUILDER color(int color) {
        //TODO: Add a bit more verification to the color??
        getInternal().color(color);
        return getThis();
    }

    @ZenCodeType.Method
    public CRT_BUILDER hidden() {
        getInternal().hidden();
        return getThis();
    }

    @ZenCodeType.Method
    public void build(String name) {
        //TODO: Validate the name
        //TODO: If it doesn't throw warnings about invalid mod (given we are registering this as CrT)
        // then move our CrT RL creators to CrTUtils instead of being inlined, if we do have issues
        // then potentially either try to register the listeners as if we were CrT or just register
        // the chemicals to our domain
        build(new ResourceLocation(MekanismHooks.CRAFTTWEAKER_MOD_ID, name));
    }

    protected abstract void build(ResourceLocation registryName);

    protected BUILDER getInternal() {
        return builder;
    }

    @SuppressWarnings("unchecked")
    protected CRT_BUILDER getThis() {
        return (CRT_BUILDER) this;
    }
}