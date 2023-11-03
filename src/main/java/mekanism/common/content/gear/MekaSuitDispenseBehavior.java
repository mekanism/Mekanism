package mekanism.common.content.gear;

import mekanism.api.gear.ICustomModule.ModuleDispenseResult;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MekaSuitDispenseBehavior extends ModuleDispenseBehavior {

    @Override
    protected ModuleDispenseResult performBuiltin(@NotNull BlockSource source, @NotNull ItemStack stack) {
        if (ArmorItem.dispenseArmor(source, stack)) {
            return ModuleDispenseResult.HANDLED;
        }
        return super.performBuiltin(source, stack);
    }
}