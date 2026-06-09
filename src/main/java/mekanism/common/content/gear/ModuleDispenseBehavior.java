package mekanism.common.content.gear;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ICustomModule.ModuleDispenseResult;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.lib.transaction.TransactionHelper;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class ModuleDispenseBehavior extends OptionalDispenseItemBehavior {

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        //Note: We don't check if the stack is empty as it is never checked in vanilla's ones, and we also
        // don't check if the stack is a module container as we only register this dispense behavior on stacks that are
        setSuccess(true);
        ModuleDispenseResult result = performBuiltin(source, stack);
        if (result == ModuleDispenseResult.HANDLED) {
            return stack;
        }
        boolean preventDrop = result == ModuleDispenseResult.FAIL_PREVENT_DROP;
        IModuleContainer moduleContainer = IModuleHelper.INSTANCE.getModuleContainer(stack);
        if (moduleContainer != null) {
            ItemAccess itemAccess = ItemAccess.forStack(stack);
            for (IModule<?> module : moduleContainer.modules()) {
                if (module.isEnabled()) {
                    //Protect against any mods that might be doing transactional logic, such as if a custom dispenser validates it has enough energy before calling this method
                    try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                        result = onModuleDispense(module, itemAccess, source, transaction);
                        if (result == ModuleDispenseResult.HANDLED) {
                            transaction.commit();
                            return stack;
                        }
                        preventDrop |= result == ModuleDispenseResult.FAIL_PREVENT_DROP;
                    }
                }
            }
        }
        if (preventDrop) {
            setSuccess(false);
            return stack;
        }
        //Note: We don't mark it as a "failed" so that it plays to proper sound when it is ejecting the item
        return super.execute(source, stack);
    }

    private <MODULE extends ICustomModule<MODULE>> ModuleDispenseResult onModuleDispense(IModule<MODULE> module, ItemAccess itemAccess, BlockSource source,
          TransactionContext transaction) {
        return module.getCustomInstance().onDispense(module, itemAccess, source, transaction);
    }

    protected ModuleDispenseResult performBuiltin(BlockSource source, ItemStack stack) {
        return ModuleDispenseResult.DEFAULT;
    }
}