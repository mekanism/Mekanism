package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.BracketResolver;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BRACKET_HANDLER)
public class CrTBracketHandlers {

    /// Gets the [ICrTChemicalStack] based on registry name. Throws an error if it can't find the [Chemical].
    ///
    /// @param tokens The [Chemical]'s resource location.
    ///
    /// @return A stack of the [Chemical] with an amount of one mB.
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_CHEMICAL)
    public static ICrTChemicalStack getChemicalStack(String tokens) {
        return CrTChemical.makeStack(getValue(CrTConstants.BRACKET_CHEMICAL, tokens, MekanismRegistries.Keys.CHEMICAL), 1);
    }

    /// Gets the [RobitSkin] based on registry name. Throws an error if it can't find the [RobitSkin].
    ///
    /// @param tokens The [RobitSkin]'s resource location.
    ///
    /// @return A reference to the [RobitSkin].
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_ROBIT_SKIN)
    public static RobitSkin getRobitSkin(String tokens) {
        return getValue(CrTConstants.BRACKET_ROBIT_SKIN, tokens, MekanismRegistries.Keys.ROBIT_SKINS);
    }

    /// Gets the [ModuleData] based on registry name. Throws an error if it can't find the [ModuleData].
    ///
    /// @param tokens The [ModuleData]'s resource location.
    ///
    /// @return A reference to the [ModuleData].
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_MODULE_DATA)
    public static ModuleData<?> getModuleData(String tokens) {
        return getValue(CrTConstants.BRACKET_MODULE_DATA, tokens, MekanismRegistries.Keys.MODULES);
    }

    private static <V> V getValue(String bracket, String tokens, ResourceKey<? extends Registry<? extends V>> registryKey) {
        Identifier registryName = Identifier.tryParse(tokens);
        if (registryName == null) {
            String typeName = bracket.replace("_", " ");
            throw new IllegalArgumentException("Could not get " + typeName + " for <" + bracket + ":" + tokens + ">. Syntax is <" + bracket + ":modid:" + bracket + "_name>");
        }
        Registry<V> registry = CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .lookupOrThrow(registryKey);
        if (registry.containsKey(registryName)) {
            return registry.get(registryName);
        }
        String typeName = bracket.replace("_", " ");
        throw new IllegalArgumentException("Could not get " + typeName + " for <" + bracket + ":" + tokens + ">, " + typeName + " does not appear to exist!");
    }
}