package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.mod.Mod;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@TypedExpansion(Mod.class)
public class CrTModExpansion {

    /**
     * Gets the chemicals that are registered under this mod's ID.
     *
     * @return A list of chemicals that were registered under this mod's ID.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("chemicals")
    public static Collection<Chemical> getChemicals(Mod _this) {
        return getModSpecific(_this, MekanismAPI.CHEMICAL_REGISTRY);
    }

    /**
     * Gets the module types that are registered under this mod's ID.
     *
     * @return A list of module types that were registered under this mod's ID.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("modules")
    public static Collection<ModuleData<?>> getModules(Mod _this) {
        return getModSpecific(_this, MekanismAPI.MODULE_REGISTRY);
    }

    /**
     * Gets the robit skins that are registered under this mod's ID.
     *
     * @return A list of robit skins that were registered under this mod's ID.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("robitSkins")
    public static Collection<RobitSkin> getRobitSkins(Mod _this) {
        return getModSpecific(_this, CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .registryOrThrow(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME)
              .entrySet());
    }

    private static <TYPE> Collection<TYPE> getModSpecific(Mod mod, Registry<TYPE> registry) {
        return getModSpecific(mod, registry.entrySet());
    }

    private static <TYPE> Collection<TYPE> getModSpecific(Mod mod, Set<Entry<ResourceKey<TYPE>, TYPE>> allElements) {
        List<TYPE> list = new ArrayList<>();
        for (Entry<ResourceKey<TYPE>, TYPE> entry : allElements) {
            if (entry.getKey().location().getNamespace().equals(mod.id())) {
                list.add(entry.getValue());
            }
        }
        return list;
    }
}