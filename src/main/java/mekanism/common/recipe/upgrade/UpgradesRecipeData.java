package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.item.interfaces.IUpgradeItem;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class UpgradesRecipeData implements RecipeUpgradeData<UpgradesRecipeData> {

    private final Map<Upgrade, Integer> upgrades;
    private final List<IInventorySlot> slots;

    UpgradesRecipeData(Map<Upgrade, Integer> upgrades, List<IInventorySlot> slots) {
        this.upgrades = upgrades;
        this.slots = slots;
    }

    @Nullable
    @Override
    public UpgradesRecipeData merge(UpgradesRecipeData other) {
        Map<Upgrade, Integer> smallerUpgrades = other.upgrades;
        Map<Upgrade, Integer> largerUpgrades = upgrades;
        if (largerUpgrades.size() < smallerUpgrades.size()) {
            smallerUpgrades = upgrades;
            largerUpgrades = other.upgrades;
        }
        //Always copy, so we can safely pass the map ownership to the new component
        // as if we are crafting with stacked inputs then it might not line up
        Map<Upgrade, Integer> upgrades = new EnumMap<>(largerUpgrades);
        if (!smallerUpgrades.isEmpty()) {
            //Add smaller to larger, so we have to iterate fewer elements
            for (Map.Entry<Upgrade, Integer> entry : smallerUpgrades.entrySet()) {
                Upgrade upgrade = entry.getKey();
                int total = upgrades.merge(upgrade, entry.getValue(), Integer::sum);
                if (total > upgrade.getMax()) {
                    //Invalid we can't store that many of this type of upgrade
                    return null;
                }
            }
        }
        List<IInventorySlot> allSlots = new ArrayList<>(slots);
        allSlots.addAll(other.slots);
        return new UpgradesRecipeData(upgrades, allSlots);
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (upgrades.isEmpty() && slots.stream().allMatch(IInventorySlot::isEmpty)) {
            return true;
        }
        Set<Upgrade> supportedUpgrades = Collections.emptySet();
        if (stack.getItem() instanceof BlockItem blockItem) {
            AttributeUpgradeSupport upgradeSupport = Attribute.get(blockItem.getBlock(), AttributeUpgradeSupport.class);
            if (upgradeSupport != null) {
                supportedUpgrades = upgradeSupport.supportedUpgrades();
            }
        }
        if (!supportedUpgrades.containsAll(upgrades.keySet())) {
            //Not all upgrades are supported, fail
            return false;
        }
        ItemStack inputStack = ItemStack.EMPTY;
        ItemStack outputStack = ItemStack.EMPTY;
        for (IInventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                ItemResource resource = slot.getResource();
                int amount = slot.getCount();
                Upgrade upgrade = resource.getItem() instanceof IUpgradeItem upgradeItem ? upgradeItem.getUpgradeType() : null;
                if (upgrade == null) {
                    //Not an upgrade
                    return false;
                }
                if (supportedUpgrades.contains(upgrade)) {
                    if (inputStack.isEmpty()) {
                        inputStack = resource.toStack(amount);
                        continue;
                    } else if (inputStack.count() < inputStack.getMaxStackSize() && resource.matches(inputStack)) {
                        int needed = inputStack.getMaxStackSize() - inputStack.count();
                        if (amount <= needed) {
                            inputStack.grow(amount);
                            continue;
                        } else {
                            inputStack.grow(needed);
                            amount -= needed;
                        }
                    }
                }
                if (outputStack.isEmpty()) {
                    outputStack = resource.toStack(amount);
                } else if (outputStack.count() < outputStack.getMaxStackSize() && resource.matches(outputStack)) {
                    int needed = outputStack.getMaxStackSize() - outputStack.count();
                    if (amount > needed) {
                        //Doesn't all fit
                        return false;
                    }
                    outputStack.grow(outputStack.count());
                } else {
                    //Can't fit all the items
                    return false;
                }
            }
        }
        //Add any upgrades we might have to the stack, and allow it to take over the map
        stack.set(MekanismDataComponents.UPGRADES, new UpgradeAware(upgrades, inputStack, outputStack));
        //Try merging stored stacks
        return true;
    }
}