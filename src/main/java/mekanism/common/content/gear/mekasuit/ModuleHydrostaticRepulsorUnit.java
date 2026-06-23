package mekanism.common.content.gear.mekasuit;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public record ModuleHydrostaticRepulsorUnit(boolean swimBoost) implements ICustomModule<ModuleHydrostaticRepulsorUnit> {

    private static final Identifier WATER_MOVEMENT = Mekanism.rl("water_movement");
    public static final Identifier SWIM_BOOST = Mekanism.rl("swim_boost");
    private static final AttributeModifier SWIM_BOOST_MODIFIER = new AttributeModifier(SWIM_BOOST, 1, AttributeModifier.Operation.ADD_VALUE);
    public static final int BOOST_STACKS = 4;

    public ModuleHydrostaticRepulsorUnit(IModule<ModuleHydrostaticRepulsorUnit> module) {
        this(module.getBooleanConfigOrFalse(SWIM_BOOST));
    }

    @Override
    public void adjustAttributes(IModule<ModuleHydrostaticRepulsorUnit> module, IModuleContainer moduleContainer, ItemAttributeModifierEvent event) {
        //Clamp out at a max efficiency of one (at three installed units)
        //Note: Value copied from default for depth strider
        AttributeModifier modifier = new AttributeModifier(WATER_MOVEMENT, Math.min(1, 0.33333334F * module.getInstalledCount()), AttributeModifier.Operation.ADD_VALUE);
        event.addModifier(Attributes.WATER_MOVEMENT_EFFICIENCY, modifier, EquipmentSlotGroup.LEGS);
        if (isSwimBoost(module) && module.hasEnoughEnergy(ItemAccessUtils.sideEffectFreeAccess(event.getItemStack()), MekanismConfig.gear.mekaSuitEnergyUsageHydrostaticRepulsion)) {
            event.addModifier(NeoForgeMod.SWIM_SPEED, SWIM_BOOST_MODIFIER, EquipmentSlotGroup.LEGS);
        }
    }

    @Override
    public void tickServer(IModule<ModuleHydrostaticRepulsorUnit> module, ItemAccess itemAccess, Player player, TransactionContext transaction) {
        //TODO - 26.2 if we want to do more than water, EntityFluidInteraction needs interrogating
        if (isSwimBoost(module) && player.isEyeInFluid(FluidTags.WATER)) {
            //Note: While we let creative players not use energy, we require that enough energy is present when we modify the attributes, as we don't have an entity context
            module.useAllEnergy(null, itemAccess, MekanismConfig.gear.mekaSuitEnergyUsageHydrostaticRepulsion.get(), transaction);
        }
    }

    private boolean isSwimBoost(IModule<ModuleHydrostaticRepulsorUnit> module) {
        return swimBoost && module.getInstalledCount() >= BOOST_STACKS;
    }
}