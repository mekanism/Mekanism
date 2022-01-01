package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IHUDElement.HUDColor;
import mekanism.api.gear.IModule;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.UnitDisplayUtils.RadiationUnit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
public class ModuleGeigerUnit implements ICustomModule<ModuleGeigerUnit> {

    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "geiger_counter.png");

    @Override
    public void addHUDElements(IModule<ModuleGeigerUnit> module, PlayerEntity player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            double magnitude = RadiationManager.INSTANCE.getClientEnvironmentalRadiation();
            hudElementAdder.accept(MekanismAPI.getModuleHelper().hudElement(icon, UnitDisplayUtils.getDisplayShort(magnitude, RadiationUnit.SV, 2),
                  magnitude < RadiationManager.MIN_MAGNITUDE ? HUDColor.REGULAR : (magnitude < 0.1 ? HUDColor.WARNING : HUDColor.DANGER)));
        }
    }
}