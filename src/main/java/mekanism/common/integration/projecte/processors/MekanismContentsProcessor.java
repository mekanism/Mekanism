package mekanism.common.integration.projecte.processors;

import java.util.Map;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.recipe.upgrade.ItemRecipeData;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.UpgradeUtils;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.nbt.INBTProcessor;
import moze_intel.projecte.api.nbt.NBTProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
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
        IEMCProxy emcProxy = ProjectEAPI.getEMCProxy();
        ItemStack stack = info.createStack();
        //Stored items
        if (stack.getItem() instanceof IItemSustainedInventory sustainedInventory) {
            ListTag storedContents = sustainedInventory.getInventory(stack);
            for (IInventorySlot slot : ItemRecipeData.readContents(storedContents)) {
                if (!slot.isEmpty()) {
                    currentEMC = addEmc(emcProxy, currentEMC, slot.getStack());
                }
            }
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            //Stored upgrades
            if (Attribute.has(blockItem.getBlock(), AttributeUpgradeSupport.class) && ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_UPGRADE, Tag.TAG_COMPOUND)) {
                Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_UPGRADE));
                for (Map.Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                    currentEMC = addEmc(emcProxy, currentEMC, UpgradeUtils.getStack(entry.getKey(), entry.getValue()));
                }
            }
        }
        //Stored modules
        if (stack.getItem() instanceof IModuleContainerItem moduleContainerItem) {
            for (Module<?> module : moduleContainerItem.getModules(stack)) {
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