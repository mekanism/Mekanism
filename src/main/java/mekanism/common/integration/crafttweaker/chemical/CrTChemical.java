package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTTags;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;
import org.openzen.zencode.java.ZenCodeType.Nullable;

public abstract class CrTChemical<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_CHEMICAL extends ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>>
      implements ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK> {

    protected final CHEMICAL chemical;

    public CrTChemical(CHEMICAL chemical) {
        this.chemical = chemical;
    }

    @Override
    public CHEMICAL getInternal() {
        return chemical;
    }

    @Override
    public boolean isIn(MCTag tag) {
        ITag<CHEMICAL> chemicalTag = getChemicalTag(tag);
        return chemicalTag != null && chemicalTag.contains(chemical);
    }

    @Nullable
    protected abstract ITag<CHEMICAL> getChemicalTag(MCTag tag);

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_GAS_IMPL)
    public static class CrTGas extends CrTChemical<Gas, GasStack, ICrTGas, ICrTGasStack> implements ICrTGas {

        public CrTGas(Gas gas) {
            super(gas);
        }

        @Nullable
        @Override
        protected ITag<Gas> getChemicalTag(MCTag tag) {
            return CrTTags.getGasTag(tag);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_INFUSE_TYPE_IMPL)
    public static class CrTInfuseType extends CrTChemical<InfuseType, InfusionStack, ICrTInfuseType, ICrTInfusionStack> implements ICrTInfuseType {

        public CrTInfuseType(InfuseType infuseType) {
            super(infuseType);
        }

        @Nullable
        @Override
        protected ITag<InfuseType> getChemicalTag(MCTag tag) {
            return CrTTags.getInfuseTypeTag(tag);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_PIGMENT_IMPL)
    public static class CrTPigment extends CrTChemical<Pigment, PigmentStack, ICrTPigment, ICrTPigmentStack> implements ICrTPigment {

        public CrTPigment(Pigment pigment) {
            super(pigment);
        }

        @Nullable
        @Override
        protected ITag<Pigment> getChemicalTag(MCTag tag) {
            return CrTTags.getPigmentTag(tag);
        }
    }

    @ZenRegister
    @ZenCodeType.Name(CrTConstants.CLASS_SLURRY_IMPL)
    public static class CrTSlurry extends CrTChemical<Slurry, SlurryStack, ICrTSlurry, ICrTSlurryStack> implements ICrTSlurry {

        public CrTSlurry(Slurry slurry) {
            super(slurry);
        }

        @Nullable
        @Override
        protected ITag<Slurry> getChemicalTag(MCTag tag) {
            return CrTTags.getSlurryTag(tag);
        }
    }
}