package mekanism.common.recipe.upgrade;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.attachments.LockData;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class LockRecipeData implements RecipeUpgradeData<LockRecipeData> {

    private final LockData lockData;

    LockRecipeData(LockData lockData) {
        this.lockData = lockData;
    }

    @Nullable
    @Override
    public LockRecipeData merge(LockRecipeData other) {
        return lockData.equals(other.lockData) ? this : null;
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess, TransactionContext transaction) {
        return ItemAccessUtils.exchange(itemAccess, itemAccess.getResource().with(MekanismDataComponents.LOCK, lockData), transaction);
    }
}