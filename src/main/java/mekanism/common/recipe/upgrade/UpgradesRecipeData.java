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
        ItemResource inputType = ItemResource.EMPTY;
        ItemResource outputType = ItemResource.EMPTY;
        long inputAmount = 0;
        long outputAmount = 0;
        for (IInventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                ItemResource resource = slot.getResource();
                long amount = slot.amountAsLong();
                Upgrade upgrade = resource.getItem() instanceof IUpgradeItem upgradeItem ? upgradeItem.getUpgradeType() : null;
                if (upgrade == null) {
                    //Not an upgrade
                    return false;
                }
                if (supportedUpgrades.contains(upgrade)) {
                    if (inputType.isEmpty()) {
                        inputType = resource;
                        inputAmount = amount;
                        continue;
                    } else if (inputAmount < inputType.getMaxStackSize() && resource.equals(inputType)) {
                        long needed = inputType.getMaxStackSize() - inputAmount;
                        if (amount <= needed) {
                            //All fits, increment and continue
                            inputAmount += amount;
                            continue;
                        }
                        //Add what we can from it, and then see if we can add it to the output slot
                        inputAmount += needed;
                        amount -= needed;
                    }
                }
                if (outputType.isEmpty()) {
                    outputType = resource;
                    outputAmount = amount;
                } else if (outputAmount < outputType.getMaxStackSize() && resource.equals(outputType)) {
                    long needed = outputType.getMaxStackSize() - outputAmount;
                    if (amount > needed) {
                        //Doesn't all fit
                        return false;
                    }
                    outputAmount += amount;
                } else {
                    //Can't fit all the items
                    return false;
                }
            }
        }
        //Add any upgrades we might have to the stack, and allow it to take over the map
        stack.set(MekanismDataComponents.UPGRADES, new UpgradeAware(upgrades, inputType, inputAmount, outputType, outputAmount));
        //Try merging stored stacks
        return true;
    }
}