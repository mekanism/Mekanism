package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.ClientRadiation;
import mekanism.common.lib.radiation.RadiationUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public class ModuleGeigerUnit implements ICustomModule<ModuleGeigerUnit> {

    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "geiger_counter.png");

    @Override
    public void addHUDElements(IModule<ModuleGeigerUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            double magnitude = ClientRadiation.getClientEnvironmentalRadiation();
            Component text = UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SV, 2);
            if (MekanismConfig.common.enableDecayTimers.get() && magnitude > IRadiationManager.INSTANCE.baselineRadiation()) {
                double maxMagnitude = ClientRadiation.getClientMaxMagnitude();
                text = MekanismLang.GENERIC_WITH_PARENTHESIS.translate(text, TextUtils.getHoursMinutes(player.level(),
                      RadiationUtil.getDecayTime(maxMagnitude, true)));
            }
            HUDColor color;
            if (magnitude <= IRadiationManager.INSTANCE.baselineRadiation()) {
                color = HUDColor.REGULAR;
            } else {
                color = magnitude < 0.1 ? HUDColor.WARNING : HUDColor.DANGER;
            }
            hudElementAdder.accept(IModuleHelper.INSTANCE.hudElement(icon, text, color));
        }
    }
}