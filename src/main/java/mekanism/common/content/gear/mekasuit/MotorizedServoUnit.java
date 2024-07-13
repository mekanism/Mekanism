package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

@ParametersAreNotNullByDefault
public class MotorizedServoUnit implements ICustomModule<MotorizedServoUnit> {

    private static final ResourceLocation SNEAK_SPEED = Mekanism.rl("motorized_servo");

    @Override
    public void adjustAttributes(IModule<MotorizedServoUnit> module, ItemAttributeModifierEvent event) {
        //Note: Value copied from default for swift sneak
        AttributeModifier modifier = new AttributeModifier(SNEAK_SPEED, Math.min(1, 0.15F * module.getInstalledCount()), AttributeModifier.Operation.ADD_VALUE);
        event.addModifier(Attributes.SNEAKING_SPEED, modifier, EquipmentSlotGroup.LEGS);
    }
}