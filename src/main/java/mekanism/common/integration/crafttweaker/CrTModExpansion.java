package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.mod.Mod;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@TypedExpansion(Mod.class)
public class CrTModExpansion {

    /**
     * Gets the gases that are registered under this mod's ID.
     *
     * @return A list of gases that were registered under this mod's ID.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("gases")
    public static Collection<Gas> getGases(Mod _this) {
        return getModSpecific(_this, MekanismAPI.GAS_REGISTRY);
    }

    /**
     * Gets the infuse types that are registered under this mod's ID.
     *
     * @return A list of infuse types that were registered under this mod's ID.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("infuseTypes")
    public static Collection<InfuseType> getInfuseTypes(Mod _this) {
        return getModSpecific(_this, MekanismAPI.INFUSE_TYPE_REGISTRY);
    }

    /**
     * Gets the pigments that are registered under this mod's ID.
     *
     * @return A list of pigments that were registered under this mod's ID.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("pigments")
    public static Collection<Pigment> getPigments(Mod _this) {
        return getModSpecific(_this, MekanismAPI.PIGMENT_REGISTRY);
    }

    /**
     * Gets the slurries that are registered under this mod's ID.
     *
     * @return A list of slurries that were registered under this mod's ID.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("slurries")
    public static Collection<Slurry> getSlurries(Mod _this) {
        return getModSpecific(_this, MekanismAPI.SLURRY_REGISTRY);
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
        String modid = mod.id();
        return allElements.stream()
              .filter(entry -> entry.getKey().location().getNamespace().equals(modid))
              .map(Map.Entry::getValue)
              .toList();
    }
}