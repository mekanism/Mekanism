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
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
public class ModuleDosimeterUnit implements ICustomModule<ModuleDosimeterUnit> {

    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "dosimeter.png");

    @Override
    public void addHUDElements(IModule<ModuleDosimeterUnit> module, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            CapabilityUtils.getCapability(Minecraft.getInstance().player, Capabilities.RADIATION_ENTITY_CAPABILITY, null).ifPresent(capability -> {
                double radiation = capability.getRadiation();
                hudElementAdder.accept(MekanismAPI.getModuleHelper().hudElement(icon, UnitDisplayUtils.getDisplayShort(radiation, RadiationUnit.SV, 2),
                      radiation < RadiationManager.MIN_MAGNITUDE ? HUDColor.REGULAR : (radiation < 0.1 ? HUDColor.WARNING : HUDColor.DANGER)));
            });
        }
    }
}