package mekanism.common.integration.crafttweaker.bracket;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.BracketDumper;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_BRACKET_DUMPERS)
public class CrTBracketDumpers {

    /// Bracket dumper to get all registered [`chemicals`][Chemical] as a collection of their bracket representations.
    @BracketDumper(value = CrTConstants.BRACKET_CHEMICAL, subCommandName = "chemicals")
    public static Collection<String> getChemicalStackDump() {
        return getDump(MekanismAPI.CHEMICAL_REGISTRY_NAME, CrTConstants.BRACKET_CHEMICAL);
    }

    /// Bracket dumper to get all registered [`robit skins`][mekanism.api.robit.RobitSkin] as a collection of their bracket representations.
    @BracketDumper(value = CrTConstants.BRACKET_ROBIT_SKIN, subCommandName = "robitSkins")
    public static Collection<String> getRobitSkinDump() {
        return getDump(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, CrTConstants.BRACKET_ROBIT_SKIN);
    }

    /// Bracket dumper to get all registered [`modules`][mekanism.api.gear.ModuleData] as a collection of their bracket representations.
    @BracketDumper(value = CrTConstants.BRACKET_MODULE_DATA, subCommandName = "moduleData")
    public static Collection<String> getModuleDataDump() {
        return getDump(MekanismAPI.MODULE_REGISTRY_NAME, CrTConstants.BRACKET_MODULE_DATA);
    }

    private static Collection<String> getDump(ResourceKey<? extends Registry<?>> registryKey, String bracket) {
        Optional<Registry<Object>> optionalRegistry = CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .lookup(registryKey);
        if (optionalRegistry.isEmpty()) {
            return Collections.emptyList();
        }
        Registry<?> registry = optionalRegistry.get();
        List<String> list = new ArrayList<>(registry.size());
        for (Identifier v : registry.keySet()) {
            list.add("<" + bracket + ":" + v + ">");
        }
        return list;
    }
}