package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.annotations.BracketDumper;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraftforge.registries.IForgeRegistry;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BRACKET_DUMPERS)
public class CrTBracketDumpers {

    @BracketDumper(CrTConstants.BRACKET_GAS)
    public static Collection<String> getGasStackDump() {
        return getChemicalStackDump(MekanismAPI.gasRegistry(), CrTUtils::stackFromGas);
    }

    @BracketDumper(CrTConstants.BRACKET_INFUSE_TYPE)
    public static Collection<String> getInfusionStackDump() {
        return getChemicalStackDump(MekanismAPI.infuseTypeRegistry(), CrTUtils::stackFromInfuseType);
    }

    @BracketDumper(CrTConstants.BRACKET_PIGMENT)
    public static Collection<String> getPigmentStackDump() {
        return getChemicalStackDump(MekanismAPI.pigmentRegistry(), CrTUtils::stackFromPigment);
    }

    @BracketDumper(CrTConstants.BRACKET_SLURRY)
    public static Collection<String> getSlurryStackDump() {
        return getChemicalStackDump(MekanismAPI.slurryRegistry(), CrTUtils::stackFromSlurry);
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, ?, ?, CRT_STACK>> Collection<String>
    getChemicalStackDump(IForgeRegistry<CHEMICAL> registry, Function<CHEMICAL, CRT_STACK> getter) {
        return registry.getValues()
              .stream()
              .map(chemical -> getter.apply(chemical).getCommandString())
              .collect(Collectors.toList());
    }
}