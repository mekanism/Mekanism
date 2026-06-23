package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.RadiationUtil;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class ModuleDosimeterUnit implements ICustomModule<ModuleDosimeterUnit> {

    private static final Identifier icon = Mekanism.rl("hud/dosimeter");

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDElements(IModule<ModuleDosimeterUnit> module, IModuleContainer moduleContainer, ITEM instance,
          Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            double radiation = RadiationManager.isGlobalRadiationEnabled() ? player.getData(MekanismAttachmentTypes.RADIATION) : 0;
            Component text = UnitDisplayUtils.getDisplayShort(radiation, RadiationUnit.SV, 2);
            if (MekanismConfig.common.enableDecayTimers.get() && radiation > IRadiationManager.INSTANCE.minRadiationMagnitude()) {
                text = MekanismLang.GENERIC_WITH_PARENTHESIS.translate(text, TextUtils.getHoursMinutes(player.level(),
                      RadiationUtil.getDecayTime(radiation, false)));
            }
            HUDColor color;
            if (radiation < IRadiationManager.INSTANCE.minRadiationMagnitude()) {
                color = HUDColor.REGULAR;
            } else {
                color = radiation < 0.1 ? HUDColor.WARNING : HUDColor.DANGER;
            }
            hudElementAdder.accept(IModuleHelper.INSTANCE.hudElement(icon, text, color));
        }
    }
}