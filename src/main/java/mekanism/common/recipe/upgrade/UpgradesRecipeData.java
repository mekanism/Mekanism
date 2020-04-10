package mekanism.common.recipe.upgrade;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class UpgradesRecipeData implements RecipeUpgradeData<UpgradesRecipeData> {

    private final Map<Upgrade, Integer> upgrades;

    UpgradesRecipeData(Map<Upgrade, Integer> upgrades) {
        this.upgrades = upgrades;
    }

    @Nullable
    @Override
    public UpgradesRecipeData merge(UpgradesRecipeData other) {
        Map<Upgrade, Integer> upgrades = new EnumMap<>(Upgrade.class);
        for (Upgrade upgrade : EnumUtils.UPGRADES) {
            int total = 0;
            if (this.upgrades.containsKey(upgrade)) {
                total += this.upgrades.get(upgrade);
            }
            if (other.upgrades.containsKey(upgrade)) {
                total += other.upgrades.get(upgrade);
            }
            if (total > upgrade.getMax()) {
                //Invalid we can't store that many of this type of upgrade
                return null;
            } else if (total > 0) {
                upgrades.put(upgrade, total);
            }
        }
        return new UpgradesRecipeData(upgrades);
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        if (upgrades.isEmpty()) {
            return true;
        }
        Set<Upgrade> supportedUpgrades = Attribute.get(((BlockItem) stack.getItem()).getBlock(), AttributeUpgradeSupport.class).getSupportedUpgrades();
        for (Upgrade upgrade : upgrades.keySet()) {
            if (!supportedUpgrades.contains(upgrade)) {
                return false;
            }
        }
        //Only transfer upgrades if we were able to find any
        CompoundNBT nbt = new CompoundNBT();
        Upgrade.saveMap(upgrades, nbt);
        ItemDataUtils.setCompound(stack, NBTConstants.COMPONENT_UPGRADE, nbt);
        return true;
    }
}