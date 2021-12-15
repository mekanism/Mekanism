package mekanism.common.content.gear;

import javax.annotation.Nonnull;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ICustomModule.ModuleDispenseResult;
import mekanism.api.gear.IModule;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.OptionalDispenseBehavior;
import net.minecraft.item.ItemStack;

public class ModuleDispenseBehavior extends OptionalDispenseBehavior {

    @Nonnull
    @Override
    protected ItemStack execute(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
        //Note: We don't check if the stack is empty as it is never checked in vanilla's ones, and we also
        // don't check if the stack is a module container as we only register this dispense behavior on stacks that are
        setSuccess(true);
        ModuleDispenseResult result = performBuiltin(source, stack);
        if (result == ModuleDispenseResult.HANDLED) {
            return stack;
        }
        boolean preventDrop = result == ModuleDispenseResult.FAIL_PREVENT_DROP;
        for (Module<?> module : ModuleHelper.INSTANCE.loadAll(stack)) {
            if (module.isEnabled()) {
                result = onModuleDispense(module, source);
                if (result == ModuleDispenseResult.HANDLED) {
                    return stack;
                }
                preventDrop |= result == ModuleDispenseResult.FAIL_PREVENT_DROP;
            }
        }
        if (preventDrop) {
            setSuccess(false);
            return stack;
        }
        //Note: We don't mark it as a "failed" so that it plays to proper sound when it is ejecting the item
        return super.execute(source, stack);
    }

    private <MODULE extends ICustomModule<MODULE>> ModuleDispenseResult onModuleDispense(IModule<MODULE> module, @Nonnull IBlockSource source) {
        return module.getCustomInstance().onDispense(module, source);
    }

    protected ModuleDispenseResult performBuiltin(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
        return ModuleDispenseResult.DEFAULT;
    }
}