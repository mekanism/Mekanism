package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.inventory.slot.UpgradeInventorySlot;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class UpgradesRecipeData implements RecipeUpgradeData<UpgradesRecipeData> {

    private final Map<Upgrade, Integer> upgrades;
    private final List<IInventorySlot> slots;

    @Nullable
    static UpgradesRecipeData tryCreate(CompoundTag componentUpgrade) {
        if (componentUpgrade.isEmpty()) {
            return null;
        }
        Map<Upgrade, Integer> upgrades = Upgrade.buildMap(componentUpgrade);
        List<IInventorySlot> slots;
        if (componentUpgrade.contains(NBTConstants.ITEMS, Tag.TAG_LIST)) {
            slots = ItemRecipeData.readContents(componentUpgrade.getList(NBTConstants.ITEMS, Tag.TAG_COMPOUND));
        } else {
            slots = Collections.emptyList();
        }
        if (upgrades.isEmpty() && slots.isEmpty()) {
            //There isn't actually any valid data stored
            return null;
        }
        return new UpgradesRecipeData(upgrades, slots);
    }

    private UpgradesRecipeData(Map<Upgrade, Integer> upgrades, List<IInventorySlot> slots) {
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
        Map<Upgrade, Integer> upgrades;
        if (smallerUpgrades.isEmpty()) {
            //If we only have one set of installed upgrades (or none), use the other one rather than trying to merge the upgrades
            upgrades = largerUpgrades;
        } else {
            //Add smaller to larger, so we have to iterate fewer elements
            upgrades = new EnumMap<>(largerUpgrades);
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
        if (upgrades.isEmpty() && slots.isEmpty()) {
            return true;
        }
        AttributeUpgradeSupport upgradeSupport = Attribute.get(((BlockItem) stack.getItem()).getBlock(), AttributeUpgradeSupport.class);
        if (upgradeSupport == null) {
            return false;
        }
        Set<Upgrade> supportedUpgrades = upgradeSupport.supportedUpgrades();
        if (!supportedUpgrades.containsAll(upgrades.keySet())) {
            //Not all upgrades are supported, fail
            return false;
        }
        List<IInventorySlot> stackSlots = List.of(
              UpgradeInventorySlot.input(null, supportedUpgrades),
              UpgradeInventorySlot.output(null)
        );
        CompoundTag nbt = new CompoundTag();
        if (!upgrades.isEmpty()) {
            //Only bother saving which upgrades we have if we actually have any
            Upgrade.saveMap(upgrades, nbt);
        }
        if (ItemRecipeData.applyToStack(slots, stackSlots, toWrite -> nbt.put(NBTConstants.ITEMS, toWrite))) {
            //Try merging stored stacks, writing if needed. If we did merge (even if we didn't have to write)
            // then save and return that we applied to our stack
            ItemDataUtils.setCompound(stack, NBTConstants.COMPONENT_UPGRADE, nbt);
            return true;
        }
        return false;
    }
}