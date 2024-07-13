package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

@ParametersAreNotNullByDefault
public record ModuleHydrostaticRepulsorUnit(boolean swimBoost) implements ICustomModule<ModuleHydrostaticRepulsorUnit> {

    private static final ResourceLocation WATER_MOVEMENT = Mekanism.rl("water_movement");
    public static final ResourceLocation SWIM_BOOST = Mekanism.rl("swim_boost");
    private static final AttributeModifier SWIM_BOOST_MODIFIER = new AttributeModifier(SWIM_BOOST, 1, AttributeModifier.Operation.ADD_VALUE);
    public static final int BOOST_STACKS = 4;

    public ModuleHydrostaticRepulsorUnit(IModule<ModuleHydrostaticRepulsorUnit> module) {
        this(module.getBooleanConfigOrFalse(SWIM_BOOST));
    }

    @Override
    public void adjustAttributes(IModule<ModuleHydrostaticRepulsorUnit> module, ItemAttributeModifierEvent event) {
        //Clamp out at a max efficiency of one (at three installed units)
        //Note: Value copied from default for depth strider
        AttributeModifier modifier = new AttributeModifier(WATER_MOVEMENT, Math.min(1, 0.33333334F * module.getInstalledCount()), AttributeModifier.Operation.ADD_VALUE);
        event.addModifier(Attributes.WATER_MOVEMENT_EFFICIENCY, modifier, EquipmentSlotGroup.LEGS);
        if (isSwimBoost(module, event.getItemStack())) {
            event.addModifier(NeoForgeMod.SWIM_SPEED, SWIM_BOOST_MODIFIER, EquipmentSlotGroup.LEGS);
        }
    }

    @Override
    public void tickServer(IModule<ModuleHydrostaticRepulsorUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        if (isSwimBoost(module, stack) && !player.getMaxHeightFluidType().isAir()) {
            module.useEnergy(player, stack, MekanismConfig.gear.mekaSuitEnergyUsageHydrostaticRepulsion.get());
        }
    }

    private boolean isSwimBoost(IModule<ModuleHydrostaticRepulsorUnit> module, ItemStack stack) {
        return swimBoost && module.getInstalledCount() >= BOOST_STACKS && module.hasEnoughEnergy(stack, MekanismConfig.gear.mekaSuitEnergyUsageHydrostaticRepulsion);
    }
}