package mekanism.common.integration.projecte.processors;

import java.util.Map;
import java.util.Optional;
import mekanism.api.Upgrade;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.UpgradeAware;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.recipe.upgrade.ItemRecipeData;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.util.UpgradeUtils;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NBTProcessor
public class MekanismContentsProcessor implements INBTProcessor {

    @Override
    public String getName() {
        return "MekanismContentsProcessor";
    }

    @Override
    public String getDescription() {
        return "Increases the EMC value of any Mekanism items by the value of the stored or installed contents.";
    }

    @Override
    public long recalculateEMC(@NotNull ItemInfo info, long currentEMC) throws ArithmeticException {
        IEMCProxy emcProxy = IEMCProxy.INSTANCE;
        ItemStack stack = info.createStack();
        //Stored items
        if (stack.getItem() instanceof IItemSustainedInventory sustainedInventory) {
            ListTag storedContents = sustainedInventory.getSustainedInventory(stack);
            for (IInventorySlot slot : ItemRecipeData.readContents(storedContents)) {
                if (!slot.isEmpty()) {
                    currentEMC = addEmc(emcProxy, currentEMC, slot.getStack());
                }
            }
        }
        if (stack.hasData(MekanismAttachmentTypes.UPGRADES)) {//Stored upgrades
            UpgradeAware upgradeAware = stack.getData(MekanismAttachmentTypes.UPGRADES);
            for (Map.Entry<Upgrade, Integer> entry : upgradeAware.getUpgrades().entrySet()) {
                currentEMC = addEmc(emcProxy, currentEMC, UpgradeUtils.getStack(entry.getKey(), entry.getValue()));
            }
            for (IInventorySlot slot : upgradeAware.getInventorySlots(null)) {
                if (!slot.isEmpty()) {
                    currentEMC = addEmc(emcProxy, currentEMC, slot.getStack());
                }
            }
        }
        //Stored modules
        Optional<? extends IModuleContainer> moduleContainer = IModuleHelper.INSTANCE.getModuleContainer(stack);
        if (moduleContainer.isPresent()) {
            for (IModule<?> module : moduleContainer.get().modules()) {
                ItemStack moduleStack = module.getData().getItemProvider().getItemStack(module.getInstalledCount());
                currentEMC = addEmc(emcProxy, currentEMC, moduleStack);
            }
        }
        return currentEMC;
    }

    private static long addEmc(IEMCProxy emcProxy, long currentEMC, ItemStack stack) throws ArithmeticException {
        long itemEmc = emcProxy.getValue(stack);
        if (itemEmc > 0) {
            long stackEmc = Math.multiplyExact(itemEmc, stack.getCount());
            currentEMC = Math.addExact(currentEMC, stackEmc);
        }
        return currentEMC;
    }
}
