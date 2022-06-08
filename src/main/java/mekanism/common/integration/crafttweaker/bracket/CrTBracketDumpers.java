package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.annotation.BracketDumper;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.Collection;
import java.util.function.Function;
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

    /**
     * Bracket dumper to get all registered {@link mekanism.api.chemical.gas.Gas gases} as a collection of their bracket representations.
     */
    @BracketDumper(value = CrTConstants.BRACKET_GAS, subCommandName = "gases")
    public static Collection<String> getGasStackDump() {
        return getChemicalStackDump(MekanismAPI.gasRegistry(), CrTUtils::stackFromGas);
    }

    /**
     * Bracket dumper to get all registered {@link mekanism.api.chemical.infuse.InfuseType infuse types} as a collection of their bracket representations.
     */
    @BracketDumper(value = CrTConstants.BRACKET_INFUSE_TYPE, subCommandName = "infuseTypes")
    public static Collection<String> getInfusionStackDump() {
        return getChemicalStackDump(MekanismAPI.infuseTypeRegistry(), CrTUtils::stackFromInfuseType);
    }

    /**
     * Bracket dumper to get all registered {@link mekanism.api.chemical.pigment.Pigment pigments} as a collection of their bracket representations.
     */
    @BracketDumper(value = CrTConstants.BRACKET_PIGMENT, subCommandName = "pigments")
    public static Collection<String> getPigmentStackDump() {
        return getChemicalStackDump(MekanismAPI.pigmentRegistry(), CrTUtils::stackFromPigment);
    }

    /**
     * Bracket dumper to get all registered {@link mekanism.api.chemical.slurry.Slurry slurries} as a collection of their bracket representations.
     */
    @BracketDumper(value = CrTConstants.BRACKET_SLURRY, subCommandName = "slurries")
    public static Collection<String> getSlurryStackDump() {
        return getChemicalStackDump(MekanismAPI.slurryRegistry(), CrTUtils::stackFromSlurry);
    }

    /**
     * Bracket dumper to get all registered {@link mekanism.api.robit.RobitSkin robit skins} as a collection of their bracket representations.
     */
    @BracketDumper(value = CrTConstants.BRACKET_ROBIT_SKIN, subCommandName = "robitSkins")
    public static Collection<String> getRobitSkinDump() {
        return getDump(MekanismAPI.robitSkinRegistry(), CrTConstants.BRACKET_ROBIT_SKIN);
    }

    /**
     * Bracket dumper to get all registered {@link mekanism.api.gear.ModuleData modules} as a collection of their bracket representations.
     */
    @BracketDumper(value = CrTConstants.BRACKET_MODULE_DATA, subCommandName = "moduleData")
    public static Collection<String> getModuleDataDump() {
        return getDump(MekanismAPI.moduleRegistry(), CrTConstants.BRACKET_MODULE_DATA);
    }

    private static <CHEMICAL extends Chemical<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, ?, CRT_STACK>> Collection<String>
    getChemicalStackDump(IForgeRegistry<CHEMICAL> registry, Function<CHEMICAL, CRT_STACK> getter) {
        return getDump(registry, chemical -> getter.apply(chemical).getCommandString());
    }

    private static <V> Collection<String> getDump(IForgeRegistry<V> registry, String bracket) {
        return getDump(registry, v -> "<" + bracket + ":" + registry.getKey(v) + ">");
    }

    private static <V> Collection<String> getDump(IForgeRegistry<V> registry, Function<V, String> getter) {
        return registry.getValues().stream().map(getter).toList();
    }
}