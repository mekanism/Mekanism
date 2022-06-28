package mekanism.common.recipe.upgrade;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.inventory.BinMekanismInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class LockRecipeData implements RecipeUpgradeData<LockRecipeData> {

    private final ItemStack lock;

    LockRecipeData(BinMekanismInventory inventory) {
        this.lock = inventory.getBinSlot().getLockStack();
    }

    @Nullable
    @Override
    public LockRecipeData merge(LockRecipeData other) {
        return ItemHandlerHelper.canItemStacksStack(lock, other.lock) ? this : null;
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        BinMekanismInventory inventory = BinMekanismInventory.create(stack);
        if (inventory == null) {
            return false;
        }
        inventory.getBinSlot().setLockStack(this.lock);
        inventory.onContentsChanged();
        return true;
    }
}