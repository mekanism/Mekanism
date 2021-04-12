package mekanism.common.integration.crafttweaker.chemical;

import javax.annotation.Nonnull;
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
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;

public abstract class CrTChemical<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_CHEMICAL extends ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK>>
      implements ICrTChemical<CHEMICAL, STACK, CRT_CHEMICAL, CRT_STACK> {

    protected final CHEMICAL chemical;

    public CrTChemical(CHEMICAL chemical) {
        this.chemical = chemical;
    }

    /**
     * {@inheritDoc}
     *
     * Mod devs should use this to get the actual Chemical
     *
     * @return The actual Chemical
     */
    @Nonnull
    @Override
    public CHEMICAL getChemical() {
        return chemical;
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o != null && getClass() == o.getClass() && chemical == ((CrTChemical<?, ?, ?, ?>) o).chemical;
    }

    @Override
    public int hashCode() {
        return chemical.hashCode();
    }

    public static class CrTGas extends CrTChemical<Gas, GasStack, ICrTGas, ICrTGasStack> implements ICrTGas {

        public CrTGas(Gas gas) {
            super(gas);
        }
    }

    public static class CrTInfuseType extends CrTChemical<InfuseType, InfusionStack, ICrTInfuseType, ICrTInfusionStack> implements ICrTInfuseType {

        public CrTInfuseType(InfuseType infuseType) {
            super(infuseType);
        }
    }

    public static class CrTPigment extends CrTChemical<Pigment, PigmentStack, ICrTPigment, ICrTPigmentStack> implements ICrTPigment {

        public CrTPigment(Pigment pigment) {
            super(pigment);
        }
    }

    public static class CrTSlurry extends CrTChemical<Slurry, SlurryStack, ICrTSlurry, ICrTSlurryStack> implements ICrTSlurry {

        public CrTSlurry(Slurry slurry) {
            super(slurry);
        }
    }
}