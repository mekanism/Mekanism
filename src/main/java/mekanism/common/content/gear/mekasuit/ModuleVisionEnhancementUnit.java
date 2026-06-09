package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ModuleVisionEnhancementUnit implements ICustomModule<ModuleVisionEnhancementUnit> {

    private static final Identifier icon = MekanismUtils.getResource(ResourceType.GUI_HUD, "vision_enhancement_unit.png");

    @Override
    public void tickServer(IModule<ModuleVisionEnhancementUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        if (module.useAllEnergy(player, itemAccess, MekanismConfig.gear.mekaSuitEnergyUsageVisionEnhancement.get(), transaction)) {
            //TODO - 26.1: Evaluate making the night vision module properly server side declarative?
        }
    }

    @Override
    public <ITEM extends TypedInstance<Item> & DataComponentGetter> void addHUDElements(IModule<ModuleVisionEnhancementUnit> module, IModuleContainer moduleContainer,
          ITEM instance, Player player, Consumer<IHUDElement> hudElementAdder) {
        hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementEnabled(icon, module.isEnabled()));
    }

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleVisionEnhancementUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleVisionEnhancementUnit> module, Player player, ItemAccess itemAccess, int shift,
          boolean displayChangeMessage, @Nullable TransactionContext transaction) {
        module.toggleEnabled(itemAccess, player, MekanismLang.MODULE_VISION_ENHANCEMENT.translate(), transaction);
    }
}