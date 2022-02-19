package mekanism.common.integration.crafttweaker.chemical;

import java.util.function.Function;
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
import mekanism.common.util.ChemicalUtil;

public abstract class CrTMutableChemicalStack<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>> extends BaseCrTChemicalStack<CHEMICAL, STACK, CRT_STACK> {

    public CrTMutableChemicalStack(STACK stack, Function<STACK, CRT_STACK> stackConverter) {
        super(stack, stackConverter);
    }

    @Override
    public CRT_STACK setAmount(long amount) {
        stack.setAmount(amount);
        return asMutable();
    }

    @Override
    public CRT_STACK asMutable() {
        return (CRT_STACK) this;
    }

    @Override
    public CRT_STACK asImmutable() {
        return stackConverter.apply(stack);
    }

    @Override
    public STACK getImmutableInternal() {
        return ChemicalUtil.copy(stack);
    }

    @Override
    protected StringBuilder getBracket() {
        return super.getBracket().append(".mutable()");
    }

    public static class CrTMutableGasStack extends CrTMutableChemicalStack<Gas, GasStack, ICrTGasStack> implements ICrTGasStack {

        public CrTMutableGasStack(GasStack stack) {
            super(stack, CrTMutableGasStack::new);
        }
    }

    public static class CrTMutableInfusionStack extends CrTMutableChemicalStack<InfuseType, InfusionStack, ICrTInfusionStack> implements ICrTInfusionStack {

        public CrTMutableInfusionStack(InfusionStack stack) {
            super(stack, CrTMutableInfusionStack::new);
        }
    }

    public static class CrTMutablePigmentStack extends CrTMutableChemicalStack<Pigment, PigmentStack, ICrTPigmentStack> implements ICrTPigmentStack {

        public CrTMutablePigmentStack(PigmentStack stack) {
            super(stack, CrTMutablePigmentStack::new);
        }
    }

    public static class CrTMutableSlurryStack extends CrTMutableChemicalStack<Slurry, SlurryStack, ICrTSlurryStack> implements ICrTSlurryStack {

        public CrTMutableSlurryStack(SlurryStack stack) {
            super(stack, CrTMutableSlurryStack::new);
        }
    }
}