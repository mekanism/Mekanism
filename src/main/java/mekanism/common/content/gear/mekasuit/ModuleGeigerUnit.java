package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.gear.IClientModuleHelper;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.ClientRadiation;
import mekanism.common.lib.radiation.RadiationUtil;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class ModuleGeigerUnit implements ICustomModule<ModuleGeigerUnit> {

    private static final Identifier icon = Mekanism.rl("hud/geiger_counter");

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDElements(IModule<ModuleGeigerUnit> module, IModuleContainer moduleContainer, ITEM instance,
          Player player, Consumer<IHUDElement> hudElementAdder) {
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
            hudElementAdder.accept(IClientModuleHelper.INSTANCE.hudElement(icon, text, color));
        }
    }
}