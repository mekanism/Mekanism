package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.common.Mekanism;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

@ParametersAreNotNullByDefault
public class GyroscopicStabilizationUnit implements ICustomModule<GyroscopicStabilizationUnit> {

    private static final AttributeModifier UNDER_WATER_SPEED = new AttributeModifier(Mekanism.rl("submerged_mining_speed"), 1, AttributeModifier.Operation.ADD_VALUE);

    @Override
    public void adjustAttributes(IModule<GyroscopicStabilizationUnit> module, ItemAttributeModifierEvent event) {
        event.addModifier(Attributes.SUBMERGED_MINING_SPEED, UNDER_WATER_SPEED, EquipmentSlotGroup.LEGS);
    }
}