package mekanism.common.content.gear;

import mekanism.api.gear.ICustomModule.ModuleDispenseResult;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;

public class MekaSuitDispenseBehavior extends ModuleDispenseBehavior {

    @Override
    protected ModuleDispenseResult performBuiltin(BlockSource source, ItemStack stack) {
        if (EquipmentDispenseItemBehavior.dispenseEquipment(source, stack)) {
            return ModuleDispenseResult.HANDLED;
        }
        return super.performBuiltin(source, stack);
    }
}