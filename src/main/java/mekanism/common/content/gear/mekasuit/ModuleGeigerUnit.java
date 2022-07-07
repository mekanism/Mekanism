package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.gear.IModule;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

@ParametersAreNotNullByDefault
public class ModuleGeigerUnit implements ICustomModule<ModuleGeigerUnit> {

    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "geiger_counter.png");

    @Override
    public void addHUDElements(IModule<ModuleGeigerUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            double magnitude = RadiationManager.INSTANCE.getClientEnvironmentalRadiation();
            double baseline = RadiationManager.BASELINE;
            hudElementAdder.accept(MekanismAPI.getModuleHelper().hudElement(icon, MekanismConfig.common.enableDecayTimers.get() && magnitude > baseline ? MekanismLang.GENERIC_WITH_PARENTHESIS.translate(UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SV, 2),
                    TextUtils.getHoursMinutes(RadiationManager.INSTANCE.getDecayTime(RadiationManager.INSTANCE.getClientMaxMagnitude()))) : UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SV, 2), magnitude <= baseline ? HUDColor.REGULAR : magnitude < 0.1 ? HUDColor.WARNING : HUDColor.DANGER));
        }
    }
}