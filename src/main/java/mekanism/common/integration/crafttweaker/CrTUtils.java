package mekanism.common.integration.crafttweaker;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack.CrTSlurryStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemical;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTSlurryStack;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType.Nullable;

public class CrTUtils {

    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MekanismHooks.CRAFTTWEAKER_MOD_ID, path);
    }

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

    public static <CHEMICAL extends Chemical<CHEMICAL>, CRT_CHEMICAL extends ICrTChemical<CHEMICAL, ?, CRT_CHEMICAL, ?>> CHEMICAL[]
    getChemicals(CRT_CHEMICAL[] crtChemicals, IntFunction<CHEMICAL[]> arrayCreator) {
        CHEMICAL[] chemicals = arrayCreator.apply(crtChemicals.length);
        for (int i = 0; i < chemicals.length; i++) {
            chemicals[i] = crtChemicals[i].getChemical();
        }
        return chemicals;
    }

    @Nullable
    public static ICrTChemicalStack<?, ?, ?, ?> fromBoxedStack(BoxedChemicalStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        switch (stack.getChemicalType()) {
            case GAS:
                return new CrTGasStack((GasStack) stack.getChemicalStack());
            case INFUSION:
                return new CrTInfusionStack((InfusionStack) stack.getChemicalStack());
            case PIGMENT:
                return new CrTPigmentStack((PigmentStack) stack.getChemicalStack());
            case SLURRY:
                return new CrTSlurryStack((SlurryStack) stack.getChemicalStack());
            default:
                return null;
        }
    }

    public static <TYPE> String describeOutputs(List<TYPE> outputs, Function<TYPE, Object> converter) {
        //Note: This isn't the best but it is probably as close as we can get
        StringBuilder description = new StringBuilder();
        int size = outputs.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                description.append(", or ");
            }
            description.append(converter.apply(outputs.get(i)));
        }
        return description.toString();
    }
}