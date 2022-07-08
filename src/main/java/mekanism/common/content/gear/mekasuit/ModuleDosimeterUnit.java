package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.gear.IModule;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

@ParametersAreNotNullByDefault
public class ModuleDosimeterUnit implements ICustomModule<ModuleDosimeterUnit> {

    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "dosimeter.png");

    @Override
    public void addHUDElements(IModule<ModuleDosimeterUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            player.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(capability -> {
                double radiation = MekanismAPI.getRadiationManager().isRadiationEnabled() ? capability.getRadiation() : 0;
                Component text = UnitDisplayUtils.getDisplayShort(radiation, RadiationUnit.SV, 2);
                if (MekanismConfig.common.enableDecayTimers.get() && radiation > RadiationManager.MIN_MAGNITUDE) {
                    text = MekanismLang.GENERIC_WITH_PARENTHESIS.translate(text, TextUtils.getHoursMinutes(RadiationManager.INSTANCE.getDecayTime(radiation, false)));
                }
                HUDColor color;
                if (radiation < RadiationManager.MIN_MAGNITUDE) {
                    color = HUDColor.REGULAR;
                } else {
                    color = radiation < 0.1 ? HUDColor.WARNING : HUDColor.DANGER;
                }
                hudElementAdder.accept(MekanismAPI.getModuleHelper().hudElement(icon, text, color));
            });
        }
    }
}