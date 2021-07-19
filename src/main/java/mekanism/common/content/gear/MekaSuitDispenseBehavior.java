package mekanism.common.content.gear;

import javax.annotation.Nonnull;
import mekanism.api.gear.ICustomModule.ModuleDispenseResult;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public class MekaSuitDispenseBehavior extends ModuleDispenseBehavior {

    @Override
    protected ModuleDispenseResult performBuiltin(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
        if (ArmorItem.dispenseArmor(source, stack)) {
            return ModuleDispenseResult.HANDLED;
        }
        return super.performBuiltin(source, stack);
    }
}