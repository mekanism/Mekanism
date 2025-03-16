package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.BracketResolver;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.chemical.CrTChemical;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BRACKET_HANDLER)
public class CrTBracketHandlers {

    /**
     * Gets the {@link ICrTChemicalStack} based on registry name. Throws an error if it can't find the {@link Chemical}.
     *
     * @param tokens The {@link Chemical}'s resource location.
     *
     * @return A stack of the {@link Chemical} with an amount of one mB.
     */
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_CHEMICAL)
    public static ICrTChemicalStack getChemicalStack(String tokens) {
        return CrTChemical.makeStack(getValue(CrTConstants.BRACKET_CHEMICAL, tokens, MekanismAPI.CHEMICAL_REGISTRY_NAME), 1);
    }

    /**
     * Gets the {@link RobitSkin} based on registry name. Throws an error if it can't find the {@link RobitSkin}.
     *
     * @param tokens The {@link RobitSkin}'s resource location.
     *
     * @return A reference to the {@link RobitSkin}.
     */
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_ROBIT_SKIN)
    public static RobitSkin getRobitSkin(String tokens) {
        return getValue(CrTConstants.BRACKET_ROBIT_SKIN, tokens, MekanismAPI.ROBIT_SKIN_REGISTRY_NAME);
    }

    /**
     * Gets the {@link ModuleData} based on registry name. Throws an error if it can't find the {@link ModuleData}.
     *
     * @param tokens The {@link ModuleData}'s resource location.
     *
     * @return A reference to the {@link ModuleData}.
     */
    @ZenCodeType.Method
    @BracketResolver(CrTConstants.BRACKET_MODULE_DATA)
    public static ModuleData<?> getModuleData(String tokens) {
        return getValue(CrTConstants.BRACKET_MODULE_DATA, tokens, MekanismAPI.MODULE_REGISTRY_NAME);
    }

    private static <V> V getValue(String bracket, String tokens, ResourceKey<? extends Registry<? extends V>> registryKey) {
        ResourceLocation registryName = ResourceLocation.tryParse(tokens);
        if (registryName == null) {
            String typeName = bracket.replace("_", " ");
            throw new IllegalArgumentException("Could not get " + typeName + " for <" + bracket + ":" + tokens + ">. Syntax is <" + bracket + ":modid:" + bracket + "_name>");
        }
        Registry<V> registry = CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .registryOrThrow(registryKey);
        if (registry.containsKey(registryName)) {
            return registry.get(registryName);
        }
        String typeName = bracket.replace("_", " ");
        throw new IllegalArgumentException("Could not get " + typeName + " for <" + bracket + ":" + tokens + ">, " + typeName + " does not appear to exist!");
    }
}