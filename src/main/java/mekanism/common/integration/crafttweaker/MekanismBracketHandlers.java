/*package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotations.BracketResolver;
import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import java.util.Locale;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.integration.crafttweaker.gas.CraftTweakerGasStack;
import mekanism.common.integration.crafttweaker.gas.IGasStack;
import net.minecraft.util.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mekanism.BracketHandlers")
public class MekanismBracketHandlers {

    @BracketResolver("gas")
    public static IGasStack getGas(String tokens) {
        if (!tokens.toLowerCase(Locale.ENGLISH).equals(tokens)) {
            CraftTweakerAPI.logWarning("Gas BEP <item:%s> does not seem to be lower-cased!", tokens);
        }
        //TODO: Make this have both parts of the registry name rather than just being one part
        Gas gas = Gas.getFromRegistry(new ResourceLocation(tokens));
        if (gas.isEmptyType()) {
            throw new IllegalArgumentException("Could not get gas with name: <gas:" + tokens + ">! Gas does not appear to exist!");
        }
        return new CraftTweakerGasStack(new GasStack(gas, 1));
    }
}*/