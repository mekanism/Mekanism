package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.gear.IModule;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

@ParametersAreNonnullByDefault
public class ModuleDosimeterUnit implements ICustomModule<ModuleDosimeterUnit> {

    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "dosimeter.png");

    @Override
    public void addHUDElements(IModule<ModuleDosimeterUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(capability -> {
                double radiation = MekanismAPI.getRadiationManager().isRadiationEnabled() ? capability.getRadiation() : 0;
                hudElementAdder.accept(MekanismAPI.getModuleHelper().hudElement(icon, UnitDisplayUtils.getDisplayShort(radiation, RadiationUnit.SV, 2),
                      radiation < RadiationManager.MIN_MAGNITUDE ? HUDColor.REGULAR : (radiation < 0.1 ? HUDColor.WARNING : HUDColor.DANGER)));
            });
        }
    }
}