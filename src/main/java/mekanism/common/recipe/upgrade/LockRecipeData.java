package mekanism.common.recipe.upgrade;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.inventory.slot.BinInventorySlot;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class LockRecipeData implements RecipeUpgradeData<LockRecipeData> {

    private final ItemResource lockType;

    LockRecipeData(ItemResource lockType) {
        this.lockType = lockType;
    }

    @Nullable
    @Override
    public LockRecipeData merge(LockRecipeData other) {
        return lockType.equals(other.lockType) ? this : null;
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess) {
        ComponentBackedBinInventorySlot slot = BinInventorySlot.getForAccess(itemAccess);
        if (slot == null) {
            return false;
        }
        return slot.setLockType(this.lockType);
    }
}