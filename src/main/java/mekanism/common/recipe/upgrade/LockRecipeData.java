package mekanism.common.recipe.upgrade;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.inventory.slot.BinInventorySlot;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class LockRecipeData implements RecipeUpgradeData<LockRecipeData> {

    private final ItemStack lock;

    LockRecipeData(ItemStack lockStack) {
        this.lock = lockStack;
    }

    @Nullable
    @Override
    public LockRecipeData merge(LockRecipeData other) {
        return ItemStack.isSameItemSameComponents(lock, other.lock) ? this : null;
    }

    @Override
    public boolean applyToStack(HolderLookup.Provider provider, ItemStack stack) {
        ComponentBackedBinInventorySlot slot = BinInventorySlot.getForStack(stack);
        if (slot == null) {
            return false;
        }
        slot.setLockStack(this.lock);
        return true;
    }
}