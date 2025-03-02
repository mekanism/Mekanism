package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.registries.MekanismModules;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public class ModuleElytraUnit implements ICustomModule<ModuleElytraUnit> {

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleElytraUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleElytraUnit> module, Player player, IModuleContainer moduleContainer, ItemStack stack, int shift, boolean displayChangeMessage) {
        module.toggleEnabled(moduleContainer, stack, player, TextComponentUtil.build(MekanismModules.ELYTRA_UNIT));
    }
}