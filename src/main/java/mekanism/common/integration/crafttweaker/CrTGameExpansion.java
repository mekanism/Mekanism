package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.game.Game;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import java.util.Collection;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@TypedExpansion(Game.class)
public class CrTGameExpansion {

    /**
     * Gets all registered gases.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("gases")
    public static Collection<Gas> getGases(Game _this) {
        return MekanismAPI.gasRegistry().getValues();
    }

    /**
     * Gets all registered infuse types.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("infuseTypes")
    public static Collection<InfuseType> getInfuseTypes(Game _this) {
        return MekanismAPI.infuseTypeRegistry().getValues();
    }

    /**
     * Gets all registered pigments.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("pigments")
    public static Collection<Pigment> getPigments(Game _this) {
        return MekanismAPI.pigmentRegistry().getValues();
    }

    /**
     * Gets all registered slurries.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("slurries")
    public static Collection<Slurry> getSlurries(Game _this) {
        return MekanismAPI.slurryRegistry().getValues();
    }

    /**
     * Gets all registered module types.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("modules")
    public static Collection<ModuleData<?>> getModules(Game _this) {
        return MekanismAPI.moduleRegistry().getValues();
    }

    /**
     * Gets all registered robit skins.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("robitSkins")
    public static Collection<RobitSkin> getRobitSkins(Game _this) {
        return MekanismAPI.robitSkinRegistry().getValues();
    }
}