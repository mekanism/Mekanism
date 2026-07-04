package mekanism.common.recipe.upgrade;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.upgrade.IUpgradeHelper;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.component.component.UpgradeAware;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class UpgradesRecipeData implements RecipeUpgradeData<UpgradesRecipeData> {

    private final Object2IntMap<Holder<Upgrade>> upgrades;
    private final List<LargeResourceStack<ItemResource>> slots;

    UpgradesRecipeData(Object2IntMap<Holder<Upgrade>> upgrades, List<LargeResourceStack<ItemResource>> slots) {
        this.upgrades = upgrades;
        this.slots = slots;
    }

    @Nullable
    @Override
    public UpgradesRecipeData merge(UpgradesRecipeData other) {
        Object2IntMap<Holder<Upgrade>> smallerUpgrades = other.upgrades;
        Object2IntMap<Holder<Upgrade>> largerUpgrades = this.upgrades;
        if (largerUpgrades.size() < smallerUpgrades.size()) {
            smallerUpgrades = this.upgrades;
            largerUpgrades = other.upgrades;
        }
        //Always copy, so we can safely pass the map ownership to the new component
        // as if we are crafting with stacked inputs then it might not line up
        Object2IntMap<Holder<Upgrade>> upgrades = new Object2IntOpenHashMap<>(largerUpgrades);
        if (!smallerUpgrades.isEmpty()) {
            //Add smaller to larger, so we have to iterate fewer elements
            for (ObjectIterator<Object2IntMap.Entry<Holder<Upgrade>>> iterator = Object2IntMaps.fastIterator(smallerUpgrades); iterator.hasNext(); ) {
                Object2IntMap.Entry<Holder<Upgrade>> entry = iterator.next();
                Holder<Upgrade> upgrade = entry.getKey();
                int total = upgrades.mergeInt(upgrade, entry.getIntValue(), Integer::sum);
                if (total > upgrade.value().max()) {
                    //Invalid we can't store that many of this type of upgrade
                    return null;
                }
            }
        }
        List<LargeResourceStack<ItemResource>> allSlots = new ArrayList<>(slots);
        allSlots.addAll(other.slots);
        return new UpgradesRecipeData(upgrades, allSlots);
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess, TransactionContext transaction) {
        if (upgrades.isEmpty() && slots.stream().allMatch(LargeResourceStack::isEmpty)) {
            return true;
        }
        ItemResource itemType = itemAccess.getResource();
        TagKey<Upgrade> supportedUpgrades = null;
        if (itemType.getItem() instanceof BlockItem blockItem) {
            AttributeUpgradeSupport upgradeSupport = Attribute.get(blockItem.getBlock(), AttributeUpgradeSupport.class);
            if (upgradeSupport != null) {
                supportedUpgrades = upgradeSupport.supportedUpgrades();
            }
        }
        if (supportedUpgrades == null) {
            //Unable to find what upgrades are supported, fail
            return false;
        }
        for (Holder<Upgrade> upgradeHolder : upgrades.keySet()) {
            if (!upgradeHolder.is(supportedUpgrades)) {
                //Upgrade is installed that isn't supported, fail
                return false;
            }
        }
        LargeResourceStack<ItemResource> input = LargeResourceStack.ITEM_HELPER.empty();
        LargeResourceStack<ItemResource> output = LargeResourceStack.ITEM_HELPER.empty();
        for (LargeResourceStack<ItemResource> slot : slots) {
            if (slot.isEmpty()) {
                continue;
            }
            ItemResource resource = slot.resource();
            long amount = slot.amount();
            Holder<Upgrade> upgradeType = resource.get(IUpgradeHelper.INSTANCE.dataComponent());
            if (upgradeType == null) {
                //Not an upgrade
                return false;
            }
            int maxStackSize = resource.getMaxStackSize();
            if (upgradeType.is(supportedUpgrades)) {
                if (input.isEmpty()) {
                    input = slot;
                    continue;
                } else if (input.matches(resource)) {
                    long needed = maxStackSize - input.amount();
                    if (amount <= needed) {
                        //All fits, increment and continue
                        input = input.grow(amount, false);
                        continue;
                    }
                    //Add what we can from it, and then see if we can add it to the output slot
                    input = input.grow(needed, false);
                    amount -= needed;
                }
            }
            if (output.isEmpty()) {
                //Note: We can't just re-use slot as the stack as amount might have changed
                output = LargeResourceStack.ITEM_HELPER.createStack(resource, amount);
            } else if (output.matches(resource)) {
                long needed = maxStackSize - output.amount();
                if (amount > needed) {
                    //Doesn't all fit
                    return false;
                }
                output = output.grow(amount, false);
            } else {
                //Can't fit all the items
                return false;
            }
        }
        //Add any upgrades we might have to the stack, and allow it to take over the map
        return ItemAccessUtils.exchange(itemAccess, itemType.with(MekanismDataComponents.UPGRADES, new UpgradeAware(upgrades, input, output)), transaction);
    }
}