package mekanism.common.content.gear.mekasuit;

import mekanism.api.chemical.ChemicalIds;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IJetpackItem.JetpackMode;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.ChemicalUtils;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class ModuleElytraUnit implements ICustomModule<ModuleElytraUnit> {

    private static final Identifier ELYTRA_FLIGHT_MODIFIER_ID = Mekanism.rl("elytra_flight");
    private static final AttributeModifier ELYTRA_FLIGHT_MODIFIER = new AttributeModifier(ELYTRA_FLIGHT_MODIFIER_ID, 1, AttributeModifier.Operation.ADD_VALUE);

    @Override
    public boolean canChangeModeWhenDisabled(IModule<ModuleElytraUnit> module) {
        return true;
    }

    @Override
    public void changeMode(IModule<ModuleElytraUnit> module, Player player, ItemAccess itemAccess, int shift, boolean displayChangeMessage,
          @Nullable TransactionContext transaction) {
        module.toggleEnabled(itemAccess, player, TextComponentUtil.build(MekanismModules.ELYTRA_UNIT), transaction);
    }

    @Override
    public void adjustAttributes(IModule<ModuleElytraUnit> module, IModuleContainer moduleContainer, ItemAttributeModifierEvent event) {
        ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(event.getItemStack());
        if (module.hasEnoughEnergy(itemAccess, MekanismConfig.gear.mekaSuitElytraEnergyUsage)) {
            //If we have enough energy to use the elytra, check if the jetpack unit is also installed, and if it is,
            // only mark that we can use the elytra if the jetpack is not set to hover or if it is if it has no hydrogen stored
            IModule<ModuleJetpackUnit> jetpack = moduleContainer.getIfEnabled(MekanismModules.JETPACK_UNIT);
            if (jetpack == null || jetpack.getCustomInstance().mode() != JetpackMode.HOVER || !ChemicalUtils.hasChemicalOfType(itemAccess, ChemicalIds.HYDROGEN)) {
                //TODO - 26.2: Elytra - https://github.com/neoforged/NeoForge/pull/3192
                //event.addModifier(NeoForgeMod.GLIDING_FLIGHT, ELYTRA_FLIGHT_MODIFIER, EquipmentSlotGroup.CHEST);
            }
        }
    }
}