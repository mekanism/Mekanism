package mekanism.common.integration.crafttweaker;

import java.util.Arrays;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;

public class CrTUtils {

    public static ICrTGasStack stackFromGas(Gas gas) {
        return new CrTGasStack(new GasStack(gas, 1));
    }

    public static ICrTInfusionStack stackFromInfuseType(InfuseType infuseType) {
        return new CrTInfusionStack(new InfusionStack(infuseType, 1));
    }

    public static ICrTPigmentStack stackFromPigment(Pigment pigment) {
        return new CrTPigmentStack(new PigmentStack(pigment, 1));
    }

    public static ICrTSlurryStack stackFromSlurry(Slurry slurry) {
        return new CrTSlurryStack(new SlurryStack(slurry, 1));
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, CRT_CHEMICAL extends ICrTChemical<CHEMICAL, ?, CRT_CHEMICAL, ?>> CHEMICAL[] getChemicals(CRT_CHEMICAL[] chemicals) {
        return (CHEMICAL[]) Arrays.stream(chemicals).map(ICrTChemical::getInternal).toArray(Chemical[]::new);
    }
}