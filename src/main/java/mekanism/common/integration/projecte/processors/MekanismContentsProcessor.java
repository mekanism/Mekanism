package mekanism.common.integration.projecte.processors;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import mekanism.api.Upgrade;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.UpgradeUtils;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;
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
        currentEMC = addEmc(emcProxy, currentEMC, ContainerType.ITEM.getAttachmentContainersIfPresent(stack));
        Optional<AbstractPersonalStorageItemInventory> personalStorage = PersonalStorageManager.getInventoryIfPresent(stack);
        if (personalStorage.isPresent()) {//Items stored in a personal chest or barrel
            currentEMC = addEmc(emcProxy, currentEMC, personalStorage.get().getInventorySlots(null));
        }
        UpgradeAware upgradeAware = stack.get(MekanismDataComponents.UPGRADES);
        if (upgradeAware != null) {//Stored upgrades
            for (Map.Entry<Upgrade, Integer> entry : upgradeAware.upgrades().entrySet()) {
                currentEMC = addEmc(emcProxy, currentEMC, UpgradeUtils.getStack(entry.getKey(), entry.getValue()));
            }
            currentEMC = addEmc(emcProxy, currentEMC, upgradeAware.asInventorySlots());
        }
        //Stored modules
        for (IModule<?> module : IModuleHelper.INSTANCE.getAllModules(stack)) {
            ItemStack moduleStack = module.getData().getItemProvider().getItemStack(module.getInstalledCount());
            currentEMC = addEmc(emcProxy, currentEMC, moduleStack);
        }
        return currentEMC;
    }

    private static long addEmc(IEMCProxy emcProxy, long currentEMC, List<IInventorySlot> slots) throws ArithmeticException {
        for (IInventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                currentEMC = addEmc(emcProxy, currentEMC, slot.getStack());
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
