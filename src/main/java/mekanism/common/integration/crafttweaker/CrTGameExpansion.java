package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.game.Game;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import java.util.Collection;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@TypedExpansion(Game.class)
public class CrTGameExpansion {

    /**
     * Gets all registered chemicals.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("chemicals")
    public static Collection<Chemical> getChemicals(Game _this) {
        return MekanismAPI.CHEMICAL_REGISTRY.stream().toList();
    }

    /**
     * Gets all registered module types.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("modules")
    public static Collection<ModuleData<?>> getModules(Game _this) {
        return MekanismAPI.MODULE_REGISTRY.stream().toList();
    }

    /**
     * Gets all registered robit skins.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("robitSkins")
    public static Collection<RobitSkin> getRobitSkins(Game _this) {
        return CraftTweakerAPI.getAccessibleElementsProvider()
              .registryAccess()
              .registryOrThrow(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME)
              .stream()
              .toList();
    }
}