package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import mekanism.api.Upgrade;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.world.item.ItemStack;
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
        if (upgrades.isEmpty() && slots.stream().allMatch(IInventorySlot::isEmpty)) {
            return true;
        }
        UpgradeAware upgradeAware = stack.getData(MekanismAttachmentTypes.UPGRADES);
        if (!upgradeAware.getSupportedUpgrades().containsAll(upgrades.keySet())) {
            //Not all upgrades are supported, fail
            return false;
        }
        //Add any upgrades we might have to the stack
        upgradeAware.setUpgrades(upgrades);
        //Try merging stored stacks
        return ItemRecipeData.applyToStack(upgradeAware, slots);
    }
}