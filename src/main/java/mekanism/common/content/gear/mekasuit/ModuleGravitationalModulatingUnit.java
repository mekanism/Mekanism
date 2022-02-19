package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.gear.config.ModuleEnumData;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekasuit.ModuleLocomotiveBoostingUnit.SprintBoost;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;

@ParametersAreNonnullByDefault
public class ModuleGravitationalModulatingUnit implements ICustomModule<ModuleGravitationalModulatingUnit> {

    private static final ResourceLocation icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "gravitational_modulation_unit.png");

    // we share with locomotive boosting unit
    private IModuleConfigItem<SprintBoost> speedBoost;

    @Override
    public void init(IModule<ModuleGravitationalModulatingUnit> module, ModuleConfigItemCreator configItemCreator) {
        speedBoost = configItemCreator.createConfigItem("speed_boost", MekanismLang.MODULE_SPEED_BOOST, new ModuleEnumData<>(SprintBoost.class, SprintBoost.LOW));
    }

    @Override
    public void addHUDElements(IModule<ModuleGravitationalModulatingUnit> module, PlayerEntity player, Consumer<IHUDElement> hudElementAdder) {
        hudElementAdder.accept(MekanismAPI.getModuleHelper().hudElementEnabled(icon, module.isEnabled()));
    }

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleGravitationalModulatingUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleGravitationalModulatingUnit> module, PlayerEntity player, ItemStack stack, int shift, boolean displayChangeMessage) {
        module.toggleEnabled(player, MekanismLang.MODULE_GRAVITATIONAL_MODULATION.translate());
    }

    public float getBoost() {
        return speedBoost.get().getBoost();
    }

    @Override
    public void tickClient(IModule<ModuleGravitationalModulatingUnit> module, PlayerEntity player) {
        //Client side handling of boost as movement needs to be applied on both the server and the client
        if (player.abilities.flying && MekanismKeyHandler.boostKey.isDown() &&
            module.canUseEnergy(player, MekanismConfig.gear.mekaSuitEnergyUsageGravitationalModulation.get().multiply(4), false)) {
            float boost = getBoost();
            if (boost > 0) {
                player.moveRelative(boost, new Vector3d(0, 0, 1));
            }
        }
    }
}