package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.registries.MekanismModules;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

@ParametersAreNotNullByDefault
public class ModuleElytraUnit implements ICustomModule<ModuleElytraUnit> {

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleElytraUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleElytraUnit> module, Player player, ItemAccess itemAccess, int shift, boolean displayChangeMessage,
          @Nullable TransactionContext transaction) {
        module.toggleEnabled(itemAccess, player, TextComponentUtil.build(MekanismModules.ELYTRA_UNIT), transaction);
    }
}