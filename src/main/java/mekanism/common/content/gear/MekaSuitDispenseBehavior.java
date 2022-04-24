package mekanism.common.content.gear;

import javax.annotation.Nonnull;
import mekanism.api.gear.ICustomModule.ModuleDispenseResult;
import net.minecraft.core.BlockSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class MekaSuitDispenseBehavior extends ModuleDispenseBehavior {

    @Override
    protected ModuleDispenseResult performBuiltin(@Nonnull BlockSource source, @Nonnull ItemStack stack) {
        if (ArmorItem.dispenseArmor(source, stack)) {
            return ModuleDispenseResult.HANDLED;
        }
        return super.performBuiltin(source, stack);
    }
}