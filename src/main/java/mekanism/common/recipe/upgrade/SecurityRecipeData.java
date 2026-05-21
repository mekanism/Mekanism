package mekanism.common.recipe.upgrade;

import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SecurityRecipeData implements RecipeUpgradeData<SecurityRecipeData> {

    private final UUID owner;
    private final SecurityMode mode;

    SecurityRecipeData(UUID owner, SecurityMode mode) {
        this.owner = owner;
        this.mode = mode;
    }

    @Nullable
    @Override
    public SecurityRecipeData merge(SecurityRecipeData other) {
        if (owner.equals(other.owner)) {
            //Pick the most restrictive security mode
            return ISecurityUtils.INSTANCE.moreRestrictive(mode, other.mode) ? other : this;
        }
        //If the owners don't match fail
        return null;
    }

    @Override
    public boolean applyToStack(ItemStack stack) {
        //TODO - 26.1: Test that this properly updates the stack in place (it should)
        ItemAccess itemAccess = ItemAccess.forStack(stack);
        IOwnerObject ownerObject = IItemSecurityUtils.INSTANCE.ownerCapability(itemAccess);
        if (ownerObject != null) {
            try (Transaction transaction = Transaction.openRoot()) {
                ownerObject.setOwnerUUID(owner, transaction);
                ISecurityObject security = IItemSecurityUtils.INSTANCE.securityCapability(itemAccess);
                if (security != null) {
                    security.setSecurityMode(mode, transaction);
                }
                transaction.commit();
            }
        }
        return true;
    }
}